package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class AddDeliveriesResponse {
    private String version = "1.0";
    private String name;
    private Set<Delivery> deliveries;

    public AddDeliveriesResponse() {
    }

    public AddDeliveriesResponse(String name, Set<Delivery> deliveries) {
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

    public Set<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddDeliveriesResponse response = (AddDeliveriesResponse) o;
        return Objects.equals(version, response.version) && Objects.equals(name, response.name) && Objects.equals(deliveries, response.deliveries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, name, deliveries);
    }

    @Override
    public String toString() {
        return "AddDeliveriesResponse{" +
                "version='" + version + '\'' +
                ", name='" + name + '\'' +
                ", deliveries=" + deliveries +
                '}';
    }
}
