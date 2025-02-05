package no.vegvesen.ixn.federation.service.importmodel;

import java.util.Objects;

public class LocalConnectionImportApi {

    private String source;

    public LocalConnectionImportApi() {

    }

    public LocalConnectionImportApi(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalConnectionImportApi that = (LocalConnectionImportApi) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return "LocalConnectionImportApi{" +
                "source='" + source + '\'' +
                '}';
    }
}
