package no.vegvesen.ixn.federation.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class Connection {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "con_back")
    private Integer id;

    private static Logger logger = LoggerFactory.getLogger(Connection.class);

    private LocalDateTime backoffStart;
    private int backoffAttempts = 0;
    @Enumerated(EnumType.STRING)
    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;
    private LocalDateTime unreachableTime;
    private LocalDateTime lastFailedConnectionAttempt;

    public Connection(){
    }

    public boolean canBeContacted(GracefulBackoffProperties backoffProperties) {
        switch (connectionStatus){
            case UNREACHABLE:
                //Calculate if allowed to connect to UNREACHABLE
                this.unreachableTime = LocalDateTime.now();
                LocalDateTime interval = lastFailedConnectionAttempt.plus(backoffProperties.getBackoffInterval(), ChronoUnit.MILLIS);
                return unreachableTime.isAfter(interval);
            case FAILED:
                return this.getBackoffStartTime() == null || LocalDateTime.now().isAfter(this.getNextPostAttemptTime(backoffProperties));
            case CONNECTED:
            default :
                return true;
        }
    }

    public void failedConnection(int maxAttemptsBeforeUnreachable) {
        this.lastFailedConnectionAttempt = LocalDateTime.now();
        if (this.getBackoffStartTime() == null) {
            this.setConnectionStatus(ConnectionStatus.FAILED);
            this.backoffStart = LocalDateTime.now();
            this.backoffAttempts = 0;
            logger.warn("Starting backoff now {}", this.backoffStart);
        } else {
            this.backoffAttempts++;
            logger.warn("Increasing backoff counter to {}", this.backoffAttempts);
            if (this.getBackoffAttempts() > maxAttemptsBeforeUnreachable) {
                this.setConnectionStatus(ConnectionStatus.UNREACHABLE);
                logger.warn("Unsuccessful in reestablishing contact with neighbour. Exceeded number of allowed connection attempts.");
                logger.warn("Number of allowed connection attempts: {} Number of actual connection attempts: {}", maxAttemptsBeforeUnreachable, this.getBackoffAttempts());
                logger.warn("Setting status of neighbour to UNREACHABLE.");
            }
        }
    }

    public void okConnection() {
        this.setConnectionStatus(ConnectionStatus.CONNECTED);
        this.backoffAttempts = 0;
        this.backoffStart = null;
    }

    // Calculates next possible post attempt time, using exponential backoff
    LocalDateTime getNextPostAttemptTime(GracefulBackoffProperties backoffProperties) {

        logger.info("Calculating next allowed time to contact neighbour.");
        int randomShift = new Random().nextInt(backoffProperties.getRandomShiftUpperLimit());
        long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, this.getBackoffAttempts()) * backoffProperties.getStartIntervalLength()) + randomShift;
        LocalDateTime nextPostAttempt = this.getBackoffStartTime().plus(exponentialBackoffWithRandomizationMillis, ChronoField.MILLI_OF_SECOND.getBaseUnit());

        logger.info("Next allowed post time: {}", nextPostAttempt.toString());
        return nextPostAttempt;
    }

    public LocalDateTime getBackoffStartTime() { return backoffStart; }

    public void setBackoffStart(LocalDateTime backoffStart) {
        this.backoffStart = backoffStart;
    }

    public ConnectionStatus getConnectionStatus() { return connectionStatus; }

    public void setConnectionStatus(ConnectionStatus connectionStatus) { this.connectionStatus = connectionStatus; }

    public int getBackoffAttempts() { return backoffAttempts; }

    public void setBackoffAttempts(int backoffAttempts) {
        this.backoffAttempts = backoffAttempts;
    }

    public LocalDateTime getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(LocalDateTime lastFailedConnectionAttempt) {
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    @Override
    public String toString() {
        return "ConnectionBackoff{" +
                "id=" + id +
                ", backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", connectionStatus=" + connectionStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return backoffAttempts == that.backoffAttempts &&
                Objects.equals(backoffStart, that.backoffStart) &&
                connectionStatus == that.connectionStatus &&
                Objects.equals(unreachableTime, that.unreachableTime) &&
                Objects.equals(lastFailedConnectionAttempt, that.lastFailedConnectionAttempt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(backoffStart, backoffAttempts, connectionStatus, unreachableTime, lastFailedConnectionAttempt);
    }
}
