package no.vegvesen.ixn.federation.model;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ConnectionBackoffTest {
    @Test
    public void failedSubscriptionRequest_firstSetsStart() {
        ConnectionBackoff connectionBackoff = new ConnectionBackoff();
        connectionBackoff.failedConnection(2);
        assertThat(connectionBackoff.getBackoffStartTime()).isNotNull().isAfter(LocalDateTime.now().minusSeconds(3));
        assertThat(connectionBackoff.getBackoffAttempts()).isEqualTo(0);
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connectionBackoff.failedConnection(2);
        assertThat(connectionBackoff.getBackoffAttempts()).isEqualTo(1);
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connectionBackoff.failedConnection(2);
        assertThat(connectionBackoff.getBackoffAttempts()).isEqualTo(2);
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connectionBackoff.failedConnection(2);
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

        connectionBackoff.failedConnection(2);
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

        connectionBackoff.okConnection();
        AssertionsForInterfaceTypes.assertThat(connectionBackoff.getConnectionStatus()).isEqualTo(ConnectionStatus.CONNECTED);
        assertThat(connectionBackoff.getBackoffStartTime()).isNull();
        assertThat(connectionBackoff.getBackoffAttempts()).isEqualTo(0);
    }

}