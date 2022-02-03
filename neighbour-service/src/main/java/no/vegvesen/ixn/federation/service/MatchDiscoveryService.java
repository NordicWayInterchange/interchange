package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MatchDiscoveryService {

    private MatchRepository matchRepository;

    private Logger logger = LoggerFactory.getLogger(MatchDiscoveryService.class);

    @Autowired
    public MatchDiscoveryService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public void syncLocalSubscriptionAndSubscriptionsToMatch(List<ServiceProvider> serviceProviders, List<Neighbour> neighbours) {
        removeMatchesToTearDown();
        for (ServiceProvider serviceProvider : serviceProviders) {
            Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions();
            String serviceProviderName = serviceProvider.getName();
            for (LocalSubscription localSubscription : localSubscriptions) {
                for (Neighbour neighbour : neighbours) {
                    Set<Subscription> requestedSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions().stream()
                            .filter(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.REQUESTED))
                            .collect(Collectors.toSet());
                    for (Subscription subscription : requestedSubscriptions) {
                        if(localSubscription.getSelector().equals(subscription.getSelector())){
                            if (matchRepository.findBySubscriptionId(subscription.getId()) == null) {
                            Match newMatch = new Match(localSubscription, subscription, serviceProviderName, MatchStatus.REQUESTED);
                            matchRepository.save(newMatch);
                            logger.info("Saved new Match {}", newMatch);
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeMatchesToTearDown() {
        List<Match> matchesToTearDown = matchRepository.findAllByStatus(MatchStatus.DELETED);
        Set<Match> matchesToRemove = new HashSet<>();
        for (Match match : matchesToTearDown) {
            if (match.getLocalSubscription().getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN)
                    || match.getSubscription().getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN)) {
                matchesToRemove.add(match);
            }
        }
        if (!matchesToRemove.isEmpty()) {
            matchRepository.deleteAll(matchesToRemove);
        }
    }
}
