package no.vegvesen.ixn.federation.api.v1_0.exportmodel;

import java.util.Objects;

public class LocalConnectionExportApi {

    private String source;

    private String destination;

    public LocalConnectionExportApi() {

    }

    public LocalConnectionExportApi(String source,
                                    String destination) {
        this.source = source;
        this.destination = destination;
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
        LocalConnectionExportApi that = (LocalConnectionExportApi) o;
        return Objects.equals(source, that.source) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

    @Override
    public String toString() {
        return "LocalConnectionExportApi{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
