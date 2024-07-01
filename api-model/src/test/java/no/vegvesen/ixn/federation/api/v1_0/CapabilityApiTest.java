package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import org.junit.jupiter.api.Test;

import java.util.List;


public class CapabilityApiTest {

    @Test
    public void denmCapability() throws JsonProcessingException {
        CapabilityApi capabilityApi = new CapabilityApi(
                new DenmApplicationApi(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        List.of(),
                        List.of(6)
                ),
                new MetadataApi()
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilityApi));
    }

}
