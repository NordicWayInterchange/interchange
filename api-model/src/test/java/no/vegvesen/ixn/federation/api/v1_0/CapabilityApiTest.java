package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DenmApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import org.junit.jupiter.api.Test;

import java.util.Collections;


public class CapabilityApiTest {

    @Test
    public void denmCapability() throws JsonProcessingException {
        CapabilitySplitApi capabilityApi = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO-123",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        Collections.emptySet(),
                        Collections.singleton(6)
                ),
                new MetadataApi()
        );
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(capabilityApi));
    }

}
