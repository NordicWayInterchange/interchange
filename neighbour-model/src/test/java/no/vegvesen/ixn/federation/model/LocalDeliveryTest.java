package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class LocalDeliveryTest {

    @Test
    public void hashCodeAndEquals() {
        LocalDelivery localDelivery1 = new LocalDelivery(
                1,
                "/path-to-my-delivery",
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED
        );
        LocalDelivery localDelivery2 = new LocalDelivery(
                "/path-to-my-delivery",
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED
        );
        LocalDelivery localDelivery3 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED

        );
        LocalDelivery localDelivery4 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.CREATED
        );
        assertThat(localDelivery1)
                .isEqualTo(localDelivery2)
                .isEqualTo(localDelivery3)
                .isEqualTo(localDelivery4);
        assertThat(localDelivery1.hashCode())
                .isEqualTo(localDelivery2.hashCode())
                .isEqualTo(localDelivery3.hashCode())
                .isEqualTo(localDelivery4.hashCode());
    }

}
