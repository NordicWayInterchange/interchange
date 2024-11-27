package no.vegvesen.ixn.federation.adminserver.model;

public class ConnectionApi {
    private long backoffStart;
    private int backofAttempts;
    private ConnectionStatusApi connectionStatus;
    private long unreachableTime;
    private long lastFailedConnectionAttempt;

    public ConnectionApi() {
    }

    public ConnectionApi(long backoffStart, int backofAttempts, ConnectionStatusApi connectionStatus, long unreachableTime, long lastFailedConnectionAttempt) {
        this.backoffStart = backoffStart;
        this.backofAttempts = backofAttempts;
        this.connectionStatus = connectionStatus;
        this.unreachableTime = unreachableTime;
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    public long getBackoffStart() {
        return backoffStart;
    }

    public void setBackoffStart(long backoffStart) {
        this.backoffStart = backoffStart;
    }

    public int getBackofAttempts() {
        return backofAttempts;
    }

    public void setBackofAttempts(int backofAttempts) {
        this.backofAttempts = backofAttempts;
    }

    public ConnectionStatusApi getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ConnectionStatusApi connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public long getUnreachableTime() {
        return unreachableTime;
    }

    public void setUnreachableTime(long unreachableTime) {
        this.unreachableTime = unreachableTime;
    }

    public long getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(long lastFailedConnectionAttempt) {
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    @Override
    public String toString() {
        return "ConnectionApi{" +
                "backoffStart=" + backoffStart +
                ", backofAttempts=" + backofAttempts +
                ", connectionStatus=" + connectionStatus +
                ", unreachableTime=" + unreachableTime +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                '}';
    }
}
