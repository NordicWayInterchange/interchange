package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.ServiceProvider;
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

        Capability a = new DatexCapability(null, "SE", null, null, null);
        Capability b = new DatexCapability(null, "FI", null, null, null);
        Capability c = new DatexCapability(null, "NO", null, null, null);

        ServiceProvider firstServiceProvider = new ServiceProvider();
        firstServiceProvider.setName("First Service Provider");
        Capabilities firstServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(a, b).collect(Collectors.toSet()));
        firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

        ServiceProvider secondServiceProvider = new ServiceProvider();
        secondServiceProvider.setName("Second Service Provider");
        Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(b, c).collect(Collectors.toSet()));
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
        Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Sets.newLinkedHashSet(new DatexCapability(null,
                        "NO",
                        null,
                        null,
                        null)),
                lastUpdated);
        serviceProvider.setCapabilities(capabilities);
        LocalDateTime result = CapabilityCalculator.calculateLastUpdatedCapabilities(Arrays.asList(serviceProvider));
        assertThat(result).isEqualTo(lastUpdated);
    }

    @Test
    void calculateLastUpdatedCapabilitiesTwoDates() {
        LocalDateTime earliest = LocalDateTime.of(2021, Month.DECEMBER,3,0,0);
        LocalDateTime latest = LocalDateTime.of(2021,Month.DECEMBER,4,0,0);
        Capabilities earliestCap = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DatexCapability(null,
                        "NO",
                        null,
                        null,
                        null)),
                earliest);
        Capabilities latestCap = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(new DatexCapability(null,
                        "NO",
                        null,
                        null,
                        null)),
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
        aServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(aCap1, aCap2), aCapDate));

        LocalDateTime bCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 2);
        Capability bCap1 = getDatexCapability("SE");
        Capability bCap2 = getDatexCapability("SE");
        ServiceProvider bServiceProvider = new ServiceProvider();
        bServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(bCap1, bCap2), bCapDate));

        LocalDateTime cCapDate = LocalDateTime.of(1999, Month.APRIL, 1, 1, 1, 1, 0);
        Capability cCap1 = getDatexCapability("FI");
        Capability cCap2 = getDatexCapability("FI");
        ServiceProvider cServiceProvider = new ServiceProvider();
        cServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(cCap1, cCap2), cCapDate));

        List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider, cServiceProvider).collect(Collectors.toList());


        LocalDateTime lastUpdatedCapabilities = CapabilityCalculator.calculateLastUpdatedCapabilities(serviceProviders);

        assertThat(lastUpdatedCapabilities).isEqualTo(bCapDate);
    }

    private Capability getDatexCapability(String originatingCountry) {
        return new DatexCapability(null, originatingCountry, null, Collections.emptySet(), Collections.emptySet());
    }

}
