package no.vegvesen.ixn.federation.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Random;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
public class ConnectionBackoff {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "con_back")
    private Integer id;

    private static Logger logger = LoggerFactory.getLogger(ConnectionBackoff.class);

    private LocalDateTime backoffStart;
    private int backoffAttempts = 0;
    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

    public ConnectionBackoff(){
    }

    public boolean canBeContacted(int randomShiftUpperLimit, int startIntervalLength) {
        if (this.getConnectionStatus() == ConnectionStatus.UNREACHABLE) {
            return false;
        }
        if (this.getConnectionStatus() == ConnectionStatus.CONNECTED) {
            return true;
        }

        if (this.getConnectionStatus() == ConnectionStatus.FAILED) {
            return this.getBackoffStartTime() == null || LocalDateTime.now().isAfter(this.getNextPostAttemptTime(randomShiftUpperLimit, startIntervalLength));
        }
        return true;
    }

    public void failedConnection(int maxAttemptsBeforeUnreachable) {
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
    LocalDateTime getNextPostAttemptTime(int randomShiftUpperLimit, int startIntervalLength) {

        logger.info("Calculating next allowed time to contact neighbour.");
        int randomShift = new Random().nextInt(randomShiftUpperLimit);
        long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, this.getBackoffAttempts()) * startIntervalLength) + randomShift;
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

    @Override
    public String toString() {
        return "ConnectionBackoff{" +
                "id=" + id +
                ", backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", connectionStatus=" + connectionStatus +
                '}';
    }
}
