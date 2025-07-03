package no.vegvesen.ixn.federation.adminserver.model.neighbour;


public class ConnectionApi {

    private Integer id;

    private Long backoffStart;

    private int backoffAttempts = 0;

    private ConnectionStatusApi connectionStatus;

    private Long unreachableTime;

    private Long lastFailedConnectionAttempt;

    public ConnectionApi(Integer id, Long backoffStart, int backoffAttempts, ConnectionStatusApi connectionStatus, Long unreachableTime, Long lastFailedConnectionAttempt) {
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

    public Long getBackoffStart() {
        return backoffStart;
    }

    public void setBackoffStart(Long backoffStart) {
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

    public Long getUnreachableTime() {
        return unreachableTime;
    }

    public void setUnreachableTime(Long unreachableTime) {
        this.unreachableTime = unreachableTime;
    }

    public Long getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(Long lastFailedConnectionAttempt) {
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
