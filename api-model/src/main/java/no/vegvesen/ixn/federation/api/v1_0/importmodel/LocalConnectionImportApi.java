package no.vegvesen.ixn.federation.api.v1_0.importmodel;

import java.util.Objects;

public class LocalConnectionImportApi {

    private String source;

    private String destination;

    public LocalConnectionImportApi() {

    }

    public LocalConnectionImportApi(String source,
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
        LocalConnectionImportApi that = (LocalConnectionImportApi) o;
        return Objects.equals(source, that.source) && Objects.equals(destination, that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, destination);
    }

    @Override
    public String toString() {
        return "LocalConnectionImportApi{" +
                "source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                '}';
    }
}
