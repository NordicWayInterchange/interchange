package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
                    Set<Subscription> requestedSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions().stream()
                            .filter(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.REQUESTED))
                            .collect(Collectors.toSet());
                    for (Subscription subscription : requestedSubscriptions) {
                        if(localSubscription.getSelector().equals(subscription.getSelector())){
                            if (matchRepository.findBySubscriptionId(subscription.getId()) == null) {
                                Match newMatch = new Match(localSubscription, subscription, serviceProviderName, MatchStatus.SETUP_EXCHANGE);
                                matchRepository.save(newMatch);
                                logger.info("Saved new Match {}", newMatch);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Match> findMatchesToSetupExchangesFor(String serviceProviderName) {
        return matchRepository.findAllByServiceProviderNameAndStatus(serviceProviderName, MatchStatus.SETUP_EXCHANGE);
    }

    public void updateMatchToSetupEndpoint(Match match) {
        match.setStatus(MatchStatus.SETUP_ENDPOINT);
        matchRepository.save(match);
        logger.info("Saved match {} with status SETUP_ENDPOINT", match);
    }

    public Match findMatchesByExchangeName(String exchangeName) {
        return matchRepository.findBySubscription_ExchangeName(exchangeName);
    }

    public void updateMatchToUp(Match match) {
        match.setStatus(MatchStatus.UP);
        matchRepository.save(match);
        logger.info("Saved match {} with status UP", match);
    }

    public void syncLocalSubscriptionAndSubscriptionsToTearDownMatchResources() {
        List<Match> matches = matchRepository.findAllByStatus(MatchStatus.UP);
        for (Match match : matches) {
            if(match.getLocalSubscription().getStatus().equals(LocalSubscriptionStatus.TEAR_DOWN) ||
                    match.getSubscription().getSubscriptionStatus().equals(SubscriptionStatus.TEAR_DOWN)) {
                match.setStatus(MatchStatus.TEARDOWN_ENDPOINT);
                matchRepository.save(match);
                logger.info("Saved match {} with status TEARDOWN_ENDPOINT", match);
            }
        }
    }

    public void updateMatchToTearDownExchange(Match match) {
        match.setStatus(MatchStatus.TEARDOWN_EXCHANGE);
        matchRepository.save(match);
        logger.info("Saved match {} with status TEARDOWN_EXCHANGE", match);
    }

    public List<Match> findMatchesToTearDownExchangesFor(String serviceProviderName) {
        return matchRepository.findAllByServiceProviderNameAndStatus(serviceProviderName, MatchStatus.TEARDOWN_EXCHANGE);
    }

    public void updateMatchToDeleted(Match match) {
        match.setStatus(MatchStatus.DELETED);
        matchRepository.save(match);
        logger.info("Saved match {} with status DELETED", match);
    }

    public void removeMatchesThatAreDeleted() {
        List<Match> matchesToRemove = matchRepository.findAllByStatus(MatchStatus.DELETED);
        if (!matchesToRemove.isEmpty()) {
            matchRepository.deleteAll(matchesToRemove);
            logger.info("Removed deleted Matches");
        }
    }

    public Match findMatchByLocalSubscriptionId(Integer id) {
        return matchRepository.findByLocalSubscriptionId(id);
    }
}
