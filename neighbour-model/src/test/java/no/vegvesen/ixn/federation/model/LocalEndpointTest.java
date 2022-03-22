package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalEndpointTest {

    @Test
    public void testEquals() {
        LocalEndpoint endpoint1 = new LocalEndpoint(
                1,
                "a",
                "host",
                1,
                2,
                3
        );
        LocalEndpoint endpoint2 = new LocalEndpoint(
                "a",
                "host",
                1,
                2,
                3
        );
        assertThat(endpoint1).isEqualTo(endpoint2);
    }

}
