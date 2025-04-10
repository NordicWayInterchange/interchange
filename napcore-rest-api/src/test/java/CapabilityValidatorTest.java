import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.IvimApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.napcore.model.CapabilityErrorCode;
import no.vegvesen.ixn.napcore.model.CapabilityErrorMessage;
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
        List<CapabilityErrorMessage> validator = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertEquals(1, validator.size());
        CapabilityErrorCode code = validator.getFirst().getCode();
        CapabilityErrorCode expectedEnum = CapabilityErrorCode.INVALID_PUBLICATION_ID_PREFIX;
        String message = validator.getFirst().getMessage();
        assertEquals(expectedEnum, code);
        assertEquals("publicationId must start with '<publisherId>:'", message);
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
        List<CapabilityErrorMessage> validator = CapabilityValidator.napcoreCapabilityHasValidProperties(capability1);
        assertEquals(2, validator.size());
        CapabilityErrorCode firstCode = validator.getFirst().getCode();
        CapabilityErrorCode firstExpectedEnum = CapabilityErrorCode.INVALID_COUNTRY_CODE;
        String firstMessage = validator.getFirst().getMessage();


        CapabilityErrorCode secondCode = validator.getLast().getCode();
        CapabilityErrorCode secondExpectedEnum = CapabilityErrorCode.INVALID_PUBLICATION_ID_PREFIX;
        String secondMessage = validator.getLast().getMessage();

        assertEquals(firstCode, firstExpectedEnum);
        assertEquals(secondCode, secondExpectedEnum);
        assertEquals("'no' is not a valid country code", firstMessage);
        assertEquals("publicationId must start with '<publisherId>:'", secondMessage);
    }

}
