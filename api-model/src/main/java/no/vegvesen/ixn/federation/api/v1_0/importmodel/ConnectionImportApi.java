package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;

public class ConnectionImportApi {

    private long backoffStart;

    private Integer backoffAttempts;

    private ConnectionStatusImportApi status;

    private long lastFailedConnectionAttempt;

    public enum ConnectionStatusImportApi {
        CONNECTED, FAILED, UNREACHABLE
    }

    public ConnectionImportApi() {

    }

    public ConnectionImportApi(long backoffStart,
                               Integer backoffAttempts,
                               ConnectionStatusImportApi status,
                               long lastFailedConnectionAttempt) {
        this.backoffStart = backoffStart;
        this.backoffAttempts = backoffAttempts;
        this.status = status;
        this.lastFailedConnectionAttempt = lastFailedConnectionAttempt;
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

    public ConnectionStatusImportApi getStatus() {
        return status;
    }

    public void setStatus(ConnectionStatusImportApi status) {
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
        ConnectionImportApi that = (ConnectionImportApi) o;
        return backoffStart == that.backoffStart && lastFailedConnectionAttempt == that.lastFailedConnectionAttempt && Objects.equals(backoffAttempts, that.backoffAttempts) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(backoffStart, backoffAttempts, status, lastFailedConnectionAttempt);
    }

    @Override
    public String toString() {
        return "ConnectionImportApi{" +
                "backoffStart=" + backoffStart +
                ", backoffAttempts=" + backoffAttempts +
                ", status=" + status +
                ", lastFailedConnectionAttempt=" + lastFailedConnectionAttempt +
                '}';
    }
}
