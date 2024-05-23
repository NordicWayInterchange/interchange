package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class ConnectionExportApi {

    private long backoffStart;

    private Integer backoffAttempts;

    private ConnectionStatusExportApi status;

    private long lastFailedConnectionAttempt;

    public enum ConnectionStatusExportApi {
        CONNECTED, FAILED, UNREACHABLE
    }

    public ConnectionExportApi() {

    }

    public ConnectionExportApi(long backoffStart,
                               Integer backoffAttempts,
                               ConnectionStatusExportApi status,
                               long lastFailedConnectionAttempt) {

    }

    public long getBackoffStart() {
        return backoffStart;
    }

    public void setBackoffStart(long backoffStart) {
        this.backoffStart = backoffStart;
    }

    public Integer getBackoffAttempts() {
        return backoffAttempts;
    }

    public void setBackoffAttempts(Integer backoffAttempts) {
        this.backoffAttempts = backoffAttempts;
    }

    public ConnectionStatusExportApi getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatusExportApi status) {
        this.status = status;
    }

    public long getLastFailedConnectionAttempt() {
        return lastFailedConnectionAttempt;
    }

    public void setLastFailedConnectionAttempt(long lastFailedConnectionAttempt) {
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConnectionExportApi that = (ConnectionExportApi) o;
        return backoffStart == that.backoffStart && lastFailedConnectionAttempt == that.lastFailedConnectionAttempt && Objects.equals(backoffAttempts, that.backoffAttempts) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(backoffStart, backoffAttempts, status, lastFailedConnectionAttempt);
    }

    @Override
    public String toString() {
        return "ConnectionExportApi{" +
                "backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", status=" + status +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                '}';
    }
}
