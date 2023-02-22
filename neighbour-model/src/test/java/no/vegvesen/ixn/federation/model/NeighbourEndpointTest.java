package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class NeighbourEndpointTest {

    @Test
    public void hashcodeAndEquals() {
        NeighbourEndpoint endpoint1 = new NeighbourEndpoint(
                1,
                "a",
                "b",
                443

        );
        NeighbourEndpoint endpoint2 = new NeighbourEndpoint(
                "a",
                "b",
                443
        );
        assertThat(endpoint1).isEqualTo(endpoint2);
    }

}
