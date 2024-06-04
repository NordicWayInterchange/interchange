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
        List<SubscriptionMatch> existingSubscriptionMatches = subscriptionMatchRepository.findAll();
        Set<SubscriptionMatch> matchesToDelete = new HashSet<>();
        for (SubscriptionMatch subscriptionMatch : existingSubscriptionMatches) {
            if (subscriptionMatch.subscriptionIsTearDown()) {
                if ( subscriptionMatch.getSubscription().getEndpoints().isEmpty()) {
                    logger.info("Removing SubscriptionMatch {}", subscriptionMatch);
                    matchesToDelete.add(subscriptionMatch);
                }
            } else {
                if (subscriptionMatch.localSubscriptionIsTearDown()) {
                    logger.info("Removing SubscriptionMatch {}", subscriptionMatch);
                    matchesToDelete.add(subscriptionMatch);
                }
            }
        }
        subscriptionMatchRepository.deleteAll(matchesToDelete);
    }
}