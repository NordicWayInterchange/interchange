package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionPostCalculatorTest {

    @Test
    public void singleWantedNoExistingSubscriptions() {
        Subscription wantedSubscription = new Subscription(
                SubscriptionStatus.REQUESTED,
                "a = b",
                "/",
                "user"
        );
        Set<Subscription> wantedSubscriptions = Collections.singleton(wantedSubscription);
        Set<Subscription> existingSubscriptions = Collections.emptySet();
        SubscriptionPostCalculator subscriptionPostCalculator = new SubscriptionPostCalculator(existingSubscriptions,wantedSubscriptions);
        assertThat(subscriptionPostCalculator.getSubscriptionsToRemove()).isEmpty();
        assertThat(subscriptionPostCalculator.getNewSubscriptions()).hasSize(1).contains(wantedSubscription);
        assertThat(subscriptionPostCalculator.getCalculatedSubscriptions()).hasSize(1).contains(wantedSubscription);
    }

    @Test
    public void singleExistingNoneWanted() {
        Subscription subscription = new Subscription(
                1,
                SubscriptionStatus.REQUESTED,
                "a = b",
                "/",
                "user"
        );
        Set<Subscription> existing = Collections.singleton(
                subscription
       );
       Set<Subscription> wanted = Collections.emptySet();
       SubscriptionPostCalculator calculator = new SubscriptionPostCalculator(existing,wanted);
       assertThat(calculator.getNewSubscriptions()).isEmpty();
       assertThat(calculator.getSubscriptionsToRemove()).hasSize(1).contains(subscription);
       assertThat(calculator.getCalculatedSubscriptions()).isEmpty();
    }

    @Test
    public void singleWantedSameAsExistingSubscriptionWithNoEndpoints() {
        Subscription wantedSubscription = new Subscription(
                SubscriptionStatus.REQUESTED,
                "a = b",
                "/",
                "user"
        );
        Set<Subscription> wantedSubscriptions = Collections.singleton(wantedSubscription);
        Subscription existingSubscription = new Subscription(
                1,
                SubscriptionStatus.REQUESTED,
                "a = b",
                "/",
                "user"
        );
        Set<Subscription> existingSubscriptions = Collections.singleton(existingSubscription);
        SubscriptionPostCalculator subscriptionPostCalculator = new SubscriptionPostCalculator(existingSubscriptions,wantedSubscriptions);
        assertThat(subscriptionPostCalculator.getSubscriptionsToRemove()).isEmpty();
        assertThat(subscriptionPostCalculator.getNewSubscriptions()).isEmpty();
        assertThat(subscriptionPostCalculator.getCalculatedSubscriptions()).hasSize(1).contains(existingSubscription);
    }
}
