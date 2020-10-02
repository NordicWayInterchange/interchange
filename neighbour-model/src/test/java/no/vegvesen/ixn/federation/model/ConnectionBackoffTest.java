package no.vegvesen.ixn.federation.model;

import org.assertj.core.api.AssertionsForInterfaceTypes;
import org.junit.Assert;
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

    @Test
    public void calculatedNextPostAttemptTimeIsInCorrectInterval(){
        ConnectionBackoff connectionBackoff = new ConnectionBackoff();
        LocalDateTime now = LocalDateTime.now();

        // Mocking the first backoff attempt, where the exponential is 0.
        double exponential = 0;
        long expectedBackoff = (long) Math.pow(2, exponential)*2; //

        System.out.println("LocalDataTime now: "+ now.toString());
        LocalDateTime lowerLimit = now.plusSeconds(expectedBackoff);
        LocalDateTime upperLimit = now.plusSeconds(expectedBackoff+60);

        System.out.println("Lower limit: " + lowerLimit.toString());
        System.out.println("Upper limit: " + upperLimit.toString());

        connectionBackoff.setBackoffAttempts(0);
        connectionBackoff.setBackoffStart(now);

        LocalDateTime result = connectionBackoff.getNextPostAttemptTime(60000, 2000);

        assertThat(result).isBetween(lowerLimit, upperLimit);
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsCONNECTED() {
        ConnectionBackoff connectionBackoff = new ConnectionBackoff();
        connectionBackoff.okConnection();
        Assert.assertTrue(connectionBackoff.canBeContacted(0,0));
        Assert.assertEquals(connectionBackoff.getConnectionStatus(), ConnectionStatus.CONNECTED);
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsFAILED() {
        ConnectionBackoff connectionBackoff = new ConnectionBackoff();
        connectionBackoff.failedConnection(4);
        Assert.assertFalse(connectionBackoff.canBeContacted(60000,2000));
        Assert.assertEquals(connectionBackoff.getConnectionStatus(), ConnectionStatus.FAILED);
    }

    @Test
    public void canBeContactedWhenConnectionStatusIsUNREACHABLE() {
        ConnectionBackoff connectionBackoff = new ConnectionBackoff();
        connectionBackoff.setBackoffStart(LocalDateTime.now());
        connectionBackoff.setBackoffAttempts(4);
        connectionBackoff.failedConnection(4);
        Assert.assertFalse(connectionBackoff.canBeContacted(60000,2000));
        Assert.assertEquals(connectionBackoff.getConnectionStatus(), ConnectionStatus.UNREACHABLE);
    }
}