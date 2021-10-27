package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class AddDeliveriesRequest {
    private String version = "1.0";
    private String name;
    private Set<SelectorApi> deliveries;

    public AddDeliveriesRequest() {
    }

    public AddDeliveriesRequest(String name, Set<SelectorApi> deliveries) {
        this.name = name;
        this.deliveries = deliveries;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SelectorApi> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<SelectorApi> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddDeliveriesRequest that = (AddDeliveriesRequest) o;
        return Objects.equals(version, that.version) && Objects.equals(name, that.name) && Objects.equals(deliveries, that.deliveries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, deliveries);
    }

    @Override
    public String toString() {
        return "AddDeliveriesRequest{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", deliveries=" + deliveries +
                '}';
    }
}
