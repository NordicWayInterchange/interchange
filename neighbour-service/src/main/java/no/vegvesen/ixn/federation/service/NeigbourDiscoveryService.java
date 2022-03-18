package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.discoverer.NeighbourDiscovererProperties;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionDeleteException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
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

    private Logger logger = LoggerFactory.getLogger(NeigbourDiscoveryService.class);

    private final DNSFacade dnsFacade;
    private final NeighbourRepository neighbourRepository;
    private final ListenerEndpointRepository listenerEndpointRepository;
    private final InterchangeNodeProperties interchangeNodeProperties;
    private final GracefulBackoffProperties backoffProperties;
    private final NeighbourDiscovererProperties discovererProperties;
    private final MatchRepository matchRepository;

    @Autowired
    public NeigbourDiscoveryService(DNSFacade dnsFacade,
                                    NeighbourRepository neighbourRepository,
                                    ListenerEndpointRepository listenerEndpointRepository,
                                    InterchangeNodeProperties interchangeNodeProperties,
                                    GracefulBackoffProperties backoffProperties,
                                    NeighbourDiscovererProperties discovererProperties, MatchRepository matchRepository) {
        this.dnsFacade = dnsFacade;
        this.neighbourRepository = neighbourRepository;
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.interchangeNodeProperties = interchangeNodeProperties;
        this.backoffProperties = backoffProperties;
        this.discovererProperties = discovererProperties;
        this.matchRepository = matchRepository;
    }
    public void checkForNewNeighbours() {
        logger.info("Checking DNS for new neighbours using {}.", dnsFacade.getClass().getSimpleName());
        List<Neighbour> neighbours = dnsFacade.lookupNeighbours();
        logger.debug("Got neighbours from DNS {}.", neighbours);

        for (Neighbour neighbour : neighbours) {
            NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
            if (neighbourRepository.findByName(neighbour.getName()) == null && !neighbour.getName().equals(interchangeNodeProperties.getName())) {

                // Found a new Neighbour. Set capabilities status of neighbour to UNKNOWN to trigger capabilities exchange.
                neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.UNKNOWN);
                logger.info("Found a new neighbour. Saving in database");
                neighbourRepository.save(neighbour);
            }
            NeighbourMDCUtil.removeLogVariables();
        }
    }

    public void capabilityExchangeWithNeighbours(NeighbourFacade neighbourFacade, Set<Capability> localCapabilities, Optional<LocalDateTime> lastUpdatedLocalCapabilities) {
        logger.info("Checking for any neighbours with UNKNOWN capabilities for capability exchange");
        List<Neighbour> neighboursForCapabilityExchange = neighbourRepository.findByCapabilities_StatusIn(
                Capabilities.CapabilitiesStatus.UNKNOWN,
                Capabilities.CapabilitiesStatus.KNOWN,
                Capabilities.CapabilitiesStatus.FAILED);
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
            logger.info("Retrying connection to unreachable neighbours {}", unreachableNeighbours.stream().map(Neighbour::getName).collect(Collectors.toList()));
            for (Neighbour neighbour : unreachableNeighbours) {
                try {
                    NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
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
            Capabilities capabilities = neighbourFacade.postCapabilitiesToCapabilities(neighbour, selfName, localCapabilities);
            capabilities.setLastCapabilityExchange(LocalDateTime.now());
            neighbour.setCapabilities(capabilities);
            neighbour.getControlConnection().okConnection();
            logger.info("Successfully completed capability exchange.");
            logger.debug("Updated neighbour: {}", neighbour.toString());
        } catch (CapabilityPostException e) {
            logger.error("Capability post failed", e);
            neighbour.getCapabilities().setStatus(Capabilities.CapabilitiesStatus.FAILED);
            neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
        } finally {
            neighbour = neighbourRepository.save(neighbour);
            logger.info("Saving updated neighbour: {}", neighbour.toString());
        }
    }

    public void evaluateAndPostSubscriptionRequest(List<Neighbour> neighboursForSubscriptionRequest, Optional<LocalDateTime> lastUpdatedLocalSubscriptions, Set<LocalSubscription> localSubscriptions, NeighbourFacade neighbourFacade) {

        for (Neighbour neighbour : neighboursForSubscriptionRequest) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.hasEstablishedSubscriptions() || neighbour.hasCapabilities()) {
                    if (neighbour.shouldCheckSubscriptionRequestsForUpdates(lastUpdatedLocalSubscriptions)) {
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

    public void postSubscriptionRequest(Neighbour neighbour, Set<LocalSubscription> localSubscriptions, NeighbourFacade neighbourFacade) {
        String neighbourName = neighbour.getName();
        Set<Capability> neighbourCapabilities = neighbour.getCapabilities().getCapabilities();
        SubscriptionRequest ourRequestedSubscriptionsFromNeighbour = neighbour.getOurRequestedSubscriptions();
        logger.info("Found neighbour for subscription request: {}", neighbourName);
        Set<Subscription> wantedSubscriptions = calculateCustomSubscriptionForNeighbour(localSubscriptions, neighbourCapabilities, neighbourName);
        Set<Subscription> existingSubscriptions = ourRequestedSubscriptionsFromNeighbour.getSubscriptions();
        if (!wantedSubscriptions.equals(existingSubscriptions)) {
            Set<Subscription> subscriptionsToRemove = new HashSet<>(existingSubscriptions);
            subscriptionsToRemove.removeAll(wantedSubscriptions);
            for (Subscription subscription : subscriptionsToRemove) {
                if (!subscription.getEndpoints().isEmpty()) {
                    tearDownListenerEndpointsFromEndpointsList(neighbour, subscription.getEndpoints());
                }
                subscription.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN);
            }
            Connection controlConnection = neighbour.getControlConnection();
            try {
                if (controlConnection.canBeContacted(backoffProperties)) {
                    Set<Subscription> additionalSubscriptions = new HashSet<>(wantedSubscriptions);
                    additionalSubscriptions.removeAll(existingSubscriptions);
                    if (!additionalSubscriptions.isEmpty()) {
                        Set<Subscription> responseSubscriptions = neighbourFacade.postSubscriptionRequest(neighbour, additionalSubscriptions, interchangeNodeProperties.getName());
                        for(Subscription subscription : responseSubscriptions) {
                            String exchangeName = UUID.randomUUID().toString();
                            subscription.setExchangeName(exchangeName);
                        }
                        ourRequestedSubscriptionsFromNeighbour.addNewSubscriptions(responseSubscriptions);
                        ourRequestedSubscriptionsFromNeighbour.setSuccessfulRequest(LocalDateTime.now());
                        //TODO we never changed the subscription status. We don't now either. Is that OK?

                    }
                    controlConnection.okConnection();
                    logger.info("Successfully posted subscription request to neighbour.");
                } else {
                    logger.info("Too soon to post subscription request to neighbour when backing off");
                }
            } catch (SubscriptionRequestException e) {
                ourRequestedSubscriptionsFromNeighbour.setStatus(SubscriptionRequestStatus.FAILED);
                controlConnection.failedConnection(backoffProperties.getNumberOfAttempts());
                logger.error("Failed subscription request. Setting status of neighbour fedIn to FAILED.", e);
            }
            // Successful subscription request, update discovery state subscription request timestamp.
            neighbour = neighbourRepository.save(neighbour);
            logger.info("Saving updated neighbour: {}", neighbour.toString());
        } else {
            logger.info("Neighbour has our last subscription request");
        }
    }

    Set<Subscription> calculateCustomSubscriptionForNeighbour(Set<LocalSubscription> localSubscriptions, Set<Capability> neighbourCapabilities, String neighbourName) {
        logger.info("Calculating custom subscription for neighbour: {}", neighbourName);
        logger.debug("Neighbour capabilities {}", neighbourCapabilities);
        logger.debug("Local subscriptions {}", localSubscriptions);
        Set<LocalSubscription> matchingSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(neighbourCapabilities, localSubscriptions);
        Set<Subscription> calculatedSubscriptions = new HashSet<>();
        for (LocalSubscription subscription : matchingSubscriptions) {
            Subscription newSubscription = new Subscription(subscription.getSelector(),
                    SubscriptionStatus.REQUESTED,
                    interchangeNodeProperties.getName());
            calculatedSubscriptions.add(newSubscription);
        }
        logger.info("Calculated custom subscription for neighbour {}: {}", neighbourName, calculatedSubscriptions);
        return calculatedSubscriptions;
    }

    public void pollSubscriptions(NeighbourFacade neighbourFacade) {
        List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
                SubscriptionStatus.REQUESTED,
                SubscriptionStatus.ACCEPTED,
                SubscriptionStatus.FAILED);
        for (Neighbour neighbour : neighboursToPoll) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    pollSubscriptionsOneNeighbour(neighbour, neighbour.getSubscriptionsForPolling(), neighbourFacade);
                }
            } catch (Exception e) {
                logger.error("Unknown error while polling subscriptions for one neighbour",e);
            }
            finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    private void pollSubscriptionsOneNeighbour(Neighbour neighbour, Set<Subscription> subscriptions, NeighbourFacade neighbourFacade) {
        try {
            for (Subscription subscription : subscriptions) {
                try {
                    if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
                        logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

                        // Throws SubscriptionPollException if unsuccessful
                        Subscription polledSubscription = neighbourFacade.pollSubscriptionStatus(subscription, neighbour);
                        subscription.setSubscriptionStatus(polledSubscription.getSubscriptionStatus());
                        subscription.setNumberOfPolls(subscription.getNumberOfPolls() + 1);
                        subscription.setConsumerCommonName(polledSubscription.getConsumerCommonName());
                        subscription.setEndpoints(polledSubscription.getEndpoints());
                        subscription.setLastUpdatedTimestamp(polledSubscription.getLastUpdatedTimestamp());
                        neighbour.getControlConnection().okConnection();
                        if(subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)){
                            logger.info("Subscription for neighbour {} with path {} is CREATED",neighbour.getName(),subscription.getPath());
                            if (polledSubscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
                                logger.info("Creating listener endpoint for neighbour {} with path {} and brokers {}",neighbour.getName(),subscription.getPath(), subscription.getEndpoints());
                                createListenerEndpointFromEndpointsList(neighbour, subscription.getEndpoints(), subscription.getExchangeName());
                            }
                        }
                        //utvide med ListenerEndpoint lookup + lage ny om det trengs
                        logger.info("Successfully polled subscription. Subscription status: {}  - Number of polls: {}", subscription.getSubscriptionStatus(), subscription.getNumberOfPolls());
                    } else {
                        // Number of poll attempts exceeds allowed number of poll attempts.
                        subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                        logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
                    }
                } catch (SubscriptionPollException e) {
                    subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
                    neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                    logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.", e);
                }
            }
        } finally {
            logger.info("Saving updated neighbour: {}", neighbour.toString());
            neighbourRepository.save(neighbour);
        }
    }

    public void createListenerEndpointFromEndpointsList(Neighbour neighbour, Set<Endpoint> endpoints, String exchangeName) {
        for(Endpoint endpoint : endpoints) {
            createListenerEndpoint(endpoint.getHost(),endpoint.getPort(), endpoint.getSource(), exchangeName, neighbour);
        }
    }

    public void createListenerEndpoint(String host, Integer port, String source, String exchangeName, Neighbour neighbour) {
        if(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource(neighbour.getName(), host, port, source) == null){
            ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbour.getName(), source, host, port, new Connection(), exchangeName));
            logger.info("ListenerEndpoint was saved: {}", savedListenerEndpoint.toString());
        }
    }

    public void pollSubscriptionsWithStatusCreated(NeighbourFacade neighbourFacade) {
        List<Neighbour> neighboursToPoll = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(
                SubscriptionStatus.CREATED);
        for (Neighbour neighbour : neighboursToPoll) {
            try {
                NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), neighbour.getName());
                if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                    pollSubscriptionsWithStatusCreatedOneNeighbour(neighbour, neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions(), neighbourFacade);
                }
            } catch (Exception e) {
                logger.error("Unknown error while polling subscription with status CREATED",e);
            }
            finally {
                NeighbourMDCUtil.removeLogVariables();
            }
        }
    }

    //TODO have to have a look at this again. The problem is that we might have non-updated subscriptions on the neighbour side, but for some reason not set it up on our side. The lastUpdated prevents us from setting it up again.
    //need to somehow check is the listenerEndpoint is already there before checking the updated timestamp.
    //It's already done in createListenerEndpointOneNeighbour, but should probably be done earlier. This
    public void pollSubscriptionsWithStatusCreatedOneNeighbour(Neighbour neighbour, Set<Subscription> subscriptions, NeighbourFacade neighbourFacade) {
        try {
            for (Subscription subscription : subscriptions) {
                try {
                    if (subscription.getNumberOfPolls() < discovererProperties.getSubscriptionPollingNumberOfAttempts()) {
                        Subscription lastUpdatedSubscription = neighbourFacade.pollSubscriptionLastUpdatedTime(subscription, neighbour);
                            if (!lastUpdatedSubscription.getEndpoints().isEmpty() || !subscription.getEndpoints().equals(lastUpdatedSubscription.getEndpoints())) {
                                //if (lastUpdatedSubscription.getLastUpdatedTimestamp() != subscription.getLastUpdatedTimestamp()) {
                                logger.info("Polled updated subscription with id {}", subscription.getId());
                                EndpointCalculator endpointCalculator = new EndpointCalculator(
                                        subscription.getEndpoints(),
                                        lastUpdatedSubscription.getEndpoints()
                                );
                                tearDownListenerEndpointsFromEndpointsList(neighbour,endpointCalculator.getEndpointsToRemove());
                                createListenerEndpointFromEndpointsList(
                                        neighbour,
                                        endpointCalculator.getNewEndpoints(),
                                        subscription.getExchangeName()
                                );
                                subscription.setEndpoints(endpointCalculator.getCalculatedEndpointsSet());
                            } else {
                                subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                                logger.warn("Number of polls has exceeded number of allowed polls. Setting subscription status to GIVE_UP.");
                            }

                    }
                } catch (SubscriptionPollException e) {
                    subscription.setSubscriptionStatus(SubscriptionStatus.FAILED);
                    neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                    logger.error("Error in polling for subscription status. Setting status of Subscription to FAILED.", e);
                }
            }
        }
        finally {
            logger.info("Saving updated neighbour: {}", neighbour.toString());
            neighbourRepository.save(neighbour);
        }
    }

    public void tearDownListenerEndpointsFromEndpointsList(Neighbour neighbour, Set<Endpoint> endpoints) {
        for(Endpoint endpoint : endpoints) {
            ListenerEndpoint listenerEndpoint = listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource(neighbour.getName(), endpoint.getHost(), endpoint.getPort(), endpoint.getSource());
            if (listenerEndpoint != null) {
                listenerEndpointRepository.delete(listenerEndpoint);
            }
            logger.info("Tearing down listenerEndpoint for neighbour {} with host {} and source {}", neighbour.getName(), endpoint.getHost(), endpoint.getSource());
        }
    }

    public void deleteSubscriptions (NeighbourFacade neighbourFacade) {
        List<Neighbour> neighbours = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN);
        for (Neighbour neighbour : neighbours) {
            if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                Set<Subscription> subscriptionsToDelete = new HashSet<>();
                for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                    if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN)) {
                        try{
                            Match match = matchRepository.findBySubscriptionId(subscription.getId());
                            if (match == null) {
                                neighbourFacade.deleteSubscription(neighbour, subscription);
                                subscriptionsToDelete.add(subscription);
                            }
                        } catch(SubscriptionDeleteException e) {
                            logger.error("Exception when deleting subscription {} to neighbour {}", subscription.getId(), neighbour.getName(), e);
                        }
                    }
                }
                neighbour.getOurRequestedSubscriptions().getSubscriptions().removeAll(subscriptionsToDelete);
                if (neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
                    neighbour.getOurRequestedSubscriptions().setStatus(SubscriptionRequestStatus.EMPTY);
                    logger.info("SubscriptionRequest is empty, setting SubscriptionRequestStatus to SubscriptionRequestStatus.EMPTY");
                }
                tearDownListenerEndpoints(neighbour);
                neighbourRepository.save(neighbour);
                logger.debug("Saving updated neighbour: {}", neighbour.toString());
            }
        }
    }

    public void tearDownListenerEndpoints(Neighbour neighbour) {
        List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAllByNeighbourName(neighbour.getName());
        Set<Subscription> subscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions();

        Set<String> sourceList = subscriptions.stream().flatMap(subscription -> subscription.getEndpoints().stream()).map(Endpoint::getSource).collect(Collectors.toSet());
        Set<String> hostList = subscriptions.stream().flatMap(subscription -> subscription.getEndpoints().stream()).map(Endpoint::getHost).collect(Collectors.toSet());

        for (ListenerEndpoint listenerEndpoint : listenerEndpoints ) {
            if (!sourceList.contains(listenerEndpoint.getSource()) && !hostList.contains(listenerEndpoint.getHost())) {
                listenerEndpointRepository.delete(listenerEndpoint);
                logger.info("Tearing down listenerEndpoint for neighbour {} with host {}, port {} and source {}", neighbour.getName(), listenerEndpoint.getHost(), listenerEndpoint.getPort(), listenerEndpoint.getSource());
            }
        }
    }

}
