package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

import static no.vegvesen.ixn.docker.DockerBaseIT.getDockerHost;
import static org.assertj.core.api.Assertions.assertThat;
public class LocalDeliveryTest {

    public static final String HOST_NAME = getDockerHost();


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
                "DENM delivery"
        );
        LocalDelivery localDelivery3 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.REQUESTED,
                "DENM delivery"

        );
        LocalDelivery localDelivery4 = new LocalDelivery(
                "messageType = 'DENM'",
                LocalDeliveryStatus.CREATED,
                "DENM delivery"
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
        String selector = "originatingCountry = 'NO'";
        LocalDeliveryEndpoint endpoint = new LocalDeliveryEndpoint(HOST_NAME, 5671, "exchange");

        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                new HashSet<>(Collections.singletonList(endpoint)),
                selector,
                LocalDeliveryStatus.CREATED,
                queueName);

        assertThat(delivery.getDlqName()).isEqualTo(queueName);
    }

}
