package no.vegvesen.ixn.federation.adminserver.model.neighbour;

import java.time.LocalDateTime;

public class ConnectionApi {

    private Integer id;

    private LocalDateTime backoffStart;

    private int backoffAttempts = 0;

    private ConnectionStatusApi connectionStatus;

    private LocalDateTime unreachableTime;

    private LocalDateTime lastFailedConnectionAttempt;

    public ConnectionApi(Integer id, LocalDateTime backoffStart, int backoffAttempts, ConnectionStatusApi connectionStatus, LocalDateTime unreachableTime, LocalDateTime lastFailedConnectionAttempt) {
        this.id = id;
        this.backoffStart = backoffStart;
        this.backoffAttempts = backoffAttempts;
        this.connectionStatus = connectionStatus;
        this.unreachableTime = unreachableTime;
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public ConnectionStatusApi getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatusApi connectionStatus) {
        this.connectionStatus = connectionStatus;
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

    @Override
    public String toString() {
        return "ConnectionApi{" +
                "id=" + id +
                ", backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", connectionStatus=" + connectionStatus +
                ", unreachableTime=" + unreachableTime +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                '}';
    }
}
