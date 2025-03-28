package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import java.util.Objects;

public class LocalConnectionApi {
    private Integer id;

    private String source;

    private String destination;

    public LocalConnectionApi() {
    }

    public LocalConnectionApi(Integer id, String source, String destination) {
        this.id = id;
        this.source = source;
        this.destination = destination;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalConnectionApi that = (LocalConnectionApi) o;
        return Objects.equals(id, that.id) && Objects.equals(source, that.source) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, source, destination);
    }

    @Override
    public String toString() {
        return "LocalConnectionApi{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
