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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<ServiceProvider> findServiceProvidersAsList() {
        return repository.findAll();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders, QpidDelta delta) {
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}",name);

            addOrRemoveServiceProviderToBiConsumerGroup(serviceProvider);
            syncPrivateChannels(serviceProvider, delta);
            serviceProvider = tearDownDeliveryQueues(serviceProvider, delta);
            serviceProvider = tearDownCapabilityExchanges(serviceProvider, delta);
            serviceProvider = syncSubscriptions(serviceProvider, delta);
            serviceProvider = removeUnwantedSubscriptions(serviceProvider);

            ServiceProviderMember groupMember = qpidClient.getServiceProviderMember(serviceProvider.getName());
            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                if (groupMember == null) {
                    qpidClient.addServiceProviderMemberToGroup(serviceProvider.getName());
                }
            } else {
                if (groupMember != null) {
                    qpidClient.removeServiceProviderMemberFromGroup(groupMember);
                }
            }

            serviceProvider = setUpCapabilityExchanges(serviceProvider, delta);
            bindCapabilityExchangesToBiQueue(serviceProvider, delta);
            serviceProvider = syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider, delta, serviceProviders);
            serviceProvider = setUpDeliveryQueue(serviceProvider, delta);
        }
    }

    public void addOrRemoveServiceProviderToBiConsumerGroup(ServiceProvider serviceProvider) {
        BiConsumerMember biConsumerMember = qpidClient.getBiConsumerMember(serviceProvider.getName());
        if (Boolean.TRUE.equals(serviceProvider.isBiconsumer())) {
            if (biConsumerMember == null) {
                qpidClient.addBiConsumerMemberToGroup(serviceProvider.getName());
            }
        } else {
            if (biConsumerMember != null) {
                qpidClient.removeBiConsumerMemberFromGroup(biConsumerMember);
            }
        }
    }

    public ServiceProvider syncSubscriptions(ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getSubscriptions().isEmpty()) {
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    processSubscription(serviceProvider, subscription, nodeProperties.getBrokerExternalName(), nodeProperties.getMessageChannelPort(), delta);
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
        } else {
            throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void optionallyCreateQueue(String queueName, String serviceProviderName, QpidDelta delta) {
        Queue queue = delta.findByQueueName(queueName);
        if (queue == null) {
            logger.info("Creating queue {}", queueName);
            queue = qpidClient.createQueue(queueName);
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
        PrivateChannelMember privateChannelUser = delta.findByPrivateChannelUserName(name);
        if (privateChannelUser == null) {
            privateChannelUser = qpidClient.addPrivateChannelMemberToGroup(name);
            delta.addPrivateChannelUser(privateChannelUser);
            logger.debug("Adding member {} to private channel group", name);
        }

        for (PrivateChannel privateChannel : privateChannels) {
            String queueName = privateChannel.getEndpoint().getQueueName();

            if (privateChannel.getStatus().equals(PrivateChannelStatus.REQUESTED)) {
                Queue queue = delta.findByQueueName(queueName);
                if (queue == null) {
                    queue = qpidClient.createNonDestructiveQueue(queueName);
                    delta.addQueue(queue);
                }
                logger.info("Creating queue {}", queueName);
                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.addExchangeWriteAccess(name, queueName);
                provider.addQueueReadAccess(name, queueName);
                for (Peer peer : privateChannel.getPeers()) {
                    String peerName = peer.getName();
                    PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                    if (peerMember == null) {
                        peerMember = qpidClient.addPrivateChannelMemberToGroup(peerName);
                        logger.debug("Adding member {} to private channel group", peer.getName());
                    }
                    peer.setStatus(PeerStatus.CREATED);
                    provider.addQueueReadAccess(peer.getName(), queueName);
                }
                qpidClient.postQpidAcl(provider);
                privateChannel.setStatus(PrivateChannelStatus.CREATED);
                logger.info("Creating private channel {} for client {}", queueName, name);
                privateChannel.setLastUpdated(LocalDateTime.now());
                privateChannelRepository.save(privateChannel);
            }
            if (privateChannel.getStatus().equals(PrivateChannelStatus.CREATED)) {
                Set<Peer> requestedPeers = privateChannel.getPeers().stream().filter(peer -> peer.getStatus().equals(PeerStatus.REQUESTED)).collect(Collectors.toSet());
                if (!requestedPeers.isEmpty()) {
                    VirtualHostAccessController provider = qpidClient.getQpidAcl();
                    for (Peer peer : requestedPeers) {
                        String peerName = peer.getName();
                        PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                        if (peerMember == null) {
                            peerMember = qpidClient.addPrivateChannelMemberToGroup(peerName);
                            delta.addPrivateChannelUser(peerMember);
                            logger.debug("Adding member {} to private channel group", peer.getName());
                        }
                        peer.setStatus(PeerStatus.CREATED);
                        provider.addQueueReadAccess(peer.getName(), queueName);
                        privateChannel.setLastUpdated(LocalDateTime.now());
                        privateChannelRepository.save(privateChannel);
                    }
                }

                Set<Peer> peersToRemove = privateChannel.getPeers().stream().filter(peer -> peer.getStatus().equals(PeerStatus.TEAR_DOWN)).collect(Collectors.toSet());
                if (!peersToRemove.isEmpty()) {
                    VirtualHostAccessController provider = qpidClient.getQpidAcl();
                    for (Peer peer : peersToRemove) {
                        String peerName = peer.getName();
                        long channelsWithPeerAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED).size();
                        long channelsWithPeerAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(peerName, PrivateChannelStatus.CREATED);

                        if (channelsWithPeerAsPeer <= 1 && channelsWithPeerAsServiceProvider == 0) {
                            PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                            if (peerMember != null) {
                                qpidClient.removePrivateChannelMemberFromGroup(peerMember);
                                delta.removePrivateChannelUser(peerMember);
                                logger.info("Adding member {} to private channel group", peerName);
                            }
                        }
                        provider.removeQueueReadAccess(peerName, queueName);
                    }
                    privateChannel.removePeers(peersToRemove);
                    privateChannel.setLastUpdated(LocalDateTime.now());
                    privateChannelRepository.save(privateChannel);
                }
            }

            if (privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                long channelsWithServiceProviderAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED).size();
                long channelsWithServiceProviderAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(privateChannel.getServiceProviderName(), PrivateChannelStatus.CREATED);

                if (channelsWithServiceProviderAsServiceProvider == 0 && channelsWithServiceProviderAsPeer == 0) {
                    PrivateChannelMember groupMember = delta.findByPrivateChannelUserName(name);
                    if (groupMember != null && privateChannelsWithStatusCreated.isEmpty()) {
                        qpidClient.removePrivateChannelMemberFromGroup(groupMember);
                        delta.removePrivateChannelUser(groupMember);
                        logger.debug("Adding member {} to private channel group", name);
                    }
                }

                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                for (Peer peer : privateChannel.getPeers()) {
                    String peerName = peer.getName();
                    long channelsWithPeerAsPeer = privateChannelRepository.findAllByPeerNameAndStatus(peerName, PrivateChannelStatus.CREATED).size();
                    long channelsWithPeerAsServiceProvider = privateChannelRepository.countByServiceProviderNameAndStatus(peerName, PrivateChannelStatus.CREATED);

                    if (channelsWithPeerAsPeer == 0 && channelsWithPeerAsServiceProvider == 0) {
                        PrivateChannelMember peerMember = delta.findByPrivateChannelUserName(peerName);
                        if (peerMember != null) {
                            qpidClient.removePrivateChannelMemberFromGroup(peerMember);
                            delta.removePrivateChannelUser(peerMember);
                            logger.info("Adding member {} to private channel group", peerName);
                        }
                    }
                    provider.removeQueueReadAccess(peerName, queueName);
                }
                provider.removeQueueWriteAccess(name, queueName);
                provider.removeQueueReadAccess(name, queueName);
                qpidClient.postQpidAcl(provider);
                logger.info("Tearing down queue {} for client {}", queueName, name);
                Queue queue = delta.findByQueueName(queueName);
                if (queue != null) {
                    qpidClient.removeQueue(queue);
                    delta.removeQueue(queue);
                }
                privateChannel.setLastUpdated(LocalDateTime.now());
                privateChannelRepository.save(privateChannel);
            }
        }
    }

    public ServiceProvider setUpCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        Set<Capability> requestedCaps = serviceProvider.getCapabilities().getCapabilitiesByStatusIsNot(CapabilityStatus.TEAR_DOWN);
        for (Capability capability : requestedCaps) {
            if (!capability.hasShards()) {
                List<CapabilityShard> newShards = new ArrayList<>();
                int numberOfShards = capability.getMetadata().getShardCount();
                for (int i = 0; i < numberOfShards; i++) {
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
                    CapabilityShard newShard = new CapabilityShard(i + 1, exchangeName, capabilitySelector);
                    newShards.add(newShard);
                }
                capability.setShards(newShards);
                capability.setStatus(CapabilityStatus.CREATED);
            } else {
                for (CapabilityShard shard : capability.getShards()) {
                    Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                    if (exchange == null) {
                        exchange = qpidClient.createHeadersExchange(shard.getExchangeName());
                        delta.addExchange(exchange);
                    }
                }
                capability.setStatus(CapabilityStatus.CREATED);
            }
        }
        return repository.save(serviceProvider);
    }

    public void bindCapabilityExchangesToBiQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
            for (CapabilityShard shard : capability.getShards()) {
                Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                if (exchange != null) {
                    if (!exchange.isBoundTo("bi-queue")) {
                        Binding binding = new Binding(shard.getExchangeName(), "bi-queue", new Filter(shard.getSelector()));
                        qpidClient.addBinding(shard.getExchangeName(), binding);
                        exchange.addBinding(binding);
                    }
                } else {
                    logger.info("Could not bind capability {}, shard with exchange name {} to bi-queue, exchange does not exist", capability.getUuid(), shard.getExchangeName());
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
                if (capability.hasShards()) {
                    for (CapabilityShard shard : capability.getShards()) {
                        Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
                        if (exchange != null) {
                            qpidClient.removeExchange(exchange);
                            logger.info("Removed exchange {} for Capability with id {}", shard.getExchangeName(), capability.getId());
                            delta.removeExchange(exchange);
                        }
                    }
                    capability.removeShards();
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
                    for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                        String exchangeName = endpoint.getTarget();
                        Exchange exchange = delta.findByExchangeName(exchangeName);
                        if (exchange == null) {
                            if (endpoint.getDlqName() != null) {
                                Queue queue = qpidClient.getQueue(endpoint.getDlqName());
                                if (queue == null) {
                                    Queue createdDlq = qpidClient.createQueue(endpoint.getDlqName());
                                    qpidClient.addReadAccess(serviceProvider.getName(),createdDlq.getName());
                                    delta.addQueue(createdDlq);
                                }
                                exchange = qpidClient.createDirectExchangeWithDlq(exchangeName, endpoint.getDlqName());
                                logger.info("Created direct exchange {} with dlqueue {}", exchangeName, endpoint.getDlqName());
                            } else {
                                exchange = qpidClient.createDirectExchange(exchangeName);
                                logger.info("Created exchange {}", exchangeName);
                            }
                            qpidClient.addWriteAccess(serviceProvider.getName(), exchangeName);
                            delta.addExchange(exchange);
                        }
                    }

                    for (OutgoingMatch match : matches) {
                        Capability capability = match.getCapability();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            for (CapabilityShard shard : capability.getShards()) {
                                Exchange endpointExchange = delta.findByExchangeName(endpoint.getTarget());
                                Exchange shardExchange = delta.findByExchangeName(shard.getExchangeName());

                                //NOTE, there's not much chance of the endpointExchange not existing, since it most likely
                                // is created in the previous loop if it didn't already exist
                                if (endpointExchange != null) {
                                    if (shardExchange != null) {
                                        if (!endpointExchange.isBoundTo(shardExchange.getName())) {
                                            if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), delivery.getSelector())) {
                                                String joinedSelector = joinTwoSelectors(shard.getSelector(), delivery.getSelector());
                                                Binding binding = new Binding(endpointExchange.getName(), shardExchange.getName(), new Filter(joinedSelector));
                                                qpidClient.addBinding(endpointExchange.getName(), binding);
                                                endpointExchange.addBinding(binding);
                                                logger.info("Added binding from {} to {}", endpointExchange.getName(), shardExchange.getName());
                                            }
                                        }
                                    } else {
                                        logger.info("No shard exchange found in qpid with name {}",shard.getExchangeName());
                                    }
                                } else {
                                    logger.info("No delivery endpoint exchange found in qpid with name {}",endpoint.getTarget());
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
                        HashSet<LocalDeliveryEndpoint> endpointsToRemove = new HashSet<>();
                        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                            if (endpoint.targetExists()) {
                                String target = endpoint.getTarget();
                                Exchange exchange = delta.findByExchangeName(target);
                                if (exchange != null) {
                                    logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                                    qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                                    qpidClient.removeExchange(exchange);
                                    delta.removeExchange(exchange);
                                }
                                endpointsToRemove.add(endpoint);
                            }
                            if (endpoint.getDlqName() != null) {
                                String dlqName = endpoint.getDlqName();
                                Queue dlq = delta.findByQueueName(dlqName);
                                if (dlq != null) {
                                    logger.info("Removing endpoint with dlQueue with name {} for service provider {}", dlqName, serviceProvider.getName());
                                    qpidClient.removeReadAccess(serviceProvider.getName(), dlqName);
                                    qpidClient.removeQueue(dlq);
                                    delta.removeQueue(dlq);
                                }
                            }
                        }
                        delivery.removeAllEndpoints(endpointsToRemove);
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
            for (LocalSubscription localSubscription : serviceProvider.wantedNonRedirectSubscriptions()) {
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
                                            if (queue != null && !exchange.isBoundTo(queue.getName())) {
                                                String exchangeName = exchange.getName();
                                                logger.debug("Adding bindings from queue {} to exchange {}", queueName, exchangeName);
                                                Binding binding = new Binding(exchangeName, queueName, new Filter(localSubscription.getSelector()));
                                                qpidClient.addBinding(exchangeName, binding);
                                                exchange.addBinding(binding);
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

    public ServiceProvider syncLocalSubscriptionsToServiceProviderCapabilities(ServiceProvider serviceProvider, QpidDelta delta, Iterable<ServiceProvider> serviceProviders) {
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<Capability> allCreatedCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> activeSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : activeSubscriptions) {
                removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCreatedCapabilities);
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCreatedCapabilities, subscription.getSelector());
                    for (Capability capability : matchingCapabilities) {
                        for (CapabilityShard shard : capability.getShards()) {
                            if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), subscription.getSelector())){
                                Exchange shardExchange = delta.findByExchangeName(shard.getExchangeName());
                                if (shardExchange != null) {
                                    //TODO need a better way of getting the endpoint
                                    Optional<LocalEndpoint> maybeEndpoint = subscription.getLocalEndpoints().stream().findFirst();
                                    if (maybeEndpoint.isPresent()) {
                                        String source = maybeEndpoint.get().getSource();
                                        if (! shardExchange.isBoundTo(source)) {
                                            Binding binding = new Binding(shard.getExchangeName(), source, new Filter(subscription.getSelector()));
                                            qpidClient.addBinding(shard.getExchangeName(), binding);
                                            shardExchange.addBinding(binding);
                                        }
                                        if (! isExistingConnection(subscription,shard)) {
                                            LocalConnection connection = new LocalConnection(shard.getExchangeName(), source);
                                            subscription.addConnection(connection);
                                        }
                                    } else {
                                        logger.warn("Cound not find endpoint for subscription {}", subscription.getId());
                                    }
                                } else {
                                    logger.info("Could not find exchange {} for shard", shard.getExchangeName());
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

    private boolean isExistingConnection(LocalSubscription subscription, CapabilityShard shard) {
        Set<String> existingConnections = subscription.getConnections().stream()
                .map(LocalConnection::getSource)
                .collect(Collectors.toSet());
        return existingConnections.contains(shard.getExchangeName());
    }

    public void removeUnusedLocalConnectionsFromLocalSubscription(LocalSubscription subscription, Set<Capability> capabilities) {
        Set<String> existingConnections = new HashSet<>();
        for (Capability cap : capabilities) {
            existingConnections.addAll(cap.getExchangesFromShards());
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
