package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.capability.MapemApplication;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {
    @Test
    public void testTransformMapemCapability() {
        MapemApplication app = new MapemApplication(
                "NO-123",
                "pub-1",
                "NO",
                "MAPEM:1.1.0",
                Collections.emptySet()
        );

        ApplicationApi appApi = app.toApi();
        assertThat(appApi.getMessageType()).isEqualTo("MAPEM");

    }

    @Test
    public void testAddSubscriptionWithEmptyConsumerCommonName() {
        AddSubscription subscription = new AddSubscription("originatingCountry = 'NO'");

        TypeTransformer transformer = new TypeTransformer();

        LocalSubscription localSubscription = transformer.transformAddSubscriptionToLocalSubscription(subscription, "service-provider", "my-node");

        System.out.println(localSubscription.toString());
    }

}
