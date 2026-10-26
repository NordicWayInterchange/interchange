package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalConnectionTest {

    @Test
    public void hashCodeAndEquals() {
        LocalConnection connection1 = new LocalConnection(
                1,
                "source",
                "destination"
        );
        LocalConnection connection2 = new LocalConnection(
                "source",
                "destination"
        );
        assertThat(connection1).isEqualTo(connection2);
        assertThat(connection1.hashCode()).isEqualTo(connection2.hashCode());

    }


}
