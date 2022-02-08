package no.vegvesen.ixn.federation.subscription;

import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
}