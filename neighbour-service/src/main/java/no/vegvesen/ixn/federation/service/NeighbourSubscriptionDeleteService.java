package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.NeighbourSubscriptionNotFound;
import no.vegvesen.ixn.federation.exceptions.SubscriptionDeleteException;
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
                    if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN)) {
                        try{
                            List<Match> match = matchRepository.findAllBySubscriptionId(subscription.getId());
                            if (match.isEmpty()) {
                                neighbourFacade.deleteSubscription(neighbour, subscription);
                                subscriptionsToDelete.add(subscription);
                            }
                        } catch(SubscriptionDeleteException e) {
                            subscription.setSubscriptionStatus(SubscriptionStatus.GIVE_UP);
                            neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                            logger.warn("Exception when deleting subscription {} to neighbour {}. Starting backoff", subscription.getId(), neighbour.getName(), e);
                        } catch(NeighbourSubscriptionNotFound e) {
                            logger.warn("Subscription {} gone from neighbour {}. Deleting subscription", subscription.getId(), neighbour.getName(), e);
                            subscriptionsToDelete.add(subscription);
                        }
                    }
                }
                neighbour.getOurRequestedSubscriptions().deleteSubscriptions(subscriptionsToDelete);
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
}
