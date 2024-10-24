package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityCalculatorTest {
    @Test
    void calculateSelfCapabilitiesTest() {

        Capability a = new Capability(new DatexApplication("SE-213", "se-pub", "SE", "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata());
        Capability b = new Capability(new DatexApplication("FI-213", "fi-pub", "FI", "1.0", List.of(), "SituationPublication","publisherName"), new Metadata());
        Capability c = new Capability(new DatexApplication("NO-213", "no-pub", "NO", "1.0", List.of(), "SituationPublication","publisherName"), new Metadata());

        ServiceProvider firstServiceProvider = new ServiceProvider();
        firstServiceProvider.setName("First Service Provider");
        Capabilities firstServiceProviderCapabilities = new Capabilities(Stream.of(a, b).collect(Collectors.toSet()));
        firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

        ServiceProvider secondServiceProvider = new ServiceProvider();
        secondServiceProvider.setName("Second Service Provider");
        Capabilities secondServiceProviderCapabilities = new Capabilities(Stream.of(b, c).collect(Collectors.toSet()));
        secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

        Set<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());

        Set<Capability> selfCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);

        assertThat(selfCapabilities).hasSize(3);
        assertThat(selfCapabilities).containsAll(Stream.of(a, b, c).collect(Collectors.toSet()));
    }

    @Test
    void calculateLastUpdateCapabilitiesEmpty() {
        ServiceProvider serviceProvider = new ServiceProvider();
        LocalDateTime localDateTime = CapabilityCalculator.calculateLastUpdatedCapabilities(Arrays.asList(serviceProvider));
        assertThat(localDateTime).isNull();
    }

    @Test
    void calculateLastUpdatedCapabiltiesOneCap() {
        ServiceProvider serviceProvider = new ServiceProvider();
        LocalDateTime lastUpdated = LocalDateTime.now();
        Capabilities capabilities = new Capabilities(
                Sets.newLinkedHashSet(new Capability(
                        new DatexApplication(
                                "NO-123",
                                "no-pub",
                                "NO",
                                "1.0",
                                List.of(),
                                "SituationPublication",
                                "publisherName"),
                        new Metadata())),
                lastUpdated);
        serviceProvider.setCapabilities(capabilities);
        LocalDateTime result = CapabilityCalculator.calculateLastUpdatedCapabilities(Arrays.asList(serviceProvider));
        assertThat(result).isEqualTo(lastUpdated);
    }

    @Test
    void calculateLastUpdatedCapabilitiesTwoDates() {
        LocalDateTime earliest = LocalDateTime.of(2021, Month.DECEMBER,3,0,0);
        LocalDateTime latest = LocalDateTime.of(2021,Month.DECEMBER,4,0,0);
        Capabilities earliestCap = new Capabilities(
                Collections.singleton(new Capability(
                        new DatexApplication(
                                "NO-123",
                                "no-pub-1",
                                "NO",
                                "1.0",
                                List.of(),
                                "SituationPublication",
                                "publisherName"),
                        new Metadata())),
                earliest);
        Capabilities latestCap = new Capabilities(
                Collections.singleton(new Capability(
                        new DatexApplication(
                                "NO-123",
                                "no-pub-2",
                                "NO",
                                "1.0",
                                List.of(),
                                "SituationPublication",
                                "publisherName"),
                        new Metadata())),
                latest);
        ServiceProvider earliestSP = new ServiceProvider();
        earliestSP.setCapabilities(earliestCap);
        ServiceProvider latestSP = new ServiceProvider();
        latestSP.setCapabilities(latestCap);
        assertThat(CapabilityCalculator.calculateLastUpdatedCapabilities(Arrays.asList(latestSP,earliestSP))).isEqualTo(latest);
    }

    @Test
    void calculateGetLastUpdatedLocalCapabilities() {
        LocalDateTime aCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
        Capability aCap1 = getDatexCapability("SE");
        Capability aCap2 = getDatexCapability("SE");
        ServiceProvider aServiceProvider = new ServiceProvider();
        aServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(aCap1, aCap2), aCapDate));

        LocalDateTime bCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
        Capability bCap1 = getDatexCapability("SE");
        Capability bCap2 = getDatexCapability("SE");
        ServiceProvider bServiceProvider = new ServiceProvider();
        bServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(bCap1, bCap2), bCapDate));

        LocalDateTime cCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
        Capability cCap1 = getDatexCapability("FI");
        Capability cCap2 = getDatexCapability("FI");
        ServiceProvider cServiceProvider = new ServiceProvider();
        cServiceProvider.setCapabilities(new Capabilities(Sets.newLinkedHashSet(cCap1, cCap2), cCapDate));

        List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider, cServiceProvider).collect(Collectors.toList());


        LocalDateTime lastUpdatedCapabilities = CapabilityCalculator.calculateLastUpdatedCapabilities(serviceProviders);

        assertThat(lastUpdatedCapabilities).isEqualTo(bCapDate);
    }

    private Capability getDatexCapability(String originatingCountry) {
        return new Capability(new DatexApplication(originatingCountry + "-123", originatingCountry + "-pub", originatingCountry, "1.0", List.of(), "SituationPublication", "publisherName"), new Metadata());
    }

}
