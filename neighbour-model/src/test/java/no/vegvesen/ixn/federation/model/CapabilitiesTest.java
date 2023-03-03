package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesTest {

    @Test
    public void testAddAllDatatypesRetainsExistingObjects() {
        DenmCapability firstCapability = new DenmCapability("NO00000", "NO", "DENM:1.2.2", Collections.singleton("1"), Collections.singleton("1"));
        firstCapability.setId(1);
        DenmCapability secondCapability = new DenmCapability("NO00001", "NO", "DENM:1.2.2", Collections.singleton("2"), Collections.singleton("2"));
        secondCapability.setId(2);
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                new HashSet<>(Arrays.asList(
                        firstCapability,
                        secondCapability
                )));
        capabilities.replaceCapabilities(Collections.singleton(new DenmCapability("NO00000", "NO", "DENM:1.2.2", Collections.singleton("1"), Collections.singleton("1"))));
        assertThat(capabilities.getCapabilities()).hasSize(1);
        //Test that the original object is the one retained
        assertThat(capabilities.getCapabilities().stream().findFirst().get().getId()).isEqualTo(1);

    }

    @Test
    public void testAddingNewCapabilityToExistingSetReplacesIt() {

        DenmCapability firstCapability = new DenmCapability("NO00000", "NO", "DENM:1.2.2", Collections.singleton("1"), Collections.singleton("1"));
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(
                        firstCapability
                ));
        DenmCapability secondCapability = new DenmCapability("NO00001", "NO", "DENM:1.2.2", Collections.singleton("2"), Collections.singleton("2"));
        capabilities.replaceCapabilities(Collections.singleton(secondCapability));
        assertThat(capabilities.getCapabilities()).hasSize(1);
        assertThat(capabilities.getCapabilities().stream().findFirst().get()).isEqualTo(secondCapability);
    }

    @Test
    public void testAddingSeveralCapabilitiesToSingeltonSet() {

        DenmCapability firstCapability = new DenmCapability("NO00000", "NO", "DENM:1.2.2", Collections.singleton("1"), Collections.singleton("1"));
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.singleton(
                        firstCapability
                ));
        DenmCapability secondCapability = new DenmCapability("NO00001", "NO", "DENM:1.2.2", Collections.singleton("2"), Collections.singleton("2"));
        DenmCapability thirdCapability = new DenmCapability("NO00002", "NO", "DENM:1.2.2", Collections.singleton("3"), Collections.singleton("3"));

        capabilities.replaceCapabilities(new HashSet<>(Arrays.asList(secondCapability,thirdCapability)));
        assertThat(capabilities.getCapabilities()).hasSize(2);
        assertThat(capabilities.getCapabilities().contains(firstCapability)).isFalse();
    }

    @Test
    public void testAddingSeveralCapabilitiesToEmptySet() {
        Capabilities capabilities = new Capabilities(
                Capabilities.CapabilitiesStatus.KNOWN,
                Collections.emptySet());
        DenmCapability firstCapability = new DenmCapability("NO00000", "NO", "DENM:1.2.2", Collections.singleton("1"), Collections.singleton("1"));
        DenmCapability secondCapability = new DenmCapability("NO00001", "NO", "DENM:1.2.2", Collections.singleton("2"), Collections.singleton("2"));

        capabilities.replaceCapabilities(new HashSet<>(Arrays.asList(firstCapability,secondCapability)));
        assertThat(capabilities.getCapabilities()).hasSize(2);
    }
}
