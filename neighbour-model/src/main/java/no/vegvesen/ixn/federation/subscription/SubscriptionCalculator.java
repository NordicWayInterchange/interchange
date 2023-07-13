package no.vegvesen.ixn.federation.subscription;

import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
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
        logger.debug("Calculating Self subscriptions...");
        Set<LocalSubscription> localSubscriptions = new HashSet<>();

        for (ServiceProvider serviceProvider : serviceProviders) {
            logger.debug("Service provider name: {}", serviceProvider.getName());
            Set<LocalSubscription> serviceProviderSubscriptions = serviceProvider
                    .getSubscriptions()
                    .stream()
                    .filter(subscription -> LocalSubscriptionStatus.CREATED.equals(subscription.getStatus()))
                    .collect(Collectors.toSet());
            logger.debug("Service Provider Subscriptions: {}", serviceProviderSubscriptions);
            localSubscriptions.addAll(serviceProviderSubscriptions);
        }
        logger.debug("Calculated Self subscriptions: {}", localSubscriptions.toString());
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

    public static Set<Subscription> calculateCustomSubscriptionForNeighbour(Set<LocalSubscription> localSubscriptions, Set<CapabilitySplit> neighbourCapabilities, String ixnName) {
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
        return calculatedSubscriptions;
    }
}
