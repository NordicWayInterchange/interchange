package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.MapemCapabilityApi;
import no.vegvesen.ixn.federation.model.MapemCapability;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {
    @Test
    public void testTransformMapemCapability() {
        MapemCapability capability = new MapemCapability(
                "NO-123",
                "NO",
                "1.0",
                Collections.emptySet(),
                Collections.emptySet()
        );

        CapabilityApi mapemCapabilityApi = capability.toApi();
        assertThat(mapemCapabilityApi.getMessageType()).isEqualTo("MAPEM");

    }

}
