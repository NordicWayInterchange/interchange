package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDeliveryEndpointTest {

    @Test
    public void hashCodeAndEquals() {
        LocalDeliveryEndpoint endpoint1 = new LocalDeliveryEndpoint(
                "host",
                123,
                "target",
                2,
                3
        );
        LocalDeliveryEndpoint endpoint2 = new LocalDeliveryEndpoint(
                1,
                "host",
                123,
                "target",
                2,
                3
        );
        //NOTE endpoints are considered equal even when messageRate and bandwidth are not equal.
        LocalDeliveryEndpoint endpoint3 = new LocalDeliveryEndpoint(
                "host",
                123,
                "target"
        );
        assertThat(endpoint3).isEqualTo(endpoint2).isEqualTo(endpoint1);
        assertThat(endpoint3.hashCode()).isEqualTo(endpoint2.hashCode()).isEqualTo(endpoint1.hashCode());
    }

}
