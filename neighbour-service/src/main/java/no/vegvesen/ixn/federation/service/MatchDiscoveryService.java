package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class MatchDiscoveryService {

    public MatchRepository matchRepository;

    private Logger logger = LoggerFactory.getLogger(MatchDiscoveryService.class);

    @Autowired
    public MatchDiscoveryService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public void syncLocalSubscriptionAndSubscriptionsToCreateMatch(List<ServiceProvider> serviceProviders, List<Neighbour> neighbours) {
        for (ServiceProvider serviceProvider : serviceProviders) {
            Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions();
            String serviceProviderName = serviceProvider.getName();
            for (LocalSubscription localSubscription : localSubscriptions) {
                //if (!serviceProviderName.equals(localSubscription.getConsumerCommonName())) {
                    for (Neighbour neighbour : neighbours) {
                        for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                            if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                                //NOTE we use equals on the selectors here, as we expect the subscription to be made based on the local one,
                                //this ending up with the same selector.
                                //TODO this really is the most telltale sign that we need to promote Selector to a class
                                if (Objects.equals(localSubscription.getSelector(),subscription.getSelector()) &&
                                        Objects.equals(localSubscription.getConsumerCommonName(),subscription.getConsumerCommonName())) {
                                    //Here, we could return an object, and check if we have a matching... well, match, in the database at a later stage.
                                    //this would make a method that is completely independent on the repos.
                                    if (matchRepository.findBySubscriptionIdAndAndLocalSubscriptionId(subscription.getId(), localSubscription.getId()) == null) {
                                        Match newMatch = new Match(localSubscription, subscription, serviceProviderName);
                                        matchRepository.save(newMatch);
                                        logger.info("Saved new Match {}", newMatch);
                                    }
                                }
                            }
                        }
                    }
                //} else {
                /*    for (Neighbour neighbour : neighbours) {
                        for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                            if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)) {
                                if (Objects.equals(localSubscription.getSelector(),subscription.getSelector()) &&
                                        Objects.equals(localSubscription.getConsumerCommonName(),subscription.getConsumerCommonName())) {
                                    if (matchRepository.findBySubscriptionIdAndAndLocalSubscriptionId(subscription.getId(), localSubscription.getId()) == null) {
                                        Match newMatch = new Match(localSubscription, subscription, serviceProviderName);
                                        matchRepository.save(newMatch);
                                        logger.info("Saved new Match {}", newMatch);
                                    }
                                }
                            }
                        }
                    }
                }*/
            }
        }
    }

    //TODO: Remove match if either subscription endpoint or local subscription endpoint is gone?
    public void syncMatches(List<ServiceProvider> serviceProviders, List<Neighbour> neighbours) {
        //TODO: Implement
    }

    public List<Match> findMatchesByLocalSubscriptionId(Integer id) {
        return matchRepository.findAllByLocalSubscriptionId(id);
    }
}
