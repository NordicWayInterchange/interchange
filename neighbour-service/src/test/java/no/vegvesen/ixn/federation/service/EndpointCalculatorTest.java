package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.model.Endpoint;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class EndpointCalculatorTest {

    @Test
    public void singleEndpointRepeatedByNeighbour() {
        Endpoint ourEndpoint = new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        );
        Set<Endpoint> ourEndpoints = Collections.singleton(ourEndpoint);
        Set<Endpoint> neighbourEndpoints = Collections.singleton(new Endpoint(
                "a",
                "b",
                1,
                2,
                3
        ));
        EndpointCalculator endpointCalculator = new EndpointCalculator(
                ourEndpoints,
                neighbourEndpoints);
        assertThat(endpointCalculator.getEndpointsToRemove()).isEmpty();
        assertThat(endpointCalculator.getNewEndpoints()).isEmpty();
        Set<Endpoint> calculatedEndpoints = endpointCalculator.getCalculatedEndpointsSet();
        assertThat(calculatedEndpoints).hasSize(1);
        Endpoint calculatedEndpoint = calculatedEndpoints.stream().findFirst().get();
        assertThat(calculatedEndpoint.getId()).isEqualTo(1);
    }

    @Test
    public void removeSingleEndpoint() {
        Set<Endpoint> ourEndpoints = Collections.singleton(new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        ));
        EndpointCalculator endpointCalculator = new EndpointCalculator(ourEndpoints,Collections.emptySet());
        assertThat(endpointCalculator.getEndpointsToRemove()).hasSize(1);
        assertThat(endpointCalculator.getNewEndpoints()).hasSize(0);
        assertThat(endpointCalculator.getCalculatedEndpointsSet()).isEmpty();
    }

    @Test
    public void addSingleEndpoint() {
        Set<Endpoint> neighbourEndpoints = Collections.singleton(new Endpoint(
                "a",
                "b",
                1,
                2,
                3
        ));
        EndpointCalculator calculator = new EndpointCalculator(Collections.emptySet(), neighbourEndpoints);
        assertThat(calculator.getEndpointsToRemove()).isEmpty();
        assertThat(calculator.getNewEndpoints()).hasSize(1);
        assertThat(calculator.getCalculatedEndpointsSet()).hasSize(1);
    }

    @Test
    public void removeTwoAddTwo() {

        HashSet<Endpoint> ourEndpoints = new HashSet<>(Arrays.asList(new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        ), new Endpoint(
                2,
                "c",
                "f",
                3
        )));

        HashSet<Endpoint> neighbourEndpoints = new HashSet<>(Arrays.asList(new Endpoint(
                "source",
                "host",
                2
        ), new Endpoint(
                "othersource",
                "host",
                2
        )));
        EndpointCalculator endpointCalculator = new EndpointCalculator(ourEndpoints,neighbourEndpoints);
        assertThat(endpointCalculator.getNewEndpoints()).hasSize(2).isEqualTo(neighbourEndpoints);
        assertThat(endpointCalculator.getEndpointsToRemove()).hasSize(2).isEqualTo(ourEndpoints);
        assertThat(endpointCalculator.getCalculatedEndpointsSet()).hasSize(2).isEqualTo(neighbourEndpoints);

    }
}
