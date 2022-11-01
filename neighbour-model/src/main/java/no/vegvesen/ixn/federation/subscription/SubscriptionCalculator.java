package no.vegvesen.ixn.federation.subscription;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SubscriptionCalculator {

    private static Logger logger = LoggerFactory.getLogger(SubscriptionCalculator.class);

    public static Set<LocalSubscription> calculateSelfSubscriptions(List<ServiceProvider> serviceProviders) {
        logger.info("Calculating Self subscriptions...");
        Set<LocalSubscription> localSubscriptions = new HashSet<>();

        for (ServiceProvider serviceProvider : serviceProviders) {
            logger.info("Service provider name: {}", serviceProvider.getName());
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider
                    .getSubscriptions()
                    .stream()
                    .filter(subscription -> LocalSubscriptionStatus.CREATED.equals(subscription.getStatus()))
                    .collect(Collectors.toSet());
            logger.info("Service Provider Subscriptions: {}", serviceProviderSubscriptions.toString());
            localSubscriptions.addAll(serviceProviderSubscriptions);
        }
        logger.info("Calculated Self subscriptions: {}", localSubscriptions.toString());
        return localSubscriptions;
    }

    public static LocalDateTime calculateLastUpdatedSubscriptions(List<ServiceProvider> serviceProviders) {
        LocalDateTime result = null;
        for (ServiceProvider serviceProvider : serviceProviders) {
            Optional<LocalDateTime> lastUpdated = serviceProvider.getSubscriptionUpdated();
            if (lastUpdated.isPresent()) {
                if (result == null || lastUpdated.get().isAfter(result)) {
                    result = lastUpdated.get();
                }
            }
        }
        return result;
    }

    public static Set<Subscription> calculateCustomSubscriptionForNeighbour(Set<LocalSubscription> localSubscriptions, Set<Capability> neighbourCapabilities, String ixnName) {
        //logger.info("Calculating custom subscription for neighbour: {}", neighbourName);
        //logger.debug("Neighbour capabilities {}", neighbourCapabilities);
        //logger.debug("Local subscriptions {}", localSubscriptions);
        Set<LocalSubscription> matchingSubscriptions = CapabilityMatcher.calculateNeighbourSubscriptionsFromSelectors(neighbourCapabilities, localSubscriptions, ixnName);
        Set<Subscription> calculatedSubscriptions = new HashSet<>();
        for (LocalSubscription subscription : matchingSubscriptions) {
            if (LocalSubscriptionStatus.isAlive(subscription.getStatus())) {
                Subscription newSubscription = new Subscription(subscription.getSelector(),
                        SubscriptionStatus.REQUESTED,
                        subscription.getConsumerCommonName());
                calculatedSubscriptions.add(newSubscription);
            }
        }
        //logger.info("Calculated custom subscription for neighbour {}: {}", neighbourName, calculatedSubscriptions);
        return calculatedSubscriptions;
    }
}
