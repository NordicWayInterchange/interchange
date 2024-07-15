package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.subscription.SubscriptionCalculator;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NeigbourDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NeigbourDiscoveryService.class);

    private final DNSFacade dnsFacade;
    private final NeighbourRepository neighbourRepository;
    private final ListenerEndpointRepository listenerEndpointRepository;
    private final InterchangeNodeProperties interchangeNodeProperties;
    private final GracefulBackoffProperties backoffProperties;
    private final NeighbourDiscovererProperties discovererProperties;


    @Autowired
    public NeigbourDiscoveryService(DNSFacade dnsFacade,
                                    NeighbourRepository neighbourRepository,
                                    ListenerEndpointRepository listenerEndpointRepository,
                                    InterchangeNodeProperties interchangeNodeProperties,
                                    GracefulBackoffProperties backoffProperties,
                                    NeighbourDiscovererProperties discovererProperties) {
        this.dnsFacade = dnsFacade;
        this.neighbourRepository = neighbourRepository;
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.interchangeNodeProperties = interchangeNodeProperties;
        this.backoffProperties = backoffProperties;
        this.discovererProperties = discovererProperties;
    }
    public void checkForNewNeighbours() {
        logger.debug("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
        List<Neighbour> neighbours = dnsFacade.lookupNeighbours();
        logger.debug("Got neighbours from DNS {}.", neighbours);

        for (Neighbour neighbour : neighbours) {
            String neighbourName = neighbour.getName();
            NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbourName);
            if (neighbourRepository.findByName(neighbourName) == null && !neighbourName.equals(interchangeNodeProperties.getName())) {

                // Found a new Neighbour. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
                neighbour.getCapabilities().setStatus(CapabilitiesStatus.UNKNOWN);
                logger.info("Found new neighbour {}. Saving in database",neighbourName);
                neighbourRepository.save(neighbour);
            }
            NeighbourMDCUtil.removeLogVariables();
        }
    }

    public void capabilityExchangeWithNeighbours(NeighbourFacade neighbourFacade, Set<Capability> localCapabilities, Optional<LocalDateTime> lastUpdatedLocalCapabilities) {
        List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_StatusIn(
                CapabilitiesStatus.UNKNOWN,
                CapabilitiesStatus.KNOWN,
                CapabilitiesStatus.FAILED);
        capabilityExchange(neighboursForCapabilityExchange, neighbourFacade, localCapabilities, lastUpdatedLocalCapabilities);
    }

    void capabilityExchange(List<Neighbour> neighboursForCapabilityExchange, NeighbourFacade neighbourFacade, Set<Capability> localCapabilities, Optional<LocalDateTime> lastUpdatedLocalCapabilities) {
        for (Neighbour neighbour : neighboursForCapabilityExchange) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    if (neighbour.needsOurUpdatedCapabilities(lastUpdatedLocalCapabilities)) {
                        logger.info("Posting capabilities to neighbour: {} ", neighbour.getName());
                        postCapabilities(neighbour, neighbourFacade, interchangeNodeProperties.getName(), localCapabilities);
                    } else {
                        logger.debug("Neighbour has our last capabilities");
                    }
                } else {
                    logger.info("Too soon to post capabilities to neighbour when backing off");
                }
            } catch (Exception e) {
                logger.error("Unknown exception while exchanging capabilities with neighbour", e);
            } finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    public void retryUnreachable(NeighbourFacade neighbourFacade, Set<Capability> localCapabilities) {
        List<Neighbour> unreachableNeighbours = neighbourRepository.findByControlConnection_ConnectionStatus(ConnectionStatus.UNREACHABLE);
        if (!unreachableNeighbours.isEmpty()) {
            logger.debug("Retrying connection to unreachable neighbours {}", unreachableNeighbours.stream().map(Neighbour::getName).collect(Collectors.toList()));
            for (Neighbour neighbour : unreachableNeighbours) {
                try {
                    NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                    logger.info("Retrying capability post to neighbour {}", neighbour.getName());
                    postCapabilities(neighbour, neighbourFacade, interchangeNodeProperties.getName(), localCapabilities);
                } catch (Exception e) {
                    logger.error("Error occurred while posting capabilities to unreachable neighbour", e);
                } finally {
                    NeighbourMDCUtil.removeLogVariables();
                }
            }
        }
    }

    private void postCapabilities(Neighbour neighbour, NeighbourFacade neighbourFacade, String selfName, Set<Capability> localCapabilities) {
        try {
            Set<NeighbourCapability> capabilities = neighbourFacade.postCapabilitiesToCapabilities(neighbour, selfName, localCapabilities);
            NeighbourCapabilities neighbourCapabilities = neighbour.getCapabilities();
            neighbourCapabilities.setStatus(CapabilitiesStatus.KNOWN);
            neighbourCapabilities.replaceCapabilities(capabilities);
            neighbourCapabilities.setLastCapabilityExchange(LocalDateTime.now());
            neighbour.getControlConnection().okConnection();
            logger.debug("Updated neighbour: {}", neighbour);
        } catch (CapabilityPostException e) {
            logger.error("Capability post failed", e);
            neighbour.getCapabilities().setStatus(CapabilitiesStatus.FAILED);
            neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
        } finally {
            neighbour = neighbourRepository.save(neighbour);
            logger.info("Saving updated neighbour: {}", neighbour.getName());
        }
    }

    public void evaluateAndPostSubscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest, Optional<LocalDateTime> lastUpdatedLocalSubscriptions, Set<LocalSubscription> localSubscriptions, NeighbourFacade neighbourFacade) {

        for (Neighbour neighbour : neighboursForSubscriptionRequest) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.hasCapabilities()) {
                    if (neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedLocalSubscriptions)) {
                        logger.info("Posting subscription request to neighbour {}", neighbour.getName());
                        postSubscriptionRequest(neighbour, localSubscriptions, neighbourFacade);
                    } else {
                        logger.debug("No need to calculateCustomSubscriptionForNeighbour based on timestamps on local subscriptions, neighbour capabilities, last subscription request");
                    }
                } else {
                    logger.warn("Neighbour has neither capabilities nor subscriptions");
                }
            } catch (Exception e) {
                logger.error("Exception when evaluating subscriptions for neighbour", e);
            }
            finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    //1. 1 LocalSubscription, matching several neighbour capabilities, thus making a 1-to-n relationship LocalSubscription -> Subscription
                //There will only be one Subscription for the Neighbour, even though we might match several capabilities on the neighbour.
                //TODO will it only be one Subscription for the Neighbour?
                //So, this is really a 1-to-1 relationship.
    //2. N LocalSubscriptions, matching 1 neighbour capability, thus making a n-to-1 relationship LocalSubscription -> Subscription
    //3. N LocalSubscriptions, each matching the same capability, thus making a n-to-n relationship LocalSubscription -> Subscription
                //There will only be one Subscription for the Neighbour, even though we might match several capabilities on the neighbour.
                //So, this is really a n-to-1 relationship.
    public void postSubscriptionRequest(Neighbour neighbour, Set<LocalSubscription> localSubscriptions, NeighbourFacade neighbourFacade) {
        String neighbourName = neighbour.getName();
        Set<NeighbourCapability> neighbourCapabilities = neighbour.getCapabilities().getCapabilities();
        SubscriptionRequest ourRequestedSubscriptionsFromNeighbour = neighbour.getOurRequestedSubscriptions();
        Set<Subscription> wantedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(localSubscriptions, Capability.transformNeighbourCapabilityToCapability(neighbourCapabilities), interchangeNodeProperties.getName());
        Set<Subscription> existingSubscriptions = ourRequestedSubscriptionsFromNeighbour.getSubscriptions();
        SubscriptionPostCalculator subscriptionPostCalculator = new SubscriptionPostCalculator(existingSubscriptions,wantedSubscriptions);
        if (!wantedSubscriptions.equals(existingSubscriptions)) {
            for (Subscription subscription : subscriptionPostCalculator.getSubscriptionsToRemove()) {
                if (!subscription.getEndpoints().isEmpty()) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbourName, subscription.getEndpoints());
                    }
                }
                subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
            }
            Connection controlConnection = neighbour.getControlConnection();
            try {
                if (controlConnection.canBeContacted(backoffProperties)) {
                    Set<Subscription> additionalSubscriptions = subscriptionPostCalculator.getNewSubscriptions();
                    if (!additionalSubscriptions.isEmpty()) {
                        Set<Subscription> responseSubscriptions = neighbourFacade.postSubscriptionRequest(neighbour, additionalSubscriptions, interchangeNodeProperties.getName());
                        //TODO: Should we save subscriptions that haa status NO_OVERLAP or ILLEGAL??
                        ourRequestedSubscriptionsFromNeighbour.addNewSubscriptions(responseSubscriptions);
                        ourRequestedSubscriptionsFromNeighbour.setSuccessfulRequest(LocalDateTime.now());
                        //TODO we never changed the subscription status. We don't now either. Is that OK?

                    }
                    controlConnection.okConnection();
                    logger.info("Successfully posted subscription request to {}", neighbourName);
                } else {
                    logger.info("Too soon to post subscription request to neighbour {} when backing off",neighbourName);
                }
            } catch (SubscriptionRequestException e) {
                controlConnection.failedConnection(backoffProperties.getNumberOfAttempts());
                logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.", e);
            }
            // Successful subscription request, update discovery state subscription request timestamp.
            neighbourRepository.save(neighbour);
        } else {
            logger.info("Neighbour {} has our last subscription request", neighbourName);
        }
    }

    public void pollSubscriptions(NeighbourFacade neighbourFacade) {
        List<Neighbour> neighboursToPoll = neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
                SubscriptionStatus.REQUESTED,
                SubscriptionStatus.FAILED);
        for (Neighbour neighbour : neighboursToPoll) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    pollSubscriptionsOneNeighbour(neighbour, neighbourFacade);
                }
                //TODO find a better solution to this
            } catch (Exception e) {
                logger.error("Unknown error while polling subscriptions for one neighbour",e);
            }
            finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    public void pollSubscriptionsWithStatusCreated(NeighbourFacade neighbourFacade) {
        List<Neighbour> neighboursToPoll = neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
                SubscriptionStatus.CREATED);
        for (Neighbour neighbour : neighboursToPoll) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour, neighbourFacade);
                }
            } catch (Exception e) {
                logger.error("Unknown error while polling subscription with status CREATED",e);
            }
            finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    public void pollSubscriptionsOneNeighbour(Neighbour neighbour, NeighbourFacade neighbourFacade) {
        try {
            Set<Subscription> subscriptionsForPolling = neighbour.getSubscriptionsForPolling();
            for (Subscription subscription : subscriptionsForPolling) {
                try {
                    if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
                        logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

                        // Throws SubscriptionPollException if unsuccessful
                        //Or SubscriptionNotFoundException if not found at neighbour
                        Subscription polledSubscription = neighbourFacade.pollSubscriptionStatus(subscription, neighbour);
                        subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
                        subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
                        subscription.setConsumerCommonName(polledSubscription.getConsumerCommonName());
                        subscription.setEndpoints(polledSubscription.getEndpoints());
                        subscription.setLastUpdatedTimestamp(polledSubscription.getLastUpdatedTimestamp());
                        neighbour.getControlConnection().okConnection();
                        if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                            logger.debug("Subscription for neighbour {} with path {} is CREATED", neighbour.getName(), subscription.getPath());
                        }
                        logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());
                    } else {
                        // Number of poll attempts exceeds allowed number of poll attempts.
                        subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                        logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
                    }
                } catch (SubscriptionPollException e) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                    }
                    subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
                    subscription.incrementNumberOfPolls();
                    neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                    logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.", e);
                } catch (SubscriptionNotFoundException e) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                    }
                    subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
                    logger.error("Subscription {} is gone from neighbour", subscription,e);
                }
            }
        } finally {
            logger.info("Saving updated neighbour: {}", neighbour.getName());
            neighbourRepository.save(neighbour);
        }
    }

    public void pollSubscriptionsWithStatusCreatedOneNeighbour(Neighbour neighbour, NeighbourFacade neighbourFacade) {
        try {
            Set<Subscription> createdSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.CREATED);
            for (Subscription subscription : createdSubscriptions) {
                try {
                    if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
                        Subscription lastUpdatedSubscription = neighbourFacade.pollSubscriptionStatus(subscription, neighbour);
                        if (lastUpdatedSubscription.getSubscriptionStatus().equals(SubscriptionStatus.RESUBSCRIBE)) {
                            if (lastUpdatedSubscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                                tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                            }
                            subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
                        } else {
                            if (!subscription.getEndpoints().equals(lastUpdatedSubscription.getEndpoints())) {
                                logger.info("Polled updated subscription with id {}", subscription.getId());
                                if (lastUpdatedSubscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                                    tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                                }
                                subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
                            } else {
                                logger.info("No subscription change for neighbour {}", neighbour.getName());
                            }
                        }
                    } else {
                        subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                        logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
                    }
                } catch (SubscriptionPollException e) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                    }
                    subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
                    neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                    logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.", e);
                } catch (SubscriptionNotFoundException e) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                    }
                    subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
                    logger.error("Subscription {} is gone from neighbour {}", subscription,neighbour.getName());
                }
            }
        }
        finally {
            logger.info("Saving updated neighbour: {}", neighbour.getName());
            neighbourRepository.save(neighbour);
        }
    }

    public void setGiveUpSubscriptionsToTearDownForRemoval(){
        List<Neighbour> neighbours = neighbourRepository.findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.GIVE_UP);
        for (Neighbour neighbour : neighbours) {
            for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.GIVE_UP)) {
                if (!subscription.getEndpoints().isEmpty()) {
                    if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                        tearDownListenerEndpointsFromEndpointsList(neighbour.getName(), subscription.getEndpoints());
                    }
                }
                subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
            }
            neighbourRepository.save(neighbour);
        }
    }

    public void tearDownListenerEndpointsFromEndpointsList(String neighbourName, Set<Endpoint> endpoints) {
        for(Endpoint endpoint : endpoints) {
            SubscriptionShard shard = endpoint.getShard();
            if (shard != null) {
                ListenerEndpoint listenerEndpoint = listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(shard.getExchangeName(), endpoint.getSource(), neighbourName);
                if (listenerEndpoint != null) {
                    listenerEndpointRepository.delete(listenerEndpoint);
                }
                logger.info("Tearing down listenerEndpoint for neighbour {} with host {} and source {}", neighbourName, endpoint.getHost(), endpoint.getSource());
            }
        }
    }
}
