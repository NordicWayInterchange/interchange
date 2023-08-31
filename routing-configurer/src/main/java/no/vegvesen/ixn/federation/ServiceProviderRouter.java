package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
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
    private final QpidClient qpidClient;
    private final MatchRepository matchRepository;
    private final OutgoingMatchRepository outgoingMatchRepository;
    private final InterchangeNodeProperties nodeProperties;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, QpidClient qpidClient, MatchRepository matchRepository, OutgoingMatchRepository outgoingMatchRepository, InterchangeNodeProperties nodeProperties) {
        this.repository = repository;
        this.qpidClient = qpidClient;
        this.matchRepository = matchRepository;
        this.outgoingMatchRepository = outgoingMatchRepository;
        this.nodeProperties = nodeProperties;
    }


    public Iterable<ServiceProvider> findServiceProviders() {
        return repository.findAll();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders, QpidDelta delta) {
        List<String> groupMemberNames = qpidClient.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}",name);
            serviceProvider = syncPrivateChannels(serviceProvider, delta);
            serviceProvider = tearDownDeliveryQueues(serviceProvider, delta);
            serviceProvider = tearDownCapabilityExchanges(serviceProvider, delta);
            serviceProvider = syncSubscriptions(serviceProvider, delta);
            serviceProvider = removeUnwantedSubscriptions(serviceProvider);

            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                optionallyAddServiceProviderToGroup(groupMemberNames,name);
            } else {
                if (groupMemberNames.contains(serviceProvider.getName())){
                    removeServiceProviderFromGroup(name,SERVICE_PROVIDERS_GROUP_NAME);
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
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void onTearDown(ServiceProvider serviceProvider, LocalSubscription subscription, QpidDelta delta) {
        Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            if (delta.queueExists(source)) {
                qpidClient.removeReadAccess(serviceProvider.getName(), source);
                qpidClient.removeQueue(source);
                logger.info("Removed queue for LocalSubscription {}", subscription);
            }
            delta.removeQueue(source);
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
        } else {
            throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void optionallyCreateQueue(String queueName, String serviceProviderName, QpidDelta delta) {
        if (!delta.queueExists(queueName)) {
            logger.info("Creating queue {}", queueName);
            qpidClient.createQueue(queueName);
            qpidClient.addReadAccess(serviceProviderName, queueName);
            delta.addQueue(new Queue(queueName));
        }
    }

    private void optionallyAddServiceProviderToGroup(List<String> groupMemberNames, String name) {
        if (!groupMemberNames.contains(name)) {
            logger.debug("Adding member {} to group {}", name,SERVICE_PROVIDERS_GROUP_NAME);
            qpidClient.addMemberToGroup(name, SERVICE_PROVIDERS_GROUP_NAME);
        }
    }

    private void removeServiceProviderFromGroup(String name, String groupName) {
        logger.debug("Removing member {} from group {}", name,groupName);
        qpidClient.removeMemberFromGroup(name,groupName);
    }

    public ServiceProvider syncPrivateChannels(ServiceProvider serviceProvider, QpidDelta delta) {
        if (!serviceProvider.getPrivateChannels().isEmpty()) {
            Set<PrivateChannel> privateChannels = new HashSet<>();
            privateChannels.addAll(serviceProvider.getPrivateChannels());
            if (!privateChannels.isEmpty()) {
                syncPrivateChannelsWithQpid(privateChannels, serviceProvider.getName(), delta);
                Set<PrivateChannel> privateChannelsToRemove = privateChannels
                        .stream()
                        .filter(s -> s.getStatus().equals(PrivateChannelStatus.TEAR_DOWN))
                        .collect(Collectors.toSet());

                privateChannels.removeAll(privateChannelsToRemove);
                serviceProvider.setPrivateChannels(privateChannels);
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    private void syncPrivateChannelsWithQpid(Set<PrivateChannel> privateChannels, String name, QpidDelta delta) {
        List<String> groupMemberNames = qpidClient.getGroupMemberNames(CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
        Set<PrivateChannel> privateChannelsWithStatusCreated = privateChannels
                .stream()
                .filter(s -> s.getStatus().equals(PrivateChannelStatus.CREATED))
                .collect(Collectors.toSet());

        if(!groupMemberNames.contains(name)) {
            qpidClient.addMemberToGroup(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
            logger.debug("Adding member {} to group {}", name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
        }

        for(PrivateChannel privateChannel : privateChannels) {
            if (privateChannel.getQueueName() == null) {
                privateChannel.setQueueName(UUID.randomUUID().toString());
            }
            String peerName = privateChannel.getPeerName();
            String queueName = privateChannel.getQueueName();
            if(privateChannel.getStatus().equals(PrivateChannelStatus.REQUESTED)) {
                if (! groupMemberNames.contains(peerName)) {
                    qpidClient.addMemberToGroup(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    logger.debug("Adding member {} to group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                }
                if (!delta.queueExists(queueName)) {
                    qpidClient.createQueue(queueName);
                    delta.addQueue(new Queue(queueName));
                }
                logger.info("Creating queue {}", queueName);
                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.addQueueWriteAccess(name,queueName);
                provider.addQueueWriteAccess(peerName,queueName);
                provider.addQueueReadAccess(name,queueName);
                provider.addQueueReadAccess(peerName,queueName);
                qpidClient.postQpidAcl(provider);
                privateChannel.setStatus(PrivateChannelStatus.CREATED);
                logger.info("Creating queue {} for client {}", queueName, peerName);
            }
            if(privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                if(groupMemberNames.contains(name) && privateChannelsWithStatusCreated.isEmpty()){
                    qpidClient.removeMemberFromGroup(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    logger.debug("Removing member {} from group {}", name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                }
                //need to check if the peer has its own private channel
                qpidClient.removeMemberFromGroup(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                logger.info("Removing member {} from group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                VirtualHostAccessController provider = qpidClient.getQpidAcl();
                provider.removeQueueWriteAccess(peerName,queueName);
                provider.removeQueueWriteAccess(name,queueName);
                provider.removeQueueReadAccess(peerName,queueName);
                provider.removeQueueReadAccess(name,queueName);
                qpidClient.postQpidAcl(provider);
                logger.info("Tearing down queue {} for client {}", queueName, peerName);
                if (delta.queueExists(queueName)) {
                    qpidClient.removeQueue(queueName);
                }
                delta.removeQueue(queueName);
            }
        }
    }

    public ServiceProvider setUpCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        if (serviceProvider.hasCapabilities()) {
            for (CapabilitySplit capability : serviceProvider.getCapabilities().getCapabilities()) {
                if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                    if (!capability.exchangeExists()) {
                        String exchangeName = "cap-" + UUID.randomUUID();
                        capability.setCapabilityExchangeName(exchangeName);
                    }
                    if (!delta.exchangeExists(capability.getCapabilityExchangeName())) {
                        qpidClient.createTopicExchange(capability.getCapabilityExchangeName());
                        logger.info("Created exchange {} for Capability with id {}", capability.getCapabilityExchangeName(), capability.getId());
                        delta.addExchange(new Exchange(capability.getCapabilityExchangeName()));
                    }
                }
            }
        }
        return repository.save(serviceProvider);
    }

    public void bindCapabilityExchangesToBiQueue(ServiceProvider serviceProvider, QpidDelta delta) {
        for (CapabilitySplit capability : serviceProvider.getCapabilities().getCapabilities()) {
            if (capability.exchangeExists()) {
                if (!delta.exchangeHasBindingToQueue(capability.getCapabilityExchangeName(), "bi-queue")){
                    String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability);
                    qpidClient.addBinding(capabilitySelector, capability.getCapabilityExchangeName(), "bi-queue", capability.getCapabilityExchangeName());
                    delta.addBindingToExchange(capability.getCapabilityExchangeName(), capabilitySelector, "bi-queue");
                }
            }
        }
    }

    public ServiceProvider tearDownCapabilityExchanges(ServiceProvider serviceProvider, QpidDelta delta) {
        Set<CapabilitySplit> tearDownCapabilities = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(capability -> capability.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        if (!tearDownCapabilities.isEmpty()) {
            for (CapabilitySplit capability : tearDownCapabilities) {
                if (capability.exchangeExists()) {
                    if (delta.exchangeExists(capability.getCapabilityExchangeName())) {
                        qpidClient.removeExchange(capability.getCapabilityExchangeName());
                        logger.info("Removed exchange {} for Capability with id {}", capability.getCapabilityExchangeName(), capability.getId());
                    }
                    delta.removeExchange(capability.getCapabilityExchangeName());
                    capability.setCapabilityExchangeName(""); //empty name to signal that there is no exchange present for this capability anymore
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
                        qpidClient.createDirectExchange(delivery.getExchangeName());
                        qpidClient.addWriteAccess(serviceProvider.getName(), delivery.getExchangeName());
                        delta.addExchange(new Exchange(delivery.getExchangeName()));
                    }

                    for (OutgoingMatch match : matches) {
                        if (match.getCapability().getStatus().equals(CapabilityStatus.CREATED) &&
                                match.getCapability().exchangeExists()) {

                            if (!delta.exchangeHasBindingToQueue(delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName())) {
                                String joinedSelector = joinDeliverySelectorWithCapabilitySelector(match.getCapability(), delivery.getSelector());
                                qpidClient.addBinding(joinedSelector, delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName(), delivery.getExchangeName());
                                delta.addBindingToExchange(delivery.getExchangeName(), joinedSelector, match.getCapability().getCapabilityExchangeName());
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
                            if (delta.exchangeExists(target)) {
                                logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                                qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                                qpidClient.removeExchange(target);
                            }
                            delta.removeExchange(target);
                            delivery.setExchangeName("");
                        }
                        if (!delivery.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)) {
                            delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                        }
                    }
                }
            }
            serviceProvider = repository.save(serviceProvider);
        }
        return serviceProvider;
    }

    public String joinDeliverySelectorWithCapabilitySelector(CapabilitySplit capability, String selector) {
        return MessageValidatingSelectorCreator.makeSelectorJoinedWithCapabilitySelector(selector,capability);
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
                            if (match.getSubscription().isSubscriptionWanted() && match.getSubscription().exchangeIsCreated()) {
                                for (String queueName : localSubscription.getLocalEndpoints().stream().map(LocalEndpoint::getSource).collect(Collectors.toSet())) {
                                    String exchangeName = match.getSubscription().getExchangeName();
                                    if (!delta.getDestinationsFromExchangeName(exchangeName).contains(queueName)) {
                                        bindQueueToSubscriptionExchange(queueName, exchangeName, localSubscription);
                                        delta.addBindingToExchange(exchangeName, localSubscription.getSelector(), queueName);
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
        qpidClient.addBinding(localSubscription.getSelector(), exchangeName, queueName, exchangeName);
    }

    public ServiceProvider syncLocalSubscriptionsToServiceProviderCapabilities(ServiceProvider serviceProvider, QpidDelta delta, Iterable<ServiceProvider> serviceProviders) {
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<CapabilitySplit> allCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : serviceProviderSubscriptions) {
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCapabilities);
                    if (!allCapabilities.isEmpty()) {
                        if (!subscription.getLocalEndpoints().isEmpty()) {
                            Set<String> existingConnections = subscription.getConnections().stream()
                                    .map(LocalConnection::getSource)
                                    .collect(Collectors.toSet());

                            Set<CapabilitySplit> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, subscription.getSelector());
                            for (CapabilitySplit capability : matchingCapabilities) {
                                if (capability.exchangeExists() && !existingConnections.contains(capability.getCapabilityExchangeName())) {
                                    if (delta.exchangeExists(capability.getCapabilityExchangeName())) {
                                        LocalEndpoint endpoint = subscription.getLocalEndpoints().stream().findFirst().get();
                                        qpidClient.addBinding(subscription.getSelector(), capability.getCapabilityExchangeName(), endpoint.getSource(), capability.getCapabilityExchangeName());
                                        delta.addBindingToExchange(capability.getCapabilityExchangeName(), subscription.getSelector(), endpoint.getSource());
                                        LocalConnection connection = new LocalConnection(capability.getCapabilityExchangeName(), endpoint.getSource());
                                        subscription.addConnection(connection);
                                    }
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

    public void removeUnusedLocalConnectionsFromLocalSubscription(LocalSubscription subscription, Set<CapabilitySplit> capabilities) {
        Set<String> existingConnections = capabilities.stream()
                .map(CapabilitySplit::getCapabilityExchangeName)
                .collect(Collectors.toSet());

        Set<LocalConnection> unwantedConnections = new HashSet<>();
        for (LocalConnection connection : subscription.getConnections()) {
            if (!existingConnections.contains(connection.getSource())) {
                unwantedConnections.add(connection);
            }
        }
        subscription.getConnections().removeAll(unwantedConnections);
    }
}
