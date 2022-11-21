package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityVerifierTest {

    public static final String PUBLISHER_ID = "publisher-1";
    public static final String ORIGINATING_COUNTRY = "NO";
    public static final String PROTOCOL_VERSION = "1.0";
    public static final Set<String> QUAD_TREE = new HashSet<>(Arrays.asList("01220123", "01230121"));

    //Datex2 property
    public static final Set<String> PUBLICATION_TYPE = new HashSet<>(Arrays.asList("1", "2"));

    //Denm property
    public static final Set<String> CAUSE_CODE = new HashSet<>(Arrays.asList("5", "6"));

    @Test
    public void testDatex2IsOk() {
        DatexCapabilityApi capability = new DatexCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE, PUBLICATION_TYPE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testDenmIsOk() {
        DenmCapabilityApi capability = new DenmCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE, CAUSE_CODE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testIvimIsOk() {
        IvimCapabilityApi capability = new IvimCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testSpatemIsOk() {
        SpatemCapabilityApi capability = new SpatemCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testMapemIsOk() {
        MapemCapabilityApi capability = new MapemCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testSremIsOk() {
        SremCapabilityApi capability = new SremCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testSsemIsOk() {
        SsemCapabilityApi capability = new SsemCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE);
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testCamIsOk() {
        CamCapabilityApi capability = new CamCapabilityApi(PUBLISHER_ID, ORIGINATING_COUNTRY, PROTOCOL_VERSION, QUAD_TREE, new HashSet<>(Arrays.asList("1", "2")));
        assertThat(CapabilityVerifier.isValid(capability)).isTrue();
    }

    @Test
    public void testDatex2ForMissingProperty() {
        DatexCapabilityApi capability = new DatexCapabilityApi();
    }
}

