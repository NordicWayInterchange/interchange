package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.exceptions.SubscriptionDeleteException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class NeighbourSubscriptionDeleteService {

    private Logger logger = LoggerFactory.getLogger(NeighbourSubscriptionDeleteService.class);
    private final NeighbourRepository neighbourRepository;
    private final GracefulBackoffProperties backoffProperties;
    private final MatchRepository matchRepository;


    @Autowired
    public NeighbourSubscriptionDeleteService(NeighbourRepository neighbourRepository, GracefulBackoffProperties backoffProperties, MatchRepository matchRepository) {
        this.neighbourRepository = neighbourRepository;
        this.backoffProperties = backoffProperties;
        this.matchRepository = matchRepository;
    }

    public void deleteSubscriptions (NeighbourFacade neighbourFacade) {
        Set<Neighbour> neighbours = new HashSet<>(neighbourRepository.findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus.TEAR_DOWN));
        for (Neighbour neighbour : neighbours) {
            if (neighbour.getControlConnection().canBeContacted(backoffProperties)) {
                Set<Subscription> subscriptionsToDelete = new HashSet<>();
                for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                    if (SubscriptionStatus.shouldTearDown(subscription.getSubscriptionStatus())) {
                        List<Match> matches = matchRepository.findAllBySubscriptionId(subscription.getId());
                        if (matches.isEmpty()) {
                            try{
                                if (subscription.getEndpoints().isEmpty()) {
                                    neighbourFacade.deleteSubscription(neighbour, subscription);
                                    subscriptionsToDelete.add(subscription);
                                }
                            } catch(SubscriptionDeleteException e) {
                                neighbour.getControlConnection().failedConnection(backoffProperties.getNumberOfAttempts());
                                logger.warn("Exception when deleting subscription {} to neighbour {}. Starting backoff", subscription.getId(), neighbour.getName(), e);
                            } catch(SubscriptionNotFoundException e) {
                                logger.warn("Subscription {} gone from neighbour {}. Deleting subscription", subscription.getId(), neighbour.getName(), e);
                            } finally {
                                if (subscription.getEndpoints().isEmpty()) {
                                    subscriptionsToDelete.add(subscription);
                                }
                            }
                        }
                    }
                }
                neighbour.getOurRequestedSubscriptions().deleteSubscriptions(subscriptionsToDelete);
                if (neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
                    logger.info("SubscriptionRequest is empty");
                }
                neighbourRepository.save(neighbour);
                logger.debug("Saving updated neighbour: {}", neighbour.toString());
            }
        }
    }
}
