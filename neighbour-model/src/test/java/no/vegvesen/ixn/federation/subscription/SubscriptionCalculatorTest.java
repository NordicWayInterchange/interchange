package no.vegvesen.ixn.federation.subscription;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionCalculatorTest {

    private String myName = "my-interchange";

    @Test
    void calculateSelfSubscriptionsTest() {
        LocalSubscription localSubA = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'FI'",myName);
        LocalSubscription localSubB = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'SE'",myName);
        LocalSubscription localSubC = new LocalSubscription(LocalSubscriptionStatus.CREATED,"originatingCountry = 'NO'",myName);

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
        LocalSubscription subscription = new LocalSubscription(1,LocalSubscriptionStatus.CREATED, "messageType = 'DATEX2' AND originatingCountry = 'NO'","");
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
                aprilNano1);


        LocalDateTime aprilNano2 = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
        ServiceProvider bServiceProvider = new ServiceProvider(null,
                null,
                new Capabilities(),
                Collections.emptySet(),
                aprilNano2);

        List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider).collect(Collectors.toList());
        assertThat(SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProviders)).isEqualTo(aprilNano2);
    }

    @Test
    void calculateLocalSubscriptionsShouldOnlyReturnDataTypesFromCreatedSubs() {
        LocalSubscription shouldNotBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'FI'",myName);
        LocalSubscription shouldBeTakenIntoAccount = new LocalSubscription(LocalSubscriptionStatus.CREATED,"messageType = 'DATEX2' AND originatingCountry = 'NO'",myName);
        ServiceProvider serviceProvider = new ServiceProvider("serviceprovider");
        serviceProvider.setSubscriptions(Sets.newLinkedHashSet(shouldNotBeTakenIntoAccount,shouldBeTakenIntoAccount));
        Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(Arrays.asList(serviceProvider));
        assertThat(localSubscriptions).hasSize(1);
    }

    @Test
    void serviceProviderGetsNewSubscriptionCreatedUpdatesSelfLastSubscriptionUpdate() {
        String serviceProviderName = "SelfServiceIT-service-provider";
        ServiceProvider serviceProviderBefore = new ServiceProvider(serviceProviderName);
        assertThat(serviceProviderBefore.getSubscriptionUpdated()).isEmpty();

        List<ServiceProvider> serviceProvidersBefore = Arrays.asList(serviceProviderBefore);
        LocalDateTime before = SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProvidersBefore);
        assertThat(before).isNull();

        serviceProviderBefore.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "messageType = 'DATEX2'",myName));
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

    @Test
    void calculateEmptyCapabiliitiesAndSubscriptions() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                Collections.emptySet(),
                Collections.emptySet(), ""
        );
        assertThat(calculatedSubscriptions).isEmpty();
    }

    @Test
    void calculateEmptyCapabilitiesActualSubscription() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                Collections.singleton(
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "originatinCountry = 'NO'",myName
                        )
                ),
                Collections.emptySet(), ""
        );
        assertThat(calculatedSubscriptions).isEmpty();
    }


    @Test
    public void calculateCustomSubscriptionForNeighbour_emptyLocalSubscriptionGivesEmptySet() {
        Set<Capability> capabilities = Collections.singleton(getDatexCapability("NO"));
        Set<Subscription> calculatedSubscription = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                Collections.emptySet(),
                capabilities, ""
        );
        assertThat(calculatedSubscription).hasSize(0);
    }

    @Test
    public void calculateSingleLocalSubscriptionMatchingSingleNeighbourCapability() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                Collections.singleton(
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                ""
                        )
                ),
                Collections.singleton(
                        new Capability(
                            new DatexApplication(
                                    "NO0001",
                                    "",
                                    "NO",
                                    "1.0",
                                    List.of("0122"),
                                    "SituationPublication",
                                    "publisherName"
                            ), new Metadata(RedirectStatus.OPTIONAL)
                        )
                ), ""
        );
        assertThat(calculatedSubscriptions)
                .hasSize(1)
                .allMatch(s -> s.getSelector().equals("originatingCountry = 'NO'"));
    }

    @Test
    void calculateSingleLocalSubscriptionMatchingTwoNeighbourCapabiltities() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                Collections.singleton(
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                ""
                        )
                ),
                new HashSet<>(Arrays.asList(
                        new Capability(
                                new DatexApplication(
                                        "NO0001",
                                        "",
                                        "NO",
                                        "1.0",
                                        List.of("0122"),
                                        "SituationPublication",
                                        "publisherName"
                                ), new Metadata(RedirectStatus.OPTIONAL)
                        ),
                        new Capability(
                            new DenmApplication(
                                "NO0001",
                                "pub-123",
                                "NO",
                                "1.0",
                                List.of("0122"),
                                List.of(6)
                        ),  new Metadata(RedirectStatus.OPTIONAL))
                )), ""
        );
        assertThat(calculatedSubscriptions)
                .hasSize(1)
                .allMatch(s -> s.getSelector().equals("originatingCountry = 'NO'"));
    }

    @Test
    public void calculateTwoLocalSubscriptionsMatchingTheSameCapability() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                new HashSet<>(Arrays.asList(
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                ""
                        ),
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "messageType = 'DATEX2'",
                                ""
                        )
                )),
                Collections.singleton(
                        new Capability(
                                new DatexApplication(
                                        "NO0001",
                                        "",
                                        "NO",
                                        "1.0",
                                        List.of("0122"),
                                        "SituationPublication",
                                        "publisherName"
                                ), new Metadata(RedirectStatus.OPTIONAL)
                        )
                ), ""
        );
        assertThat(calculatedSubscriptions)
                .hasSize(2);
    }

    @Test
    public void calculateTowLocalSubscriptionsMatchingSeparateCapabilties() {
        Set<Subscription> calculatedSubscriptions = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                new HashSet<>(Arrays.asList(
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "originatingCountry = 'NO'",
                                ""
                        ),
                        new LocalSubscription(
                                LocalSubscriptionStatus.REQUESTED,
                                "messageType = 'DATEX2'",
                                ""
                        )
                )),
                new HashSet<>(Arrays.asList(
                        new Capability(
                                new DatexApplication(
                                        "NO0001",
                                        "pub-1",
                                        "NO",
                                        "1.0",
                                        List.of("0122"),
                                        "SituationPublication",
                                        "publisherName"
                                ), new Metadata(RedirectStatus.OPTIONAL)
                        ),
                        new Capability(
                                new DenmApplication(
                                        "NO0001",
                                        "pub-123",
                                        "NO",
                                        "1.0",
                                        List.of("0122"),
                                        List.of(6)
                                ),  new Metadata(RedirectStatus.OPTIONAL))
                )), ""
        );
        assertThat(calculatedSubscriptions)
                .hasSize(2);
    }


    @Test
    public void calculateCustomSubscriptionForNeighbour_localSubscriptionOriginatingCountryMatchesCapabilityOfNeighbourGivesLocalSubscription() {
        Set<LocalSubscription> localSubscriptions = new HashSet<>();
        localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"originatingCountry = 'NO'", ""));

        Set<Capability> capabilities = org.mockito.internal.util.collections.Sets.newSet(getDatexCapability("NO"));
        Set<Subscription> calculatedSubscription = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(localSubscriptions, capabilities, "");

        assertThat(calculatedSubscription).hasSize(1);
        assertThat(calculatedSubscription.iterator().next().getSelector()).isEqualTo("originatingCountry = 'NO'");
    }

    @Test
    public void calculateCustomSubscriptionForNeighbour_localSubscriptionMessageTypeAndOriginatingCountryMatchesCapabilityOfNeighbourGivesLocalSubscription() {
        Set<LocalSubscription> localSubscriptions = new HashSet<>();
        localSubscriptions.add(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'DATEX2' AND originatingCountry = 'NO'", ""));

        Set<Capability> capabilities = Collections.singleton(getDatexCapability("NO"));
        Set<Subscription> calculatedSubscription = SubscriptionCalculator.calculateCustomSubscriptionForNeighbour(
                localSubscriptions,
                capabilities, "");

        assertThat(calculatedSubscription).hasSize(1);
        assertThat(calculatedSubscription.iterator().next().getSelector())
                .contains("originatingCountry = 'NO'")
                .contains(" AND ")
                .contains("messageType = 'DATEX2'");
    }

    private Capability getDatexCapability(String country) {
        return new Capability(
                new DatexApplication(country + "-123", country + "-pub", country, "1.0", List.of("0122"), "SituationPublication", "publisherName"),
                new Metadata(RedirectStatus.OPTIONAL));
    }

}
