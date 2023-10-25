package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
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
                for (Neighbour neighbour : neighbours) {
                    for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                        if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED) && localSubscription.isSubscriptionWanted()) {
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
            }
        }
    }

    public void syncMatchesToDelete() {
        List<Match> existingMatches = matchRepository.findAll();
        Set<Match> matchesToDelete = new HashSet<>();
        for (Match match : existingMatches) {
            if (match.subscriptionIsTearDown()) {
                if ( match.getSubscription().getEndpoints().isEmpty()) {
                    logger.info("Removing Match {}", match);
                    matchesToDelete.add(match);
                }
            } else {
                if (match.localSubscriptionIsTearDown()) {
                    logger.info("Removing Match {}", match);
                    matchesToDelete.add(match);
                }
            }
        }
        matchRepository.deleteAll(matchesToDelete);
    }
}