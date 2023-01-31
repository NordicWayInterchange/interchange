package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Entity
public class MessageConnection {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "con_mess")
    private Integer id;

    private static Logger logger = LoggerFactory.getLogger(MessageConnection.class);

    private LocalDateTime backoffStart;
    private int backoffAttempts = 0;

    @Enumerated(EnumType.STRING)
    private MessageConnectionStatus messageConnectionStatus = MessageConnectionStatus.CONNECTED;

    private LocalDateTime unreachableTime;
    private LocalDateTime lastFailedConnectionAttempt;

    public MessageConnection() {

    }

    public LocalDateTime getBackoffStart() {
        return backoffStart;
    }

    public void setBackoffStart(LocalDateTime backoffStart) {
        this.backoffStart = backoffStart;
    }

    public int getBackoffAttempts() {
        return backoffAttempts;
    }

    public void setBackoffAttempts(int backoffAttempts) {
        this.backoffAttempts = backoffAttempts;
    }

    public MessageConnectionStatus getMessageConnectionStatus() {
        return messageConnectionStatus;
    }

    public void setMessageConnectionStatus(MessageConnectionStatus messageConnectionStatus) {
        this.messageConnectionStatus = messageConnectionStatus;
    }

    public LocalDateTime getUnreachableTime() {
        return unreachableTime;
    }

    public void setUnreachableTime(LocalDateTime unreachableTime) {
        this.unreachableTime = unreachableTime;
    }

    public LocalDateTime getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(LocalDateTime lastFailedConnectionAttempt) {
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    public boolean canBeContacted(GracefulBackoffProperties backoffProperties) {
        switch (messageConnectionStatus){
            case UNREACHABLE:
                //Calculate if allowed to connect to UNREACHABLE
                this.unreachableTime = LocalDateTime.now();
                LocalDateTime interval = lastFailedConnectionAttempt.plus(backoffProperties.getBackoffInterval(), ChronoUnit.MILLIS);
                return unreachableTime.isAfter(interval);
            case FAILED:
                return this.getBackoffStart() == null || LocalDateTime.now().isAfter(this.getNextPostAttempt(backoffProperties));
            case CONNECTED:
            default :
                return true;
        }
    }

    LocalDateTime getNextPostAttempt(GracefulBackoffProperties backoffProperties) {
        logger.info("Calculating next allowed time to contact neighbour.");
        int randomShift = new Random().nextInt(backoffProperties.getRandomShiftUpperLimit());
        long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, this.getBackoffAttempts()) * backoffProperties.getStartIntervalLength()) + randomShift;
        LocalDateTime nextPostAttempt = this.getBackoffStart().plus(exponentialBackoffWithRandomizationMillis, ChronoField.MILLI_OF_SECOND.getBaseUnit());

        logger.info("Next allowed post time: {}", nextPostAttempt.toString());
        return nextPostAttempt;
    }

    public void failedMessageConnection(int maxAttemptsBeforeUnreachable) {
        this.lastFailedConnectionAttempt = LocalDateTime.now();
        if (this.getBackoffStart() == null) {
            this.setMessageConnectionStatus(MessageConnectionStatus.FAILED);
            this.backoffStart = LocalDateTime.now();
            this.backoffAttempts = 0;
            logger.warn("Starting backoff now {}", this.backoffStart);
        } else {
            this.backoffAttempts++;
            logger.warn("Increasing backoff counter to {}", this.backoffAttempts);
            if (this.getBackoffAttempts() > maxAttemptsBeforeUnreachable) {
                this.setMessageConnectionStatus(MessageConnectionStatus.UNREACHABLE);
                logger.warn("Unsuccessful in reestablishing contact with neighbour. Exceeded number of allowed connection attempts.");
                logger.warn("Number of allowed connection attempts: {} Number of actual connection attempts: {}", maxAttemptsBeforeUnreachable, this.getBackoffAttempts());
                logger.warn("Setting status of neighbour to UNREACHABLE.");
            }
        }
    }

    public void okMessageConnection() {
        this.setMessageConnectionStatus(MessageConnectionStatus.CONNECTED);
        this.backoffAttempts = 0;
        this.backoffStart = null;
    }

    @Override
    public String toString() {
        return "MessageConnection{" +
                "id=" + id +
                ", backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", messageConnectionStatus=" + messageConnectionStatus +
                ", unreachableTime=" + unreachableTime +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                '}';
    }
}
