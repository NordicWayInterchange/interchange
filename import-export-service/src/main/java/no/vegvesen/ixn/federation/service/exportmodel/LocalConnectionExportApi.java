package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;

public class LocalConnectionExportApi {

    private String source;

    public LocalConnectionExportApi() {

    }

    public LocalConnectionExportApi(String source) {
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
        LocalConnectionExportApi that = (LocalConnectionExportApi) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source);
    }

    @Override
    public String toString() {
        return "LocalConnectionExportApi{" +
                "source='" + source + '\'' +
                '}';
    }
}
