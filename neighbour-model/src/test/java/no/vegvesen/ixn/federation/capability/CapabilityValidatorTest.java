package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityValidatorTest {

    @Test
    public void testDatexCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        Collections.singleton("12001"),
                        "SituationPublication"
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testDatexCapabilityWithoutPublicationTypeIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        Collections.singleton("12001"),
                        ""
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        Collections.singleton("12001"),
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testDenmCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("12001"),
                        Collections.singleton(6)
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testDenmCapabilityWithoutCauseCodeIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("12001"),
                        Collections.emptySet()
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.singleton("12001"),
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testIvimCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testSpatemCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new SpatemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SPATEM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testMapemCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new MapemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "MAPEM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testSsemCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new SsemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SSEM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testSremCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new SremApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SREM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testCamCapabilityIsValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new CamApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "CAM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isTrue();
    }

    @Test
    public void testCapabilityWithoutPublisherIdIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new IvimApplicationApi(
                        null,
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testCapabilityWithoutPublicationIdIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "",
                        "NO",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        null,
                        "NO",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testCapabilityWithoutOriginatingCountryIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "",
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        null,
                        "IVIM",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testCapabilityWithoutProtocolVersionIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "",
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        null,
                        Collections.singleton("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }

    @Test
    public void testCapabilityWithoutQuadTreeTilesIsNotValid() {
        CapabilitySplitApi capability = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        Collections.emptySet()
                ),
                new MetadataApi()
        );

        CapabilitySplitApi capabilityNull = new CapabilitySplitApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isFalse();
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).isFalse();
    }
}
