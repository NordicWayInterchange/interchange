package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

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

}
