package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SubscriptionMatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class SubscriptionMatchDiscoveryService {

    public SubscriptionMatchRepository subscriptionMatchRepository;

    private Logger logger = LoggerFactory.getLogger(SubscriptionMatchDiscoveryService.class);

    @Autowired
    public SubscriptionMatchDiscoveryService(SubscriptionMatchRepository subscriptionMatchRepository) {
        this.subscriptionMatchRepository = subscriptionMatchRepository;
    }

    public boolean checkIfSubscriptionShouldResubscribe(LocalSubscription localSubscription, Subscription subscription) {
        if(!(Objects.equals(subscription.getSelector(), localSubscription.getSelector()) && Objects.equals(subscription.getConsumerCommonName(), localSubscription.getConsumerCommonName()))){
            return false;
        }

        List<SubscriptionMatch> matches = subscriptionMatchRepository.findAllByLocalSubscriptionId(localSubscription.getId());
        if (!matches.isEmpty()) {
            SubscriptionMatch match = subscriptionMatchRepository.findBySubscriptionIdAndLocalSubscriptionId(subscription.getId(), localSubscription.getId());
            return match == null;
        }
        else {
            return false;
        }
    }

    public void syncLocalSubscriptionAndSubscriptionsToCreateMatch(List<ServiceProvider> serviceProviders, List<Neighbour> neighbours) {
        for (ServiceProvider serviceProvider : serviceProviders) {
            Set<LocalSubscription> localSubscriptions = serviceProvider.getSubscriptions();
            String serviceProviderName = serviceProvider.getName();
            for (LocalSubscription localSubscription : localSubscriptions) {
                for (Neighbour neighbour : neighbours) {
                    for (Subscription subscription : neighbour.getOurRequestedSubscriptions().getSubscriptions()) {
                        if (subscription.getSubscriptionStatus().equals(SubscriptionStatus.CREATED) && localSubscription.getStatus().equals(LocalSubscriptionStatus.CREATED)) {
                            if (Objects.equals(localSubscription.getSelector(),subscription.getSelector()) &&
                                    Objects.equals(localSubscription.getConsumerCommonName(),subscription.getConsumerCommonName())) {
                                if (subscriptionMatchRepository.findBySubscriptionIdAndAndLocalSubscriptionId(subscription.getId(), localSubscription.getId()) == null) {
                                    SubscriptionMatch newSubscriptionMatch = new SubscriptionMatch(localSubscription, subscription, serviceProviderName);
                                    subscriptionMatchRepository.save(newSubscriptionMatch);
                                    logger.info("Saved new SubscriptionMatch {}", newSubscriptionMatch);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void syncMatchesToDelete() {
        List<SubscriptionMatch> existingMatches = subscriptionMatchRepository.findAll();
        Set<SubscriptionMatch> matchesToDelete = new HashSet<>();
        for (SubscriptionMatch match : existingMatches) {
            if (match.subscriptionIsTearDown()) {
                if (match.getSubscription().getEndpoints().isEmpty()) {
                    logger.info("Removing Match {}", match);
                    matchesToDelete.add(match);
                }
            } else if(match.localSubscriptionIsTearDown()){
                logger.info("Removing Match {}", match);
                matchesToDelete.add(match);
            } else if(match.localSubscriptionAndSubscriptionIsResubscribe()){
                logger.info("Removing Match {}", match);
                matchesToDelete.add(match);
            }
        }
        subscriptionMatchRepository.deleteAll(matchesToDelete);
    }
}