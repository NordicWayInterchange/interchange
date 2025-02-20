package no.vegvesen.ixn.federation.capability;

import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CapabilityValidatorTest {

    @Test
    public void testDatexCapabilityWithMissingPublisherName(){
        CapabilityApi capabilityApi = new CapabilityApi(
                new DatexApplicationApi(
                        "N000000",
                        "N000000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
                        "",
                        "IVIM",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
                        "NO",
                        "",
                        List.of("12001")
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
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
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        List.of()
                ),
                new MetadataApi()
        );

        CapabilityApi capabilityNull = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        null
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityIsValid(capability)).contains("quadTree");
        assertThat(CapabilityValidator.capabilityIsValid(capabilityNull)).contains("quadTree");
    }

    @Test
    public void testCapabilityWithInvalidCharactersInPropertiesIsNotValid(){
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "'NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        List.of("1")
                ),
                new MetadataApi()
        );

        CapabilityApi capability2 = new CapabilityApi(
                new IvimApplicationApi(
                        " NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        List.of("2")
                ),
                new MetadataApi()
        );
        CapabilityApi capability3 = new CapabilityApi(
                new IvimApplicationApi(
                        "*NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        List.of("1")
                ),
                new MetadataApi()
        );

        assertThat(CapabilityValidator.capabilityHasValidProperties(capability1)).containsKey(false);
        assertThat(CapabilityValidator.capabilityHasValidProperties(capability2)).containsKey(false);
        assertThat(CapabilityValidator.capabilityHasValidProperties(capability3)).containsKey(false);
    }

    @Test
    public void testCapabilityWithQuadTreeTileExceedingCharacterLimit(){
        StringBuilder quadTile = new StringBuilder();
        while (quadTile.length() < 300){
            quadTile.append("1");
        }
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "'NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        List.of(quadTile.toString())
                ),
                new MetadataApi()
        );
        assertThat(CapabilityValidator.capabilityHasValidProperties(capability1)).containsKey(false);
    }

    @Test
    public void testCapabilityWithQuadTreeInsideLimitButTotalExceeds255(){
        List<String> quadTree = new ArrayList<>();
        while(quadTree.size() < 300){
            quadTree.add("1");
        }
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
                        "NO",
                        "IVIM",
                        quadTree
                ),
                new MetadataApi()
        );
        assertThat(CapabilityValidator.capabilityHasValidProperties(capability1)).doesNotContainKey(false);
    }

    @Test
    public void testCapabilityWithInvalidPublicationId(){
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "pub-1",
                        "NO",
                        "IVIM",
                        List.of("1")
                ),
                new MetadataApi()
        );

        CapabilityException thrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
        Assertions.assertEquals("INVALID_PUBLICATION_ID_PREFIX", thrown.getErrorCode());
        Assertions.assertEquals("publicationId must start with '<publisherId>:'", thrown.getMessage());

    }

    @Test
    public void testCapabilityWithInvalidCountryCode(){
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
                        "no",
                        "IVIM",
                        List.of("1")
                ),
                new MetadataApi()
        );
        CapabilityException thrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });

        Assertions.assertEquals("INVALID_COUNTRY_CODE", thrown.getErrorCode());
        Assertions.assertEquals("'no' is not a valid country code", thrown.getMessage());

        capability1.getApplication().setOriginatingCountry("No");
        CapabilityException secondThrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
        Assertions.assertEquals("INVALID_COUNTRY_CODE", secondThrown.getErrorCode());
        Assertions.assertEquals("'No' is not a valid country code", secondThrown.getMessage());

        capability1.getApplication().setOriginatingCountry("N");
        CapabilityException thirdThrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
        Assertions.assertEquals("INVALID_COUNTRY_CODE", thirdThrown.getErrorCode());
        Assertions.assertEquals("'N' is not a valid country code", thirdThrown.getMessage());

        capability1.getApplication().setOriginatingCountry("NOK");
        CapabilityException FourthThrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
        Assertions.assertEquals("INVALID_COUNTRY_CODE", FourthThrown.getErrorCode());
        Assertions.assertEquals("'NOK' is not a valid country code", FourthThrown.getMessage());


        capability1.getApplication().setOriginatingCountry("NO");
        assertDoesNotThrow(() -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
    }

    @Test
    public void testCapabilityWithPropertyExceedingCharLimitIsNotValid(){
        StringBuilder sb = new StringBuilder();
        while(sb.length() < 280){
            sb.append("l");
        }
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000:pub-1",
                        "NO",
                        sb.toString(),
                        List.of("1")
                ),
                new MetadataApi()
        );
        assertThat(CapabilityValidator.capabilityHasValidProperties(capability1)).containsKey(false);
    }

    @Test
    public void testCapabilityWithInvalidPublisherId(){
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO0000",
                        "NO0000:pub-1",
                        "NO",
                        "test",
                        List.of("1")
                ),
                new MetadataApi()
        );
        CapabilityException thrown = assertThrows(CapabilityException.class, () -> {
            CapabilityValidator.capabilityHasValidProperties(capability1);
        });
        Assertions.assertEquals("INVALID_PUBLISHER_ID_FORMAT", thrown.getErrorCode());
        Assertions.assertEquals("publisherId must contain exactly two uppercase letters followed by five digits in the format <country code><5 numbers>", thrown.getMessage());
    }

    @Test
    public void testCapabilityWithInvalidShardCountIsNotValid(){
        CapabilityApi capability = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO00000-pub-1",
                        "NO",
                        "IVIM",
                        List.of()
                ),
                new MetadataApi(11, "test", RedirectStatusApi.OPTIONAL, 1, 1, 1)
        );
        assertThat(CapabilityValidator.isShardCountValid(capability.getMetadata())).isFalse();
    }

    @Test
    public void testCapabilityValidWithValidShardCount() {
        CapabilityApi datexNO = new CapabilityApi(
                new DatexApplicationApi(
                        "NO00000",
                        "NO00000:NO-pub-1",
                         "NO",
                          "1.0",
                           List.of("12003"),
                            "SituationPublication",
                             "publisherName"
                ),
                new MetadataApi(5,
                        "test",
                         RedirectStatusApi.OPTIONAL,
                          1,
                           1,
                            1
                )
        );
        assertThat(CapabilityValidator.capabilityHasValidProperties(datexNO)).containsKey(true);
    }
}
