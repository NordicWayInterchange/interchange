package no.vegvesen.ixn.serviceprovider.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

public class Delivery {
    private String id;

    private String path;

    private String selector;

    private long lastUpdatedTimestamp;

    private DeliveryStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;

    public Delivery() {
    }


    public Delivery(String id, String path, String selector, long lastUpdatedTimestamp, DeliveryStatus status, String errorMessage) {
        this.id = id;
        this.path = path;
        this.selector = selector;
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
        this.status = status;
        this.errorMessage = errorMessage;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Delivery delivery = (Delivery) o;
        return lastUpdatedTimestamp == delivery.lastUpdatedTimestamp && Objects.equals(id, delivery.id) && Objects.equals(path, delivery.path) && Objects.equals(selector, delivery.selector) && status == delivery.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, selector, lastUpdatedTimestamp, status);
    }

    @Override
    public String toString() {
        return "Delivery{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", selector='" + selector + '\'' +
                ", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
                ", status=" + status +
                ", errorMessage=" + errorMessage +
                '}';
    }
}
