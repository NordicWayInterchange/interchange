package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.SubscriptionDeleteException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NeighbourSubscriptionDeleteService {

    private Logger logger = LoggerFactory.getLogger(NeighbourSubscriptionDeleteService.class);
    private final NeighbourRepository neighbourRepository;
    private final ListenerEndpointRepository listenerEndpointRepository;
    private final GracefulBackoffProperties backoffProperties;
    private final MatchRepository matchRepository;


    @Autowired
    public NeighbourSubscriptionDeleteService(NeighbourRepository neighbourRepository, ListenerEndpointRepository listenerEndpointRepository, GracefulBackoffProperties backoffProperties, MatchRepository matchRepository) {
        this.neighbourRepository = neighbourRepository;
        this.listenerEndpointRepository = listenerEndpointRepository;
        this.backoffProperties = backoffProperties;
        this.matchRepository = matchRepository;
    }

    public void deleteSubscriptions (NeighbourFacade neighbourFacade) {
        List<Neighbour> neighbours = neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN);
        for (Neighbour neighbour : neighbours) {
            if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                Set<Subscription> subscriptionsToDelete = new HashSet<>();
                for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                    if (SubscriptionStatus.shouldTearDown(subscription.getSubscriptionStatus())) {
                        try{
                            List<Match> matches = matchRepository.findAllBySubscriptionId(subscription.getId());
                            if (matches.isEmpty()) {
                                neighbourFacade.deleteSubscription(neighbour, subscription);
                                tearDownListenerEndpointsFromEndpointsList(neighbour, subscription);
                                subscriptionsToDelete.add(subscription);
                            } else {
                                tearDownListenerEndpointsFromEndpointsList(neighbour, subscription);
                                setMatchesToTearDownEndpoint(matches);
                            }
                        } catch(SubscriptionDeleteException e) {
                            subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                            tearDownListenerEndpointsFromEndpointsList(neighbour, subscription);
                            List<Match> matches = matchRepository.findAllBySubscriptionId(subscription.getId());
                            setMatchesToTearDownEndpoint(matches);
                            neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                            logger.warn("Exception when deleting subscription {} to neighbour {}. Starting backoff", subscription.getId(), neighbour.getName(), e);
                        } catch(SubscriptionNotFoundException e) {
                            logger.warn("Subscription {} gone from neighbour {}. Deleting subscription", subscription.getId(), neighbour.getName(), e);
                            tearDownListenerEndpointsFromEndpointsList(neighbour, subscription);
                            subscriptionsToDelete.add(subscription);
                        }
                    }
                }
                neighbour.getOurRequestedSubscriptions().deleteSubscriptions(subscriptionsToDelete);
                if (neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
                    neighbour.getOurRequestedSubscriptions().setStatus(SubscriptionRequestStatus.EMPTY);
                    logger.info("SubscriptionRequest is empty, setting SubscriptionRequestStatus to SubscriptionRequestStatus.EMPTY");
                }
                //tearDownListenerEndpoints(neighbour);
                neighbourRepository.save(neighbour);
                logger.debug("Saving updated neighbour: {}", neighbour.toString());
            }
        }
    }

    public void setMatchesToTearDownEndpoint(List<Match> matches) {
        Set<Match> matchesToSave = new HashSet<>();
        for (Match match : matches) {
            if (match.getStatus().equals(MatchStatus.UP)) {
                match.setStatus(MatchStatus.TEARDOWN_ENDPOINT);
                matchesToSave.add(match);
            }
        }
        matchRepository.saveAll(matchesToSave);
    }

    public void tearDownListenerEndpoints(Neighbour neighbour) {
        List<ListenerEndpoint> listenerEndpoints = listenerEndpointRepository.findAllByNeighbourName(neighbour.getName());
        Set<Subscription> subscriptions = neighbour.getOurRequestedSubscriptions().getCreatedSubscriptions();

        Set<String> sourceList = subscriptions.stream().flatMap(subscription -> subscription.getEndpoints().stream()).map(Endpoint::getSource).collect(Collectors.toSet());
        Set<String> hostList = subscriptions.stream().flatMap(subscription -> subscription.getEndpoints().stream()).map(Endpoint::getHost).collect(Collectors.toSet());

        for (ListenerEndpoint listenerEndpoint : listenerEndpoints ) {
            if (!sourceList.contains(listenerEndpoint.getSource()) && !hostList.contains(listenerEndpoint.getHost())) {
                listenerEndpointRepository.delete(listenerEndpoint);
                logger.info("Tearing down listenerEndpoint for neighbour {} with host {}, port {} and source {}", neighbour.getName(), listenerEndpoint.getHost(), listenerEndpoint.getPort(), listenerEndpoint.getSource());
            }
        }
    }

    public void tearDownListenerEndpointsFromEndpointsList(Neighbour neighbour, Subscription subscription) {
        Set<Endpoint> endpointsToRemove = new HashSet<>();
        for(Endpoint endpoint : subscription.getEndpoints()) {
            ListenerEndpoint listenerEndpoint = listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource(neighbour.getName(), endpoint.getHost(), endpoint.getPort(), endpoint.getSource());
            if (listenerEndpoint != null) {
                listenerEndpointRepository.delete(listenerEndpoint);
            }
            logger.info("Tearing down listenerEndpoint for neighbour {} with host {} and source {}", neighbour.getName(), endpoint.getHost(), endpoint.getSource());
            endpointsToRemove.add(endpoint);
        }
        subscription.getEndpoints().removeAll(endpointsToRemove);
    }
}
