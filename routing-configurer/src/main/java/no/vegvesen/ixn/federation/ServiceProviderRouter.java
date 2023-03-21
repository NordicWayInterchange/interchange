package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidAcl;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private final OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;
    private final InterchangeNodeProperties nodeProperties;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, QpidClient qpidClient, MatchRepository matchRepository, OutgoingMatchDiscoveryService outgoingMatchDiscoveryService, InterchangeNodeProperties nodeProperties) {
        this.repository = repository;
        this.qpidClient = qpidClient;
        this.matchRepository = matchRepository;
        this.outgoingMatchDiscoveryService = outgoingMatchDiscoveryService;
        this.nodeProperties = nodeProperties;
    }


    public Iterable<ServiceProvider> findServiceProviders() {
		return repository.findAll();
    }

    public void syncServiceProviders(Iterable<ServiceProvider> serviceProviders) {
        List<String> groupMemberNames = qpidClient.getGroupMemberNames(SERVICE_PROVIDERS_GROUP_NAME);
        for (ServiceProvider serviceProvider : serviceProviders) {
            String name = serviceProvider.getName();
            logger.debug("Checking service provider {}",name);
            syncPrivateChannels(name);
            tearDownDeliveryQueues(name);
            tearDownCapabilityExchanges(name);
            syncSubscriptions(name);
            removeUnwantedSubscriptions(name);

            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                optionallyAddServiceProviderToGroup(groupMemberNames,name);
            } else {
                if (groupMemberNames.contains(serviceProvider.getName())){
                    removeServiceProviderFromGroup(name,SERVICE_PROVIDERS_GROUP_NAME);
                }
            }

            setUpCapabilityExchanges(name);
            bindCapabilityExchangesToBiQueue(name);
            syncLocalSubscriptionsToServiceProviderCapabilities(name, serviceProviders);
            setUpDeliveryQueue(name);
        }
    }

    public void syncSubscriptions(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        //TODO: Check for subscriptions
        for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
            if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                processSubscription(serviceProvider, subscription, nodeProperties.getName(), nodeProperties.getMessageChannelPort());
            }
            else {
                processRedirectSubscription(subscription);
            }
        }
        repository.save(serviceProvider);
    }

    public void processSubscription(ServiceProvider serviceProvider, LocalSubscription subscription, String nodeName, String messageChannelPort) {
        switch (subscription.getStatus()) {
            case REQUESTED:
                if (subscription.getLocalEndpoints().isEmpty()) {
                    LocalEndpoint endpoint = new LocalEndpoint(UUID.randomUUID().toString(), nodeName, Integer.parseInt(messageChannelPort));
                    subscription.getLocalEndpoints().add(endpoint);
                }
                //NOTE fallthrough!
            case CREATED:
                onRequested(serviceProvider.getName(), subscription);
                break;
			case TEAR_DOWN:
                //	Check that the binding exist, if so, delete it
                onTearDown(serviceProvider, subscription);
                break;
            case ILLEGAL:
                // Remove the subscription from the ServiceProvider
                break;
                //needs testing.
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
    }

    private void onTearDown(ServiceProvider serviceProvider, LocalSubscription subscription) {
        Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            if (qpidClient.queueExists(source)) {
                qpidClient.removeReadAccess(serviceProvider.getName(), source);
                qpidClient.removeQueue(source);
                logger.info("Removed queue for LocalSubscription {}", subscription);
            }
            endpointsToRemove.add(endpoint);
        }
        if (!endpointsToRemove.isEmpty()) {
            subscription.getLocalEndpoints().removeAll(endpointsToRemove);
        }
        subscription.getConnections().clear();
    }

    public void removeUnwantedSubscriptions(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        //TODO: Check for subscriptions
        Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions();
        Set<LocalSubscription> subscriptionsToRemove = new HashSet<>();
        for (LocalSubscription localSubscription : localSubscriptions) {
            if (!localSubscription.isSubscriptionWanted()) {
                List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                if (matches.isEmpty() && localSubscription.getLocalEndpoints().isEmpty()) {
                    subscriptionsToRemove.add(localSubscription);
                }
            }
        }
        serviceProvider.removeSubscriptions(subscriptionsToRemove);
        repository.save(serviceProvider);
    }

    private void onRequested(String serviceProviderName, LocalSubscription subscription) {
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            optionallyCreateQueue(source, serviceProviderName);
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

    private void optionallyCreateQueue(String queueName, String serviceProviderName) {
        if (!qpidClient.queueExists(queueName)) {
            logger.info("Creating queue {}", queueName);
            qpidClient.createQueue(queueName);
            qpidClient.addReadAccess(serviceProviderName, queueName);
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

    public void syncPrivateChannels(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        Set<PrivateChannel> privateChannels = new HashSet<>();
        privateChannels.addAll(serviceProvider.getPrivateChannels());
        if(!privateChannels.isEmpty()) {
            syncPrivateChannelsWithQpid(privateChannels, serviceProviderName);
            Set<PrivateChannel> privateChannelsToRemove = privateChannels
                    .stream()
                    .filter(s -> s.getStatus().equals(PrivateChannelStatus.TEAR_DOWN))
                    .collect(Collectors.toSet());

            privateChannels.removeAll(privateChannelsToRemove);
            serviceProvider.setPrivateChannels(privateChannels);
            repository.save(serviceProvider);
        }
    }

    private void syncPrivateChannelsWithQpid(Set<PrivateChannel> privateChannels, String name) {
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
                qpidClient.addMemberToGroup(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                logger.debug("Adding member {} to group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                qpidClient.createQueue(queueName);
                logger.info("Creating queue {}", queueName);
                QpidAcl acl = qpidClient.getQpidAcl();
                acl.addQueueWriteAccess(name,queueName);
                acl.addQueueWriteAccess(peerName,queueName);
                acl.addQueueReadAccess(name, queueName);
                acl.addQueueReadAccess(peerName,queueName);
                qpidClient.postQpidAcl(acl);
                privateChannel.setStatus(PrivateChannelStatus.CREATED);
                logger.info("Creating queue {} for client {}", queueName, peerName);
            }
            if(privateChannel.getStatus().equals(PrivateChannelStatus.TEAR_DOWN)) {
                if(groupMemberNames.contains(name) && privateChannelsWithStatusCreated.isEmpty()){
                    qpidClient.removeMemberFromGroup(name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                    logger.debug("Removing member {} from group {}", name, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                }
                qpidClient.removeMemberFromGroup(peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                logger.info("Removing member {} from group {}", peerName, CLIENTS_PRIVATE_CHANNELS_GROUP_NAME);
                QpidAcl acl = qpidClient.getQpidAcl();
                acl.removeQueueWriteAccess(peerName,queueName);
                acl.removeQueueWriteAccess(name, queueName);
                acl.removeQueueReadAccess(peerName,queueName);
                acl.removeQueueReadAccess(name, queueName);
                qpidClient.postQpidAcl(acl);
                logger.info("Tearing down queue {} for client {}", queueName, peerName);
                qpidClient.removeQueue(queueName);
            }
        }
    }

    public void setUpCapabilityExchanges(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        if (serviceProvider.hasCapabilities()) {
            for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
                if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                    if (!capability.exchangeExists()) {
                        String exchangeName = "cap-" + UUID.randomUUID();
                        capability.setCapabilityExchangeName(exchangeName);
                        //repository.save(serviceProvider);
                    }
                    if (!qpidClient.exchangeExists(capability.getCapabilityExchangeName())) {
                        qpidClient.createTopicExchange(capability.getCapabilityExchangeName());
                        logger.info("Created exchange {} for Capability with id {}", capability.getCapabilityExchangeName(), capability.getId());
                    }
                }
            }
            repository.save(serviceProvider);
        }
    }

    public void bindCapabilityExchangesToBiQueue(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
            if (capability.exchangeExists()) {
                if (!qpidClient.getQueueBindKeys("bi-queue").contains(capability.getCapabilityExchangeName())) {
                    String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability);
                    qpidClient.bindToBiQueue(capabilitySelector, capability.getCapabilityExchangeName());
                }
            }
        }
    }

    public void tearDownCapabilityExchanges(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        Set<Capability> tearDownCapabilities = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(capability -> capability.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());
        //TODO: Check for capabilities
        for (Capability capability : tearDownCapabilities) {
            if (capability.exchangeExists()) {
                if (qpidClient.exchangeExists(capability.getCapabilityExchangeName())) {
                    qpidClient.removeExchange(capability.getCapabilityExchangeName());
                    logger.info("Removed exchange {} for Capability with id {}", capability.getCapabilityExchangeName(), capability.getId());
                }
                capability.setCapabilityExchangeName(""); //empty name to signal that there is no exchange present for this capability anymore
            }
        }
        repository.save(serviceProvider);
    }

    public void setUpDeliveryQueue(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        List<OutgoingMatch> deliveriesToSetupQueueFor = outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(serviceProvider.getName());
        //TODO: Check for outgoingMatches
        for (OutgoingMatch match : deliveriesToSetupQueueFor) {
            if (match.getCapability().getStatus().equals(CapabilityStatus.CREATED)) {
                LocalDelivery delivery = match.getLocalDelivery();
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                    if (!qpidClient.exchangeExists(delivery.getExchangeName())) {
                        String joinedSelector = joinDeliverySelectorWithCapabilitySelector(match.getCapability(), delivery.getSelector());
                        qpidClient.createDirectExchange(delivery.getExchangeName());
                        qpidClient.addWriteAccess(serviceProvider.getName(), delivery.getExchangeName());
                        qpidClient.bindDirectExchange(joinedSelector, delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName());
                        outgoingMatchDiscoveryService.updateOutgoingMatchToUp(match);
                    } else {
                        String joinedSelector = joinDeliverySelectorWithCapabilitySelector(match.getCapability(), delivery.getSelector());
                        qpidClient.bindDirectExchange(joinedSelector, delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName());
                        outgoingMatchDiscoveryService.updateOutgoingMatchToUp(match);
                    }
                }
            }
        }
        repository.save(serviceProvider);
    }

    public void tearDownDeliveryQueues(String serviceProviderName) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        //TODO: Check for deliveries
        List<OutgoingMatch> matchesToTearDownEndpointsFor = outgoingMatchDiscoveryService.findMatchesToTearDownEndpointsFor(serviceProvider.getName());
        for (OutgoingMatch match : matchesToTearDownEndpointsFor) {
            if (match.getLocalDelivery().getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)) {
                if (match.getLocalDelivery().exchangeExists()) {
                    String target = match.getLocalDelivery().getExchangeName();
                    if (qpidClient.exchangeExists(target)) {
                        logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                        qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                        qpidClient.removeExchange(target);
                    }
                    match.getLocalDelivery().setExchangeName("");
                }
            }
            outgoingMatchDiscoveryService.updateOutgoingMatchToDeleted(match);
        }

        //Have to do something here, think that the delivery exchange name will change in restart
        Set<LocalDelivery> deliveries = serviceProvider.getDeliveries();
        for (LocalDelivery delivery : deliveries) {
            if (!delivery.getStatus().equals(LocalDeliveryStatus.ILLEGAL)
                    && !delivery.getStatus().equals(LocalDeliveryStatus.REQUESTED)
                    && !delivery.getStatus().equals(LocalDeliveryStatus.TEAR_DOWN)) {
                List<OutgoingMatch> matches = outgoingMatchDiscoveryService.findMatchesFromDeliveryId(delivery.getId());
                if (matches.isEmpty()) {
                    if (delivery.exchangeExists()) {
                        String target = delivery.getExchangeName();
                        if (qpidClient.exchangeExists(target)) {
                            logger.info("Removing endpoint with name {} for service provider {}", target, serviceProvider.getName());
                            qpidClient.removeWriteAccess(serviceProvider.getName(), target);
                            qpidClient.removeExchange(target);
                        }
                        delivery.setExchangeName("");
                    }
                    delivery.setStatus(LocalDeliveryStatus.NO_OVERLAP);
                }
            }
        }
        repository.save(serviceProvider);
    }

    public String joinDeliverySelectorWithCapabilitySelector(Capability capability, String selector) {
        return MessageValidatingSelectorCreator.makeSelectorJoinedWithCapabilitySelector(selector,capability);
    }

    @Scheduled(fixedRateString = "${create-bindings-subscriptions-exchange.interval}")
    public void createBindingsWithMatches() {
        List<ServiceProvider> serviceProviders = repository.findAll();
        for (ServiceProvider serviceProvider : serviceProviders) {
            for (LocalSubscription localSubscription : serviceProvider.getSubscriptions()) {
                if (localSubscription.isSubscriptionWanted() && !localSubscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                    if (!localSubscription.getLocalEndpoints().isEmpty()) {
                        List<Match> matches = matchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
                        for (Match match : matches) {
                            if (match.getSubscription().isSubscriptionWanted() && match.getSubscription().exchangeIsCreated()) {
                                for (String queueName : localSubscription.getLocalEndpoints().stream().map(LocalEndpoint::getSource).collect(Collectors.toSet())) {
                                    String exchangeName = match.getSubscription().getExchangeName();
                                    if (!qpidClient.getQueueBindKeys(queueName).contains(qpidClient.createBindKey(exchangeName, queueName))) {
                                        bindQueueToSubscriptionExchange(queueName, exchangeName, localSubscription);
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
        qpidClient.bindSubscriptionExchange(localSubscription.getSelector(), exchangeName, queueName);
    }

    public void syncLocalSubscriptionsToServiceProviderCapabilities(String serviceProviderName, Iterable<ServiceProvider> serviceProviders) {
        ServiceProvider serviceProvider = repository.findByName(serviceProviderName);
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<Capability> allCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : serviceProviderSubscriptions) {
                if (!serviceProvider.getName().equals(subscription.getConsumerCommonName())) {
                    removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCapabilities);
                    if (!allCapabilities.isEmpty()) {
                        if (!subscription.getLocalEndpoints().isEmpty()) {
                            Set<String> existingConnections = subscription.getConnections().stream()
                                    .map(LocalConnection::getSource)
                                    .collect(Collectors.toSet());

                            Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, subscription.getSelector());

                            for (Capability capability : matchingCapabilities) {
                                if (capability.exchangeExists() && !existingConnections.contains(capability.getCapabilityExchangeName())) {
                                    if (qpidClient.exchangeExists(capability.getCapabilityExchangeName())) {
                                        LocalEndpoint endpoint = subscription.getLocalEndpoints().stream().findFirst().get();
                                        qpidClient.bindTopicExchange(subscription.getSelector(), capability.getCapabilityExchangeName(), endpoint.getSource());
                                        LocalConnection connection = new LocalConnection(capability.getCapabilityExchangeName(), endpoint.getSource());
                                        subscription.addConnection(connection);
                                    }
                                }
                            }
                        }
                    }
                    repository.save(serviceProvider);
                }
            }
        }
    }

    public void removeUnusedLocalConnectionsFromLocalSubscription(LocalSubscription subscription, Set<Capability> capabilities) {
        Set<String> existingConnections = capabilities.stream()
                .map(Capability::getCapabilityExchangeName)
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
