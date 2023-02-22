package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class EndpointTest {

    @Test
    public void hashCodeAndEquals() {
       Endpoint endpoint1 = new Endpoint(
               "a",
               "b",
               1,
               2,
               3
       );
       Endpoint endpoint2 = new Endpoint(
               1,
               "a",
               "b",
               1,
               2,
               3
       );
       assertThat(endpoint1).isEqualTo(endpoint2);
    }


    @Test
    public void testEndpointsToRemove() {
        Endpoint endpoint1 = new Endpoint(
                "a",
                "b",
                1,
                2,
                3
        );
        Endpoint endpoint2 = new Endpoint(
                1,
                "a",
                "b",
                1,
                2,
                3
        );
        Set<Endpoint> wantedEndpoints = Collections.singleton(endpoint1);
        Set<Endpoint> existingEndpoins = Collections.singleton(endpoint2);

        Set<Endpoint> endpointsToRemove = new HashSet<>(existingEndpoins);
        endpointsToRemove.removeAll(wantedEndpoints);

        assertThat(endpointsToRemove).isEmpty();

        Set<Endpoint> additionalEndpoints = new HashSet<>(wantedEndpoints);
        additionalEndpoints.removeAll(existingEndpoins);
        assertThat(additionalEndpoints).isEmpty();



    }

}
