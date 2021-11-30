package no.vegvesen.ixn.federation.subscription;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionCalculatorTest {
    @Test
    void calculateSelfSubscriptionsTest() {
        LocalSubscription localSubA = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'FI'");
        LocalSubscription localSubB = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'SE'");
        LocalSubscription localSubC = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'NO'");

        ServiceProvider firstServiceProvider = new ServiceProvider();
        firstServiceProvider.setName("First Service Provider");
        firstServiceProvider.addLocalSubscription(localSubA);
        firstServiceProvider.addLocalSubscription(localSubB);

        ServiceProvider secondServiceProvider = new ServiceProvider();
        secondServiceProvider.setName("Second Service Provider");
        secondServiceProvider.addLocalSubscription(localSubB);
        secondServiceProvider.addLocalSubscription(localSubC);

        List<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toList());

        Set<LocalSubscription> selfSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(serviceProviders);

        assertThat(selfSubscriptions).hasSize(3);
        assertThat(selfSubscriptions).containsAll(Stream.of(localSubA, localSubB, localSubC).collect(Collectors.toSet()));
    }


    @Test
    void calculateLastUpdatedSubscriptionsEmpty() {
        ServiceProvider serviceProvider = new ServiceProvider();
        LocalDateTime lastUpdatedSubscriptions = SubscriptionCalculator.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider));
        assertThat(lastUpdatedSubscriptions).isNull();
    }


    @Test
    void calculateLastUpdatedSubscriptionOneSub() {
        ServiceProvider serviceProvider = new ServiceProvider();
        LocalSubscription subscription = new LocalSubscription(1,LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'NO'");
        serviceProvider.addLocalSubscription(subscription);
        Optional<LocalDateTime> lastUpdated = serviceProvider.getSubscriptionUpdated();
        assertThat(lastUpdated).isPresent();
        assertThat(SubscriptionCalculator.calculateLastUpdatedSubscriptions(Arrays.asList(serviceProvider))).isEqualTo(lastUpdated.get());
    }

    @Test
    void fetchSelfCalculatesGetLastUpdatedLocalSubscriptions() {
        LocalDateTime aprilNano1 = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 1);
        ServiceProvider aServiceProvider = new ServiceProvider(null,
                null,
                new Capabilities(),
                Collections.emptySet(),
                Collections.emptySet(),
                aprilNano1);


        LocalDateTime aprilNano2 = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
        ServiceProvider bServiceProvider = new ServiceProvider(null,
                null,
                new Capabilities(),
                Collections.emptySet(),
                Collections.emptySet(),
                aprilNano2);

        List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider).collect(Collectors.toList());
        assertThat(SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProviders)).isEqualTo(aprilNano2);
    }

    @Test
    void calculateLocalSubscriptionsShouldOnlyReturnDataTypesFromCreatedSubs() {
        LocalSubscription shouldNotBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'FI'");
        LocalSubscription shouldBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.CREATED,"messageType = 'DATEX2' AND originatingCountry = 'NO'");
        ServiceProvider serviceProvider = new ServiceProvider("serviceprovider");
        serviceProvider.setSubscriptions(Sets.newLinkedHashSet(shouldNotBeTakenIntoAccount,shouldBeTakenIntoAccount));
        Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(Arrays.asList(serviceProvider));
        assertThat(localSubscriptions).hasSize(1);
    }

    //TODO I'm having real problems understanding what this test really means... The filtering is done in the test, not the services/calculators.
    @Test
    void addLocalSubscriptionWithConsumerCommonNameSameAsServiceProviderNameFromServiceProvider() {
        ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
        LocalSubscription subscription1 = new LocalSubscription(LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'NO'", "my-service-provider");
        LocalSubscription subscription2 = new LocalSubscription(LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'SE'", "");

        serviceProvider.addLocalSubscription(subscription1);
        serviceProvider.addLocalSubscription(subscription2);

        List<ServiceProvider> SPs = new ArrayList<>(Arrays.asList(serviceProvider));

        Set<LocalSubscription> serviceProviderSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(SPs)
                .stream()
                .filter(s -> s.getConsumerCommonName().equals(serviceProvider.getName()))
                .collect(Collectors.toSet());

        assertThat(serviceProviderSubscriptions).hasSize(1);
    }

    @Test
    void serviceProviderGetsNewSubscriptionCreatedUpdatesSelfLastSubscriptionUpdate() {
        String serviceProviderName = "SelfServiceIT-service-provider";
        ServiceProvider serviceProviderBefore = new ServiceProvider(serviceProviderName);
        assertThat(serviceProviderBefore.getSubscriptionUpdated()).isEmpty();

        List<ServiceProvider> serviceProvidersBefore = Arrays.asList(serviceProviderBefore);
        LocalDateTime before = SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProvidersBefore);
        assertThat(before).isNull();

        serviceProviderBefore.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'"));
        Optional<LocalDateTime> subscriptionUpdatedRequested = serviceProviderBefore.getSubscriptionUpdated();
        assertThat(subscriptionUpdatedRequested).isPresent();

        Optional<LocalDateTime> subscriptionUpdatedRequestedSaved = serviceProviderBefore.getSubscriptionUpdated();
        assertThat(subscriptionUpdatedRequestedSaved).isNotNull().isEqualTo(subscriptionUpdatedRequested);

        Set<LocalSubscription> subscriptions = serviceProviderBefore.getSubscriptions();
        LocalSubscription requestedSubscription = subscriptions.iterator().next();
        LocalSubscription createdSubscription = requestedSubscription.withStatus(LocalSubscriptionStatus.CREATED);
        subscriptions.remove(requestedSubscription);
        subscriptions.add(createdSubscription);
        serviceProviderBefore.updateSubscriptions(subscriptions);


        assertThat(serviceProviderBefore.getSubscriptionUpdated()).isPresent().hasValueSatisfying(v -> v.isAfter(subscriptionUpdatedRequested.get()));

        LocalDateTime after = SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProvidersBefore);
        assertThat(Optional.of(after)).isNotEmpty().isEqualTo(serviceProviderBefore.getSubscriptionUpdated());
    }
}
