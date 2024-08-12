package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityValidatorTest {

    @Test
    public void testDatexCapabilityWithMissingPublisherName(){
        CapabilityApi capabilityApi = new CapabilityApi(
                new DatexApplicationApi(
                        "N000000",
                        "N000000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        List.of("12001"),
                        "SituationPublication",
                        null
                ),
                new MetadataApi()
        );
        assertThat(CapabilityValidator.capabilityIsValid(capabilityApi)).isNotEmpty();
    }
    @Test
    public void testDatexCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        List.of("12001"),
                        "SituationPublication",
                        "publisherName"
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testDatexCapabilityWithoutPublicationTypeIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        List.of("12001"),
                        "",
                        "publisherName"
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DATEX2:3.2",
                        List.of("12001"),
                        null,
                        "publisherName"
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("publicationType");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("publicationType");
    }

    @Test
    public void testDenmCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12001"),
                        List.of(6)
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testDenmCapabilityWithoutCauseCodeIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12001"),
                        List.of()
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new DenmApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of("12001"),
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("causeCode");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("causeCode");
    }

    @Test
    public void testIvimCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testSpatemCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new SpatemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SPATEM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testMapemCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new MapemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "MAPEM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testSsemCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new SsemApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SSEM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testSremCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new SremApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "SREM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testCamCapabilityIsValid() {
        CapabilityApi capability = new CapabilityApi(
                new CamApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "CAM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).isEmpty();
    }

    @Test
    public void testCapabilityWithoutPublisherIdIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        null,
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("publisherId");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("publisherId");
    }

    @Test
    public void testCapabilityWithoutPublicationIdIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "",
                        "NO",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        null,
                        "NO",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("publicationId");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("publicationId");
    }

    @Test
    public void testCapabilityWithoutOriginatingCountryIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        null,
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("originatingCountry");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("originatingCountry");
    }

    @Test
    public void testCapabilityWithoutProtocolVersionIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        null,
                        List.of("12001")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("protocolVersion");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("protocolVersion");
    }

    @Test
    public void testCapabilityWithoutQuadTreeTilesIsNotValid() {
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        List.of()
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("quadTree");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("quadTree");
    }
}
