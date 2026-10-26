package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
public class LocalDeliveryTest {

    @Test
    public void hashCodeAndEquals() {
        LocalDelivery localDelivery1 = new LocalDelivery(
                UUID.randomUUID().toString(),
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED
        );
        LocalDelivery localDelivery2 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED,
                "DENM delivery",
                false
        );
        LocalDelivery localDelivery3 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED,
                "DENM delivery",
                false

        );
        LocalDelivery localDelivery4 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.CREATED,
                "DENM delivery",
                false
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

    @Test
    public void TestLocalDeliveryWithDlqTest() {
        String queueName = "dlqueue";
        String secondQueueName = "second_dlqueue";
        String selector = "originatingCountry = 'NO'";

        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(
                1,
                "host",
                123,
                "target",
                2,
                3,
                queueName
        );

        LocalDeliveryEndpoint secondEndpoint = new LocalDeliveryEndpoint(
                1,
                "host",
                456,
                "target",
                2,
                3,
                secondQueueName
        );

        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                new HashSet<>(Set.of(endpoint, secondEndpoint)),
                selector,
                LocalDeliveryStatus.CREATED);

        assertThat(Objects.requireNonNull(delivery.getEndpoints().stream().findFirst().orElse(null)).getDlqName()).isEqualTo(queueName);
        assertThat(Objects.requireNonNull(delivery.getEndpoints().stream().skip(1).findFirst().orElse(null)).getDlqName()).isEqualTo(secondQueueName);
    }

}
