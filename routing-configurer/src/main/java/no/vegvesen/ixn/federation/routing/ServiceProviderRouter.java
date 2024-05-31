package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME;

@Component
@ConfigurationPropertiesScan("no.vegvesen.ixn")
public class ServiceProviderRouter {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouter.class);

    private final ServiceProviderRepository repository;
    private final PrivateChannelRepository privateChannelRepository;
    private final QpidClient qpidClient;
    private final MatchRepository matchRepository;
    private final OutgoingMatchRepository outgoingMatchRepository;
    private final InterchangeNodeProperties nodeProperties;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, PrivateChannelRepository privateChannelRepository, QpidClient qpidClient, MatchRepository matchRepository, OutgoingMatchRepository outgoingMatchRepository, InterchangeNodeProperties nodeProperties) {
        this.repository = repository;
        this.privateChannelRepository = privateChannelRepository;
        this.qpidClient = qpidClient;
        this.matchRepository = matchRepository;
        this.outgoingMatchRepository = outgoingMatchRepository;
        this.nodeProperties = nodeProperties;
    }


    public Iterable<ServiceProvider> findServiceProviders() {
        return repository.findAll();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders, QpidDelta delta) {
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}",name);
            syncPrivateChannels(serviceProvider, delta);
            serviceProvider = tearDownDeliveryQueues(serviceProvider, delta);
            serviceProvider = tearDownCapabilityExchanges(serviceProvider, delta);
            serviceProvider = syncSubscriptions(serviceProvider, delta);
            serviceProvider = removeUnwantedSubscriptions(serviceProvider);

            GroupMember groupMember = qpidClient.getGroupMember(serviceProvider.getName(),SERVICE_PROVIDERS_GROUP_NAME);
            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                if (groupMember == null) {
                    qpidClient.addMemberToGroup(serviceProvider.getName(),SERVICE_PROVIDERS_GROUP_NAME);
                }
            } else {
                if (groupMember != null) {
                    qpidClient.removeMemberFromGroup(groupMember,SERVICE_PROVIDERS_GROUP_NAME);
                }
            }

            serviceProvider = setUpCapabilityExchanges(serviceProvider, delta);
            bindCapabilityExchangesToBiQueue(serviceProvider, delta);
            serviceProvider = syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider, delta, serviceProviders);
            serviceProvider = setUpDeliveryQueue(serviceProvider, delta);
        }
    }

    public ServiceProvider syncSubscriptions(ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getSubscriptions().isEmpty()) {
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    processSubscription(serviceProvider, subscription, nodeProperties.getName(), nodeProperties.getMessageChannelPort(), delta);
                } else {
                    processRedirectSubscription(subscription);
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public void processSubscription(ServiceProvider serviceProvider, LocalSubscription subscription, String nodeName, String messageChannelPort, QpidDelta delta) {
        switch (subscription.getStatus()) {
            case REQUESTED:
                if (subscription.getLocalEndpoints().isEmpty()) {
                    String queueName = "loc-" + UUID.randomUUID().toString();
                    LocalEndpoint endpoint = new LocalEndpoint(queueName, nodeName, Integer.parseInt(messageChannelPort));
                    subscription.getLocalEndpoints().add(endpoint);
                }
                //NOTE fallthrough!
            case CREATED:
                onRequested(serviceProvider.getName(), subscription, delta);
                break;
            case TEAR_DOWN:
                //	Check that the binding exist, if so, delete it
                onTearDown(serviceProvider, subscription, delta);
                break;
            case ILLEGAL:
                // Remove the subscription from the ServiceProvider
                //serviceProvider.removeSubscription(subscription);
                break;
                //needs testing.
            case ERROR:
                subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
                break;
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void onTearDown(ServiceProvider serviceProvider, LocalSubscription subscription, QpidDelta delta) {
        Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            Queue queue = delta.findByQueueName(source);
            if (queue != null) {
                qpidClient.removeReadAccess(serviceProvider.getName(), source);
                qpidClient.removeQueue(queue);
                delta.removeQueue(queue);
                logger.info("Removed queue for LocalSubscription {}", subscription);
            }
            endpointsToRemove.add(endpoint);
        }
        if (!endpointsToRemove.isEmpty()) {
            subscription.getLocalEndpoints().removeAll(endpointsToRemove);
        }
        subscription.getConnections().clear();
    }

    public ServiceProvider removeUnwantedSubscriptions(ServiceProvider serviceProvider) {
        if (!serviceProvider.getSubscriptions().isEmpty()) {
            Set<LocalSubscription> subscriptionsToRemove = new HashSet<>();
            for (LocalSubscription localSubscription : serviceProvider.getSubscriptions()) {
                if (!localSubscription.isSubscriptionWanted()) {
                    List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                    if (matches.isEmpty() && localSubscription.getLocalEndpoints().isEmpty()) {
                        subscriptionsToRemove.add(localSubscription);
                    }
                }
            }
            serviceProvider.removeSubscriptions(subscriptionsToRemove);
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    private void onRequested(String serviceProviderName, LocalSubscription subscription, QpidDelta delta) {
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            optionallyCreateQueue(source, serviceProviderName, delta);
        }
        subscription.setStatus(LocalSubscriptionStatus.CREATED);
    }

    public void processRedirectSubscription(LocalSubscription subscription) {
        if (subscription.getStatus().equals(LocalSubscriptionStatus.REQUESTED)) {
            subscription.setStatus(LocalSubscriptionStatus.CREATED);
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.CREATED)) {
            //Just skip
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN)) {
            subscription.getLocalEndpoints().clear();
        } else if (subscription.getStatus().equals(LocalSubscriptionStatus.ILLEGAL)) {
            subscription.getLocalEndpoints().clear();
            subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        }else if(subscription.getStatus().equals(LocalSubscriptionStatus.ERROR)){
            subscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
        }
        else {
            throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void optionallyCreateQueue(String queueName, String serviceProviderName, QpidDelta delta) {
        if (!delta.queueExists(queueName)) {
            logger.info("Creating queue {}", queueName);
            Queue queue = qpidClient.createQueue(queueName);
            qpidClient.addReadAccess(serviceProviderName, queueName);
            delta.addQueue(queue);
        }
    }

    public void syncPrivateChannels(ServiceProvider serviceProvider, QpidDelta delta) {
        List<PrivateChannel> privateChannelList = privateChannelRepository.findAllByServiceProviderName(serviceProvider.getName());
        if (!privateChannelList.isEmpty()) {
            syncPrivateChannelsWithQpid(privateChannelList, serviceProvider.getName(), delta);
            privateChannelList.stream().filter((a) -> a.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)).forEach(privateChannelRepository::delete);
        }

    }

    private void syncPrivateChannelsWithQpid(List<PrivateChannel> privateChannels, String name, QpidDelta delta) {

        List<PrivateChannel> privateChannelsWithStatusCreated = privateChannelRepository.findAllByStatusAndServiceProviderName(PrivateChannelStatus.CREATED, name);
        GroupMember groupMember = qpidClient.getGroupMember(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
        if (groupMember == null) {
            qpidClient.addMemberToGroup(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
            logger.debug("Adding member {} to group {}", name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
        }

        for (PrivateChannel privateChannel : privateChannels) {

            String peerName = privateChannel.getPeerName();
            String queueName = privateChannel.getEndpoint().getQueueName();

            if (privateChannel.getStatus().equals(PrivateChannelStatus.REQUESTED)) {
                GroupMember peer = qpidClient.getGroupMember(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                if (peer == null) {
                    qpidClient.addMemberToGroup(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                }
                logger.debug("Adding member {} to group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                if (!delta.queueExists(queueName)) {
                    Queue queue = qpidClient.createQueue(queueName);
                    delta.addQueue(queue);
                }
                logger.info("Creating queue {}", queueName);
                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.addExchangeWriteAccess(name, queueName);
                provider.addExchangeWriteAccess(peerName, queueName);
                provider.addQueueReadAccess(name, queueName);
                provider.addQueueReadAccess(peerName, queueName);
                qpidClient.postQpidAcl(provider);
                privateChannel.setStatus(PrivateChannelStatus.CREATED);
                logger.info("Creating queue {} for client {}", queueName, peerName);
            }
            if (privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                GroupMember member = qpidClient.getGroupMember(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                long channelsWithPeerAsPeer = privateChannelRepository.countByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED);
                long channelsWithServiceProviderAsPeer = privateChannelRepository.countByPeerNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED);

                long channelsWithServiceProviderAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED);
                long channelsWithPeerAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(peerName, PrivateChannelStatus.CREATED);

                if (channelsWithServiceProviderAsServiceProvider == 0 && channelsWithServiceProviderAsPeer == 0) {
                    if (member != null && privateChannelsWithStatusCreated.isEmpty()) {
                        qpidClient.removeMemberFromGroup(member, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                        logger.debug("Removing member {} from group {}", name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    }
                }
                if(channelsWithPeerAsPeer == 0 && channelsWithPeerAsServiceProvider == 0) {
                    GroupMember peer = qpidClient.getGroupMember(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    if (peer != null) {
                        qpidClient.removeMemberFromGroup(peer, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                        logger.info("Removing member {} from group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    }
                }

                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.removeQueueWriteAccess(peerName, queueName);
                provider.removeQueueWriteAccess(name, queueName);
                provider.removeQueueReadAccess(peerName, queueName);
                provider.removeQueueReadAccess(name, queueName);
                qpidClient.postQpidAcl(provider);
                logger.info("Tearing down queue {} for client {}", queueName, peerName);
                Queue queue = delta.findByQueueName(queueName);
                if (queue != null) {
                    qpidClient.removeQueue(queue);
                    delta.removeQueue(queue);
                }
            }
            privateChannelRepository.save(privateChannel);
        }
    }

    public ServiceProvider setUpCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        if (serviceProvider.hasCapabilities()) {
            for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
                if (!capability.getMetadata().hasShards() && capability.getStatus().equals(CapabilityStatus.REQUESTED)) {
                    List<CapabilityShard> newShards = new ArrayList<>();
                    int numberOfShards = capability.getMetadata().getShardCount();
                    for (int i = 0; i<numberOfShards; i++) {
                        String exchangeName = "cap-" + UUID.randomUUID();
                        Exchange exchange = qpidClient.createHeadersExchange(exchangeName);
                        logger.info("Created exchange {} for Capability with id {}", exchangeName, capability.getId());
                        delta.addExchange(exchange);

                        String capabilitySelector;
                        if (capability.isSharded()) {
                            capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, i+1);
                        } else {
                            capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability, null);
                        }
                        CapabilityShard newShard = new CapabilityShard(i+1, exchangeName, capabilitySelector);
                        newShards.add(newShard);
                    }
                    capability.getMetadata().setShards(newShards);
                    capability.setStatus(CapabilityStatus.CREATED);
                }
            }
        }
        return repository.save(serviceProvider);
    }

    public void bindCapabilityExchangesToBiQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
            for (CapabilityShard shard : capability.getMetadata().getShards()) {
                if (!delta.exchangeHasBindingToQueue(shard.getExchangeName(), "bi-queue")){
                    qpidClient.addBinding(shard.getExchangeName(), new Binding(shard.getExchangeName(), "bi-queue", new Filter(shard.getSelector())));
                    delta.addBindingToExchange(shard.getExchangeName(), shard.getSelector(), "bi-queue");
                }
            }
        }
    }

    public ServiceProvider tearDownCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        Set<Capability> tearDownCapabilities = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(capability -> capability.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        if (!tearDownCapabilities.isEmpty()) {
            for (Capability capability : tearDownCapabilities) {
                if (capability.getMetadata().hasShards()) {
                    for (CapabilityShard shard : capability.getMetadata().getShards()) {
                        Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                        if (exchange != null) {
                            qpidClient.removeExchange(exchange);
                            logger.info("Removed exchange {} for Capability with id {}", shard.getExchangeName(), capability.getId());
                            delta.removeExchange(exchange);
                        }
                    }
                    capability.getMetadata().removeShards();
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public ServiceProvider setUpDeliveryQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        if (serviceProvider.hasDeliveries()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                    List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId());
                    if (!delta.exchangeExists(delivery.getExchangeName())) {
                        String exchangeName = delivery.getExchangeName();
                        Exchange exchange = qpidClient.createDirectExchange(exchangeName);
                        qpidClient.addWriteAccess(serviceProvider.getName(), exchangeName);
                        delta.addExchange(exchange);
                    }

                    for (OutgoingMatch match : matches) {
                        Capability capability = match.getCapability();
                        if (capability.hasShards()) {
                            if (capability.isSharded()) {
                                //TODO: Sharding, check if selector is sharded as well.
                            } else {
                                CapabilityShard shard = capability.getMetadata().getShards().get(0);
                                if (!delta.exchangeHasBindingToQueue(delivery.getExchangeName(), shard.getExchangeName())) {
                                    String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                                    qpidClient.addBinding(delivery.getExchangeName(), new Binding(delivery.getExchangeName(), shard.getExchangeName(), new Filter(joinedSelector)));
                                    delta.addBindingToExchange(delivery.getExchangeName(), joinedSelector, shard.getExchangeName());
                                }
                            }
                        }
                    }
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public ServiceProvider tearDownDeliveryQueues(ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getDeliveries().isEmpty()) {
            for (LocalDelivery delivery : serviceProvider.getDeliveries()) {
                if (!delivery.getStatus().equals(LocalDeliveryStatus.ILLEGAL)
                        && !delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)) {
                    List<OutgoingMatch> matches = outgoingMatchRepository.findAllByLocalDelivery_Id(delivery.getId());
                    if (matches.isEmpty()) {
                        if (delivery.exchangeExists()) {
                            String target = delivery.getExchangeName();
                            Exchange exchange = delta.findByExchangeName(target);
                            if (exchange != null) {
                                logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                                qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                                qpidClient.removeExchange(exchange);
                                delta.removeExchange(exchange);
                            }
                            delivery.setExchangeName("");
                        }
                        if (!(delivery.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN) || delivery.getStatus().equals(LocalDeliveryStatus.ERROR))) {
                            delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                        }
                    }
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public String joinTwoSelectors(String firstSelector, String secondSelector) {
        return String.format("(%s) AND (%s)", firstSelector, secondSelector);
    }

    @Scheduled(fixedRateString = "${create-bindings-subscriptions-exchange.interval}")
    public void createBindingsWithMatches() {
        List<ServiceProvider> serviceProviders = repository.findAll();
        QpidDelta delta = qpidClient.getQpidDelta();
        for (ServiceProvider serviceProvider : serviceProviders) {
            for (LocalSubscription localSubscription : serviceProvider.getSubscriptions()) {
                if (localSubscription.isSubscriptionWanted() && !localSubscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                    if (!localSubscription.getLocalEndpoints().isEmpty()) {
                        List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                        for (Match match : matches) {
                            if (match.getSubscription().getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                                for (Endpoint endpoint : match.getSubscription().getEndpoints()) {
                                    if (endpoint.hasShard()) {
                                        Exchange exchange = delta.findByExchangeName(endpoint.getShard().getExchangeName());
                                        if (exchange != null) {
                                            for (String queueName : localSubscription.getLocalEndpoints().stream().map(LocalEndpoint::getSource).collect(Collectors.toSet())) {
                                                Queue queue = delta.findByQueueName(queueName);
                                                if (queue != null && !delta.getDestinationsFromExchangeName(exchange.getName()).contains(queueName)) {
                                                    bindQueueToSubscriptionExchange(queueName, exchange.getName(), localSubscription);
                                                    delta.addBindingToExchange(exchange.getName(), localSubscription.getSelector(), queueName);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void bindQueueToSubscriptionExchange(String queueName, String exchangeName, LocalSubscription localSubscription) {
        logger.debug("Adding bindings from queue {} to exchange {}", queueName, exchangeName);
        qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(localSubscription.getSelector())));
    }

    public ServiceProvider syncLocalSubscriptionsToServiceProviderCapabilities(ServiceProvider serviceProvider, QpidDelta delta, Iterable<ServiceProvider> serviceProviders) {
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<Capability> allCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : serviceProviderSubscriptions) {
                removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCapabilities);
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities.stream().filter(c -> c.getStatus().equals(CapabilityStatus.CREATED)).collect(Collectors.toSet()), subscription.getSelector());
                    for (Capability capability : matchingCapabilities) {
                        Set<String> existingConnections = subscription.getConnections().stream()
                                .map(LocalConnection::getSource)
                                .collect(Collectors.toSet());

                        if (subscription.isSharded()) {
                            if (capability.isSharded()) {
                                for (CapabilityShard shard : capability.getMetadata().getShards()) {
                                    if (!existingConnections.contains(shard.getExchangeName())) {
                                        if (CapabilityMatcher.matchCapabilityShardToSelector(capability, shard.getShardId(), subscription.getSelector())) {
                                            createLocalEndpointForLocalSubscription(subscription, serviceProvider.getName(), delta, shard);
                                        }
                                    }
                                }
                            }
                        } else {
                            for (CapabilityShard shard : capability.getMetadata().getShards()) {
                                if (!existingConnections.contains(shard.getExchangeName())) {
                                    createLocalEndpointForLocalSubscription(subscription, serviceProvider.getName(), delta, shard);
                                }
                            }
                        }
                    }
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public void createLocalEndpointForLocalSubscription(LocalSubscription subscription, String serviceProviderName, QpidDelta delta, CapabilityShard shard) {
        String queueName = "loc-" + UUID.randomUUID();
        logger.debug("Creating local endpoint {} for local subscription with id {}", queueName, subscription.getId());
        LocalEndpoint endpoint = new LocalEndpoint(queueName, nodeProperties.getName(), Integer.parseInt(nodeProperties.getMessageChannelPort()));
        optionallyCreateQueue(endpoint.getSource(), serviceProviderName, delta);
        qpidClient.addBinding(shard.getExchangeName(), new Binding(shard.getExchangeName(), endpoint.getSource(), new Filter(subscription.getSelector())));
        LocalConnection connection = new LocalConnection(shard.getExchangeName(), endpoint.getSource());
        subscription.addConnection(connection);
        subscription.addLocalEndpoint(endpoint);
    }

    public void removeUnusedLocalConnectionsFromLocalSubscription(LocalSubscription subscription, Set<Capability> capabilities) {
        Set<String> existingConnections = new HashSet<>();
        for (Capability cap : capabilities) {
            existingConnections.addAll(cap.getMetadata().getExchangesFromShards());
        }

        Set<LocalConnection> unwantedConnections = new HashSet<>();
        for (LocalConnection connection : subscription.getConnections()) {
            if (!existingConnections.contains(connection.getSource())) {
                unwantedConnections.add(connection);
            }
        }
        subscription.getConnections().removeAll(unwantedConnections);
    }
}
