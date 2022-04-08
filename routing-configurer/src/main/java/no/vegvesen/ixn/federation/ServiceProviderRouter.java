package no.vegvesen.ixn.federation;

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
            Set<LocalSubscription> newSubscriptions = new HashSet<>();
            for (LocalSubscription subscription : serviceProvider.getSubscriptions()) {

                Optional<LocalSubscription> newSubscription = processSubscription(name, subscription,nodeProperties.getName(),nodeProperties.getMessageChannelPort());
                newSubscription.ifPresent(newSubscriptions::add);
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

            setUpSubscriptionExchanges(name);
            setUpDeliveryQueue(name, nodeProperties.getName(), nodeProperties.getMessageChannelPort());


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
                    LocalEndpoint endpoint = new LocalEndpoint(UUID.randomUUID().toString(),nodeName,Integer.parseInt(messageChannelPort));
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
            removeBindingIfExists(source, subscription);
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
            optionallyAddQueueBindings(source, subscription.getSelector(), subscription.bindKey());
        }
        return Optional.of(subscription.withStatus(LocalSubscriptionStatus.CREATED));
    }

    private void removeBindingIfExists(String queueName, LocalSubscription subscription) {
        if (qpidClient.queueExists(queueName)) {
            if (qpidClient.getQueueBindKeys(queueName).contains(subscription.bindKey())) {
                qpidClient.unbindBindKey(queueName, subscription.bindKey(), "outgoingExchange");
            }
        }
    }

    //TODO this should probably be removed
    private void optionallyAddQueueBindings(String queueName, String selector, String bindKey) {
        if (!qpidClient.getQueueBindKeys(queueName).contains(bindKey)) {
            logger.debug("Adding bindings to the queue {}", queueName);
            qpidClient.bindTopicExchange(selector, "outgoingExchange", queueName);
        }
    }

    private void optionallyCreateQueue(String queueName, String serviceProviderName) {
        if (!qpidClient.queueExists(queueName)) {
            logger.info("Creating queue {}", queueName);
            qpidClient.createQueue(queueName);
            qpidClient.addReadAccess(serviceProviderName, queueName);
        }
    }

    private void optionallyCreateWriteQueue(String queueName, String serviceProviderName) {
        if (!qpidClient.queueExists(queueName)) {
            logger.info("Creating queue {}", queueName);
            qpidClient.createQueue(queueName);
            qpidClient.addWriteAccess(serviceProviderName, queueName);
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

    public void setUpSubscriptionExchanges(String serviceProviderName) {
        List<Match> matches = matchDiscoveryService.findMatchesToSetupExchangesFor(serviceProviderName);
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
                qpidClient.removeDirectExchange(exchangeName);
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

    public void setUpDeliveryQueue(String serviceProviderName, String nodeName, String port) {
        List<OutgoingMatch> deliveriesToSetupQueueFor = outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(serviceProviderName);
        for (OutgoingMatch match : deliveriesToSetupQueueFor) {
            LocalDelivery delivery = match.getLocalDelivery();
            updateDeliveryWithEndpoint(match.getCapability(), delivery, nodeName, Integer.parseInt(port));
            for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
                qpidClient.createDirectExchange(endpoint.getTarget());
                qpidClient.addWriteAccess(serviceProviderName, endpoint.getTarget());
                qpidClient.bindDirectExchange(endpoint.getSelector(), endpoint.getTarget(), "outgoingExchange");
            }
            outgoingMatchDiscoveryService.updateOutgoingMatchToUp(match);
        }
    }

    public void removeDeliveryQueueByCapability(Integer capabilityId) {
        List<OutgoingMatch> matches = outgoingMatchDiscoveryService.findMatchesFromCapabilityId(capabilityId);
        for (OutgoingMatch match : matches) {
            LocalDelivery delivery = match.getLocalDelivery();
            removeDeliveryQueue(match.getServiceProviderName(), delivery);
            delivery.removeAllEndpoints(delivery.getEndpoints());
        }
        outgoingMatchDiscoveryService.removeListOfOutgoingMatches(matches, capabilityId);
    }

    public void removeDeliveryQueueByDelivery(Integer deliveryId) {
        OutgoingMatch match = outgoingMatchDiscoveryService.findMatchFromDeliveryId(deliveryId);
        LocalDelivery delivery = match.getLocalDelivery();
        removeDeliveryQueue(match.getServiceProviderName(), delivery);
        outgoingMatchDiscoveryService.removeOutgoingMatch(match);
    }

    public void removeDeliveryQueue(String serviceProviderName, LocalDelivery delivery) {
        for (LocalDeliveryEndpoint endpoint : delivery.getEndpoints()) {
            String target = endpoint.getTarget();
            if (qpidClient.queueExists(target)) {
                if (qpidClient.getQueueBindKeys(target).contains(endpoint.bindKey())) {
                    qpidClient.unbindBindKey(target, endpoint.bindKey(), "outgoingExchange");
                }
                qpidClient.removeWriteAccess(serviceProviderName, target);
                qpidClient.removeQueue(target);
            }
        }
    }

    public void updateDeliveryWithEndpoint(Capability capability, LocalDelivery delivery, String nodeName, Integer port) {
        String selector = joinDeliverySelectorWitCapabilitySelector(capability, delivery.getSelector());
        String target = UUID.randomUUID().toString();
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(nodeName, port, target, selector);
        delivery.setEndpoints(new HashSet<>(Arrays.asList(endpoint)));
    }

    public String joinDeliverySelectorWitCapabilitySelector(Capability capability, String selector) {
        String capabilitySelector = MessageValidatingSelectorCreator.makeSelector(capability);
        String joinedSelector = String.format("(%s) AND (%s)", capabilitySelector, selector);
        return joinedSelector;
    }

    private void createSubscriptionExchange(String exchangeName) {
        qpidClient.createTopicExchange(exchangeName);
    }

    private void bindQueueToSubscriptionExchange(String queueName, String exchangeName, LocalSubscription localSubscription) {
        logger.debug("Adding bindings from queue {} to exchange {}", queueName, exchangeName);
        qpidClient.bindTopicExchange(localSubscription.getSelector(), exchangeName, queueName);
    }
}
