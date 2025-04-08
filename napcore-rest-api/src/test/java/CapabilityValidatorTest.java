import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.IvimApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.CapabilityValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CapabilityValidatorTest {

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
        Map<String, String> validator = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertThat(validator).isNotEmpty();
        assertTrue(validator.containsKey("INVALID_PUBLICATION_ID_PREFIX"));
        assertEquals("publicationId must start with '<publisherId>:'", validator.get("INVALID_PUBLICATION_ID_PREFIX"));
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
        Map<String, String> validator1 = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertTrue(validator1.containsKey("INVALID_COUNTRY_CODE"));
        assertEquals("'no' is not a valid country code", validator1.get("INVALID_COUNTRY_CODE"));

    }

}
