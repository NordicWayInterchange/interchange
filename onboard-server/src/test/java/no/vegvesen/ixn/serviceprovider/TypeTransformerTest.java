package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.serviceprovider.model.AddSubscription;
import no.vegvesen.ixn.serviceprovider.model.BiqueueAccessResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {

    @Test
    public void testAddSubscriptionWithEmptyConsumerCommonName() {
        AddSubscription subscription = new AddSubscription("originatingCountry = 'NO'", "NO SUB");

        TypeTransformer transformer = new TypeTransformer();

        LocalSubscription localSubscription = transformer.transformAddSubscriptionToLocalSubscription(subscription, "service-provider", "my-node");

        assertThat(localSubscription.getSelector()).isEqualTo("originatingCountry = 'NO'");
    }


    @Test
    public void testHandleNullBiConsumer() {
        ServiceProvider serviceProvider = new ServiceProvider(
                "testuser",
                null, //To explicitly test for null values
                new Capabilities(),
                Set.of(),
                Set.of(),
                LocalDateTime.now()
        );
        TypeTransformer typeTransformer = new TypeTransformer();
        BiqueueAccessResponse biqueueAccessResponse = typeTransformer.transformBiQueueToGetBiqueueResponse(serviceProvider);
        assertThat(biqueueAccessResponse.isAccess()).isFalse();


    }

}
