package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Endpoint;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class EndpointCalculatorTest {

    @Test
    public void calculateEndpointsToRemove() {
        Endpoint neighbourEndpoint = new Endpoint(
                "a",
                "b",
                1,
                2,
                3
        );
        Endpoint ourEndpoint = new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        );
        EndpointCalculator endpointCalculator = new EndpointCalculator(
                Collections.singleton(ourEndpoint),
                Collections.singleton(neighbourEndpoint));
        assertThat(endpointCalculator.getEndpointsToRemove()).isEmpty();
        assertThat(endpointCalculator.getNewEndpoints()).isEmpty();
        assertThat(endpointCalculator.getCalculatedEndpointsSet()).isEqualTo(Collections.singleton(ourEndpoint));
    }

    @Test
    public void removeSingleEndpoint() {
        Endpoint ourEndpoint = new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        );
        EndpointCalculator endpointCalculator = new EndpointCalculator(Collections.singleton(ourEndpoint),Collections.emptySet());
        assertThat(endpointCalculator.getEndpointsToRemove()).hasSize(1);
        assertThat(endpointCalculator.getNewEndpoints()).hasSize(0);
        assertThat(endpointCalculator.getCalculatedEndpointsSet()).isEmpty();



    }
}
