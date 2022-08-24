package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidAcl;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME;

@Component
public class ServiceProviderRouter {

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouter.class);

    private final ServiceProviderRepository repository;
    private final QpidClient qpidClient;
    private final MatchDiscoveryService matchDiscoveryService;
    private final OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;
    private final InterchangeNodeProperties nodeProperties;

    @Autowired
    public ServiceProviderRouter(ServiceProviderRepository repository, QpidClient qpidClient, MatchDiscoveryService matchDiscoveryService, OutgoingMatchDiscoveryService outgoingMatchDiscoveryService, InterchangeNodeProperties nodeProperties) {
        this.repository = repository;
        this.qpidClient = qpidClient;
        this.matchDiscoveryService = matchDiscoveryService;
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
            syncPrivateChannels(serviceProvider);
            tearDownSubscriptionExchanges(name);
            tearDownDeliveryQueues(serviceProvider);
            tearDownCapabilityExchanges(serviceProvider);
            Set<LocalSubscription> newSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {
                if (!subscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                    Optional<LocalSubscription> newSubscription = processSubscription(name, subscription, nodeProperties.getName(), nodeProperties.getMessageChannelPort());
                    newSubscription.ifPresent(newSubscriptions::add);
                }
            }


            if (serviceProvider.hasCapabilitiesOrActiveSubscriptions()) {
                if (serviceProvider.hasCapabilities() || (! newSubscriptions.isEmpty())) {
                    optionallyAddServiceProviderToGroup(groupMemberNames,name);
                }
            } else {
                if (groupMemberNames.contains(serviceProvider.getName())){
                    removeServiceProviderFromGroup(name,SERVICE_PROVIDERS_GROUP_NAME);
                }
            }
            setUpCapabilityExchanges(serviceProvider);
            syncLocalSubscriptionsToServiceProviderCapabilities(serviceProvider, StreamSupport.stream(serviceProviders.spliterator(), false)
                    .collect(Collectors.toSet()));
            setUpSubscriptionExchanges(serviceProvider);
            setUpDeliveryQueue(serviceProvider);

            //save if it has changed from the initial
            if (! newSubscriptions.equals(serviceProvider.getSubscriptions())) {
                serviceProvider.updateSubscriptions(newSubscriptions);
                repository.save(serviceProvider);
                logger.debug("Saved updated service provider {}",serviceProvider);
            } else {
                logger.debug("Service provider not changed, {}", serviceProvider);
            }
        }
    }

    public Optional<LocalSubscription> processSubscription(String serviceProviderName, LocalSubscription subscription, String nodeName, String messageChannelPort) {
        Optional<LocalSubscription> newSubscription;
        switch (subscription.getStatus()) {
            case REQUESTED:
                if (subscription.getLocalEndpoints().isEmpty()) {
                    LocalEndpoint endpoint = new LocalEndpoint(UUID.randomUUID().toString(), nodeName, Integer.parseInt(messageChannelPort));
                    subscription.getLocalEndpoints().add(endpoint);
                }
                //NOTE fallthrough!
            case CREATED:
                newSubscription = onRequested(serviceProviderName, subscription);
                break;
			case TEAR_DOWN:
                //	Check that the binding exist, if so, delete it
                newSubscription = onTearDown(serviceProviderName, subscription);
                break;
            default:
                throw new IllegalStateException("Unknown subscription status encountered");
        }
        return newSubscription;
    }

    private Optional<LocalSubscription> onTearDown(String serviceProviderName, LocalSubscription subscription) {
        List<Match> match = matchDiscoveryService.findMatchByLocalSubscriptionId(subscription.getId());
        Set<LocalEndpoint> endpointsToRemove = new HashSet<>();
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            if (match.isEmpty()) {
                if (qpidClient.queueExists(source)) {
                    qpidClient.removeReadAccess(serviceProviderName, source);
                    qpidClient.removeQueue(source);
                    logger.info("Removed queue for LocalSubscription {}", subscription);
                }
            }
            endpointsToRemove.add(endpoint);
        }
        subscription.getLocalEndpoints().removeAll(endpointsToRemove);
        if (match.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(subscription);
        }
    }

    private Optional<LocalSubscription> onRequested(String serviceProviderName, LocalSubscription subscription) {
        for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
            String source = endpoint.getSource();
            optionallyCreateQueue(source, serviceProviderName);
        }
        return Optional.of(subscription.withStatus(LocalSubscriptionStatus.CREATED));
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

    public void syncPrivateChannels(ServiceProvider serviceProvider) {
        Set<PrivateChannel> privateChannels = serviceProvider.getPrivateChannels();
        String name = serviceProvider.getName();
        if(!privateChannels.isEmpty()) {
            syncPrivateChannelsWithQpid(privateChannels, name);
            Set<PrivateChannel> privateChannelsToRemove = privateChannels
                    .stream()
                    .filter(s -> s.getStatus().equals(PrivateChannelStatus.TEAR_DOWN))
                    .collect(Collectors.toSet());

            privateChannels.removeAll(privateChannelsToRemove);
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

    public void setUpSubscriptionExchanges(ServiceProvider serviceProvider) {
        List<Match> matches = matchDiscoveryService.findMatchesToSetupExchangesFor(serviceProvider.getName());
        for (Match match : matches) {
            String exchangeName = match.getSubscription().getExchangeName();
            createSubscriptionExchange(exchangeName);
            for (LocalEndpoint endpoint : match.getLocalSubscription().getLocalEndpoints()) {
                bindQueueToSubscriptionExchange(endpoint.getSource(), exchangeName, match.getLocalSubscription());
            }
            matchDiscoveryService.updateMatchToSetupEndpoint(match);
        }
    }

    public void tearDownSubscriptionExchanges(String serviceProviderName) {
        List<Match> matches = matchDiscoveryService.findMatchesToTearDownExchangesFor(serviceProviderName);
        for (Match match : matches) {
            String exchangeName = match.getSubscription().getExchangeName();
            String bindKey = match.getLocalSubscription().bindKey();
            if (qpidClient.exchangeExists(exchangeName)) {
                for (LocalEndpoint endpoint : match.getLocalSubscription().getLocalEndpoints()) {
                    if (qpidClient.getQueueBindKeys(endpoint.getSource()).contains(bindKey)) {
                        qpidClient.unbindBindKey(endpoint.getSource(), bindKey, exchangeName);
                    }
                }
                qpidClient.removeExchange(exchangeName);
            }
            removeLocalSubscriptionQueue(match.getLocalSubscription(), serviceProviderName);
            matchDiscoveryService.updateMatchToDeleted(match);
        }
    }

    public void removeLocalSubscriptionQueue(LocalSubscription subscription, String serviceProviderName) {
        if (subscription.getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN)) {
            for (LocalEndpoint endpoint : subscription.getLocalEndpoints()) {
                String queueName = endpoint.getSource();
                if (qpidClient.queueExists(queueName)) {
                    qpidClient.removeReadAccess(serviceProviderName, queueName);
                    qpidClient.removeQueue(queueName);
                    logger.info("Removed queue for LocalSubscription {}", subscription);
                }
            }
        }
    }

    public void setUpCapabilityExchanges(ServiceProvider serviceProvider) {
        if (serviceProvider.hasCapabilities()) {
            for (Capability capability : serviceProvider.getCapabilities().getCapabilities()) {
                if (capability.getStatus().equals(CapabilityStatus.CREATED)) {
                    if (!capability.exchangeExists()) {
                        String exchangeName = "cap-" + UUID.randomUUID();
                        if (!qpidClient.exchangeExists(exchangeName)) {
                            qpidClient.createTopicExchange(exchangeName);
                            capability.setCapabilityExchangeName(exchangeName);
                            logger.info("Created exchange {} for Capability with id {}", exchangeName, capability.getId());
                        }
                    }
                }
            }
            repository.save(serviceProvider);
        }
    }

    public void tearDownCapabilityExchanges(ServiceProvider serviceProvider) {
        Set<Capability> tearDownCapabilities = serviceProvider.getCapabilities().getCapabilities().stream()
                .filter(capability -> capability.getStatus().equals(CapabilityStatus.TEAR_DOWN))
                .collect(Collectors.toSet());

        for (Capability capability : tearDownCapabilities) {
            if (capability.exchangeExists()) {
                qpidClient.removeExchange(capability.getCapabilityExchangeName());
                capability.setCapabilityExchangeName(""); //empty name to signal that there is no exchange present for this capability anymore
                logger.info("Removed exchange {} for Capability with id {}", capability.getCapabilityExchangeName(), capability.getId());
            }
        }
        repository.save(serviceProvider);
    }

    public void setUpDeliveryQueue(ServiceProvider serviceProvider) {
        List<OutgoingMatch> deliveriesToSetupQueueFor = outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(serviceProvider.getName());
        for (OutgoingMatch match : deliveriesToSetupQueueFor) {
            if (match.getCapability().getStatus().equals(CapabilityStatus.CREATED)) {
                LocalDelivery delivery = match.getLocalDelivery();
                if (delivery.getStatus().equals(LocalDeliveryStatus.CREATED)) {
                    if (!qpidClient.exchangeExists(delivery.getExchangeName())) {
                        String joinedSelector = joinDeliverySelectorWithCapabilitySelector(match.getCapability(), delivery.getSelector());
                        //match.setSelector(joinedSelector);
                        qpidClient.createDirectExchange(delivery.getExchangeName());
                        qpidClient.addWriteAccess(serviceProvider.getName(), delivery.getExchangeName());
                        qpidClient.bindDirectExchange(joinedSelector, delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName());
                        outgoingMatchDiscoveryService.updateOutgoingMatchToUp(match);
                    } else {
                        String joinedSelector = joinDeliverySelectorWithCapabilitySelector(match.getCapability(), delivery.getSelector());
                        //match.setSelector(joinedSelector);
                        qpidClient.bindDirectExchange(joinedSelector, delivery.getExchangeName(), match.getCapability().getCapabilityExchangeName());
                        outgoingMatchDiscoveryService.updateOutgoingMatchToUp(match);
                    }
                }
            }
        }
    }

    public void tearDownDeliveryQueues(ServiceProvider serviceProvider) {
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

        Set<LocalDelivery> deliveries = serviceProvider.getDeliveries();
        for (LocalDelivery delivery : deliveries) {
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

    public String joinDeliverySelectorWithCapabilitySelector(Capability capability, String selector) {
        return MessageValidatingSelectorCreator.makeSelectorJoinedWithCapabilitySelector(selector,capability);
    }

    private void createSubscriptionExchange(String exchangeName) {
        qpidClient.createTopicExchange(exchangeName);
    }

    private void bindQueueToSubscriptionExchange(String queueName, String exchangeName, LocalSubscription localSubscription) {
        logger.debug("Adding bindings from queue {} to exchange {}", queueName, exchangeName);
        qpidClient.bindTopicExchange(localSubscription.getSelector(), exchangeName, queueName);
    }

    public void syncLocalSubscriptionsToServiceProviderCapabilities(ServiceProvider serviceProvider, Set<ServiceProvider> serviceProviders) {
        if (serviceProvider.hasActiveSubscriptions()) {
            Set<Capability> allCapabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider.activeSubscriptions();
            for (LocalSubscription subscription : serviceProviderSubscriptions) {
                if (!subscription.getConsumerCommonName().equals(serviceProvider.getName())) {
                    removeUnusedLocalConnectionsFromLocalSubscription(subscription, allCapabilities);
                    if (!allCapabilities.isEmpty()) {
                        if (!subscription.getLocalEndpoints().isEmpty()) {
                            Set<String> existingConnections = subscription.getConnections().stream()
                                    .map(LocalConnection::getSource)
                                    .collect(Collectors.toSet());

                            Set<Capability> matchingCapabilities = CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, subscription.getSelector());

                            for (Capability capability : matchingCapabilities) {
                                if (capability.exchangeExists() && !existingConnections.contains(capability.getCapabilityExchangeName())) {
                                    LocalEndpoint endpoint = subscription.getLocalEndpoints().stream().findFirst().get();
                                    qpidClient.bindTopicExchange(subscription.getSelector(), capability.getCapabilityExchangeName(), endpoint.getSource());
                                    LocalConnection connection = new LocalConnection(capability.getCapabilityExchangeName(), endpoint.getSource());
                                    subscription.addConnection(connection);
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
