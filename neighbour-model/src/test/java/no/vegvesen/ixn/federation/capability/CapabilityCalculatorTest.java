package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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

        CapabilitySplit a = new CapabilitySplit(new DatexApplication("SE-213", "se-pub", "SE", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata());
        CapabilitySplit b = new CapabilitySplit(new DatexApplication("FI-213", "fi-pub", "FI", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata());
        CapabilitySplit c = new CapabilitySplit(new DatexApplication("NO-213", "no-pub", "NO", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata());

        ServiceProvider firstServiceProvider = new ServiceProvider();
        firstServiceProvider.setName("First Service Provider");
        Capabilities firstServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(a, b).collect(Collectors.toSet()));
        firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

        ServiceProvider secondServiceProvider = new ServiceProvider();
        secondServiceProvider.setName("Second Service Provider");
        Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(b, c).collect(Collectors.toSet()));
        secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

        Set<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());

        Set<CapabilitySplit> selfCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);

        assertThat(selfCapabilities).hasSize(3);
        assertThat(selfCapabilities).containsAll(Stream.of(a, b, c).collect(Collectors.toSet()));
    }

    @Test
    void calculateLastUpdateCapabilitiesEmpty() {
        ServiceProvider serviceProvider = new ServiceProvider();
        LocalDateTime localDateTime = CapabilityCalculator.calculateLastUpdatedCreatedCapabilities(Arrays.asList(serviceProvider));
        assertThat(localDateTime).isNull();
    }

    @Test
    void calculateLastUpdatedCapabilitiesOneCap() {
        ServiceProvider serviceProvider = new ServiceProvider();

        CapabilitySplit capability = new CapabilitySplit(
                new DatexApplication("NO-123", "no-pub", "NO", "1.0", Collections.emptySet(), "SituationPublication"),
                new Metadata()
        );
        capability.setLastUpdated(LocalDateTime.now());
        capability.setStatus(CapabilityStatus.CREATED);

        Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(capability));

        serviceProvider.setCapabilities(capabilities);
        LocalDateTime result = CapabilityCalculator.calculateLastUpdatedCreatedCapabilities(Arrays.asList(serviceProvider));
        assertThat(result).isEqualTo(capability.getLastUpdated().get());
    }

    @Test
    void calculateLastUpdatedCapabilitiesTwoDates() {
        LocalDateTime earliest = LocalDateTime.now();
        LocalDateTime latest = LocalDateTime.now().plusMinutes(5);

        CapabilitySplit earliestCap = new CapabilitySplit(
                new DatexApplication("NO-123", "no-pub-1", "NO", "1.0", Collections.emptySet(), "SituationPublication"),
                new Metadata()
        );
        earliestCap.setLastUpdated(earliest);
        earliestCap.setStatus(CapabilityStatus.CREATED);

        Capabilities earliestCaps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(earliestCap)
        );

        CapabilitySplit latestCap = new CapabilitySplit(
                new DatexApplication("NO-123", "no-pub-2", "NO", "1.0", Collections.emptySet(), "SituationPublication"),
                new Metadata()
        );
        latestCap.setLastUpdated(latest);
        latestCap.setStatus(CapabilityStatus.CREATED);

        Capabilities latestCaps = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(latestCap)
        );

        ServiceProvider earliestSP = new ServiceProvider();
        earliestSP.setCapabilities(earliestCaps);

        ServiceProvider latestSP = new ServiceProvider();
        latestSP.setCapabilities(latestCaps);

        assertThat(CapabilityCalculator.calculateLastUpdatedCreatedCapabilities(Arrays.asList(latestSP,earliestSP))).isEqualTo(latest);
    }

    @Test
    void calculateGetLastUpdatedLocalCapabilities() {
        LocalDateTime aCapDate = LocalDateTime.now();
        CapabilitySplit aCap1 = getDatexCapability("SE");
        aCap1.setLastUpdated(aCapDate);
        aCap1.setStatus(CapabilityStatus.CREATED);
        CapabilitySplit aCap2 = getDatexCapability("SE");
        aCap2.setLastUpdated(aCapDate);
        aCap2.setStatus(CapabilityStatus.CREATED);

        ServiceProvider aServiceProvider = new ServiceProvider();
        aServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(aCap1, aCap2)));

        LocalDateTime bCapDate = LocalDateTime.now().plusMinutes(5);
        CapabilitySplit bCap1 = getDatexCapability("SE");
        bCap1.setLastUpdated(bCapDate);
        bCap1.setStatus(CapabilityStatus.CREATED);
        CapabilitySplit bCap2 = getDatexCapability("SE");
        bCap2.setLastUpdated(bCapDate);
        bCap2.setStatus(CapabilityStatus.CREATED);

        ServiceProvider bServiceProvider = new ServiceProvider();
        bServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(bCap1, bCap2)));

        LocalDateTime cCapDate = LocalDateTime.now();
        CapabilitySplit cCap1 = getDatexCapability("FI");
        cCap1.setLastUpdated(cCapDate);
        cCap1.setStatus(CapabilityStatus.CREATED);
        CapabilitySplit cCap2 = getDatexCapability("FI");
        cCap2.setStatus(CapabilityStatus.CREATED);
        cCap2.setLastUpdated(cCapDate);

        ServiceProvider cServiceProvider = new ServiceProvider();
        cServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Sets.newLinkedHashSet(cCap1, cCap2)));

        List<ServiceProvider> serviceProviders = Stream.of(aServiceProvider, bServiceProvider, cServiceProvider).collect(Collectors.toList());

        LocalDateTime lastUpdatedCapabilities = CapabilityCalculator.calculateLastUpdatedCreatedCapabilities(serviceProviders);

        assertThat(lastUpdatedCapabilities).isEqualTo(bCapDate);
    }

    private CapabilitySplit getDatexCapability(String originatingCountry) {
        return new CapabilitySplit(new DatexApplication(originatingCountry + "-123", originatingCountry + "-pub", originatingCountry, "1.0", Collections.emptySet(), "SituationPublication"), new Metadata());
    }

}
