import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.IvimApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.CapabilityValidator;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        List<String> validator = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertEquals(1, validator.size());

        String content = validator.getFirst();
        assertEquals("Reason: INVALID_PUBLICATION_ID_PREFIX, Message: publicationId must start with '<publisherId>:'", content);
    }

    @Test
    public void testCapabilityWithInvalidCountryCode(){
        CapabilityApi capability1 = new CapabilityApi(
                new IvimApplicationApi(
                        "NO00000",
                        "NO:pub-1",
                        "no",
                        "IVIM",
                        List.of("1")
                ),
                new MetadataApi()
        );
        List<String> validator = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertEquals(2, validator.size());

        String first = validator.getFirst();
        String second = validator.get(1);
        assertEquals("Reason: INVALID_COUNTRY_CODE, Message: 'no' is not a valid country code", first);
        assertEquals("Reason: INVALID_PUBLICATION_ID_PREFIX, Message: publicationId must start with '<publisherId>:'", second);
    }

}
