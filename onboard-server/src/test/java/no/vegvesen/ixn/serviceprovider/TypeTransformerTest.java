package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.MapemCapability;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
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
                Collections.emptySet()
        );

        CapabilityApi mapemCapabilityApi = capability.toApi();
        assertThat(mapemCapabilityApi.getMessageType()).isEqualTo("MAPEM");

    }

    @Test
    public void testAddSubscriptionWithEmptyConsumerCommonName() {
        AddSubscription subscription = new AddSubscription("originatingCountry = 'NO'");

        TypeTransformer transformer = new TypeTransformer();

        LocalSubscription localSubscription = transformer.transformAddSubscriptionToLocalSubscription(subscription, "service-provider", "my-node");

        System.out.println(localSubscription.toString());
    }

}
