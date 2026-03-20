package no.vegvesen.ixn.federation.model;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {
    private GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();

    @Test
    public void failedSubscriptionRequest_firstSetsStart() {
        Connection connection = new Connection();
        connection.failedConnection(2);
        assertThat(connection.getBackoffStartTime()).isNotNull().isAfter(LocalDateTime.now().minusSeconds(3));
        assertThat(connection.getBackoffAttempts()).isEqualTo(0);
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connection.failedConnection(2);
        assertThat(connection.getBackoffAttempts()).isEqualTo(1);
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connection.failedConnection(2);
        assertThat(connection.getBackoffAttempts()).isEqualTo(2);
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.FAILED);

        connection.failedConnection(2);
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

        connection.failedConnection(2);
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.UNREACHABLE);

        connection.okConnection();
        AssertionsForInterfaceTypes.assertThat(connection.getConnectionStatus()).isEqualTo(ConnectionStatus.CONNECTED);
        assertThat(connection.getBackoffStartTime()).isNull();
        assertThat(connection.getBackoffAttempts()).isEqualTo(0);
    }

    @Test
    public void calculatedNextPostAttemptTimeIsInCorrectInterval(){
        Connection connection = new Connection();
        LocalDateTime now = LocalDateTime.now();

        // Mocking the first backoff attempt, where the exponential is 0.
        double exponential = 0;
        long expectedBackoff = (long) Math.pow(2, exponential)*2; //

        System.out.println("LocalDataTime now: "+ now.toString());
        LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
        LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

        System.out.println("Lower limit: " + lowerLimit.toString());
        System.out.println("Upper limit: " + upperLimit.toString());

        connection.setBackoffAttempts(0);
        connection.setBackoffStart(now);

        LocalDateTime result = connection.getNextPostAttemptTime(backoffProperties);

        assertThat(result).isBetween(lowerLimit, upperLimit);
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsCONNECTED() {
        Connection connection = new Connection();
        connection.okConnection();
        assertTrue(connection.canBeContacted(backoffProperties));
        assertEquals(ConnectionStatus.CONNECTED, connection.getConnectionStatus());
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsFAILED() {
        Connection connection = new Connection();
        connection.failedConnection(4);
        assertFalse(connection.canBeContacted(backoffProperties));
        assertEquals(ConnectionStatus.FAILED, connection.getConnectionStatus());
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsUNREACHABLE() {
        Connection connection = new Connection();
        connection.setBackoffStart(LocalDateTime.now());
        connection.setBackoffAttempts(4);
        connection.failedConnection(4);
        assertFalse(connection.canBeContacted(backoffProperties));
        assertEquals(ConnectionStatus.UNREACHABLE, connection.getConnectionStatus());
    }

    @Test
    public void canBeContactedWhenLastFailedConnectionAttemptIsNull() {
        Connection connection = new Connection();
        connection.setConnectionStatus(ConnectionStatus.UNREACHABLE);
        assertTrue(connection.canBeContacted(backoffProperties));
    }
}