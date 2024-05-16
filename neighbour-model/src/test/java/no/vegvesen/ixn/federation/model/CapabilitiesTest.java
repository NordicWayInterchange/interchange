package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesTest {

    @Test
    public void testAddAllDatatypesRetainsExistingObjects() {
        CapabilitySplit firstCapability = new CapabilitySplit(new DenmApplication("NO00000", "pub-1", "NO", "DENM:1.2.2", List.of("1"), List.of(1)), new Metadata(RedirectStatus.OPTIONAL));
        firstCapability.setId(1);
        CapabilitySplit secondCapability = new CapabilitySplit(new DenmApplication("NO00001", "pub-2", "NO", "DENM:1.2.2", List.of("2"), List.of(2)), new Metadata(RedirectStatus.OPTIONAL));
        secondCapability.setId(2);
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                new HashSet<>(Arrays.asList(
                        firstCapability,
                        secondCapability
                )));
        capabilities.replaceCapabilities(Collections.singleton(new CapabilitySplit(new DenmApplication("NO00000", "pub-1", "NO", "DENM:1.2.2", List.of("1"), List.of(1)), new Metadata(RedirectStatus.OPTIONAL))));
        assertThat(capabilities.getCapabilities()).hasSize(1);
        //Test that the original object is the one retained
        assertThat(capabilities.getCapabilities().stream().findFirst().get().getId()).isEqualTo(1);

    }

    @Test
    public void testAddingNewCapabilityToExistingSetReplacesIt() {

        CapabilitySplit firstCapability = new CapabilitySplit(new DenmApplication("NO00000", "pub-1", "NO", "DENM:1.2.2", List.of("1"), List.of(1)), new Metadata());
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(
                        firstCapability
                ));
        CapabilitySplit secondCapability = new CapabilitySplit(new DenmApplication("NO00001", "pub-1", "NO", "DENM:1.2.2", List.of("2"), List.of(2)), new Metadata());
        capabilities.replaceCapabilities(Collections.singleton(secondCapability));
        assertThat(capabilities.getCapabilities()).hasSize(1);
        assertThat(capabilities.getCapabilities().stream().findFirst().get()).isEqualTo(secondCapability);
    }

    @Test
    public void testAddingSeveralCapabilitiesToSingeltonSet() {

        CapabilitySplit firstCapability = new CapabilitySplit(new DenmApplication("NO00000", "pub-1", "NO", "DENM:1.2.2", List.of("1"), List.of(1)), new Metadata());
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(
                        firstCapability
                ));
        CapabilitySplit secondCapability = new CapabilitySplit(new DenmApplication("NO00001", "pub-1", "NO", "DENM:1.2.2", List.of("2"), List.of(2)), new Metadata());
        CapabilitySplit thirdCapability = new CapabilitySplit(new DenmApplication("NO00002", "pub-1", "NO", "DENM:1.2.2", List.of("3"), List.of(3)), new Metadata());

        capabilities.replaceCapabilities(new HashSet<>(Arrays.asList(secondCapability,thirdCapability)));
        assertThat(capabilities.getCapabilities()).hasSize(2);
        assertThat(capabilities.getCapabilities().contains(firstCapability)).isFalse();
    }

    @Test
    public void testAddingSeveralCapabilitiesToEmptySet() {
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.emptySet());
        CapabilitySplit firstCapability = new CapabilitySplit(new DenmApplication("NO00000", "pub-1", "NO", "DENM:1.2.2", List.of("1"), List.of(1)), new Metadata());
        CapabilitySplit secondCapability = new CapabilitySplit(new DenmApplication("NO00001", "pub-1", "NO", "DENM:1.2.2", List.of("2"), List.of(2)), new Metadata());

        capabilities.replaceCapabilities(new HashSet<>(Arrays.asList(firstCapability,secondCapability)));
        assertThat(capabilities.getCapabilities()).hasSize(2);
    }
}
