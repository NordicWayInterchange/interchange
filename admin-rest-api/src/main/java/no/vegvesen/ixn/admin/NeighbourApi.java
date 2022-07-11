package no.vegvesen.ixn.admin;

import java.util.Objects;

public class NeighbourApi {
    private String id;
    private String path;

    private String name;

    private String numberOfCapabilities;

    private String numberOfOurRequestedSubcriptions;

    private String numberOfNeighbourRequestedSubscriptions;
    private NeighbourStatusApi status;

    public NeighbourApi() {
    }

    public NeighbourApi(String id, String path, String name, String numberOfCapabilities, String numberOfOurRequestedSubcriptions, String numberOfNeighbourRequestedSubscriptions, NeighbourStatusApi status) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.numberOfCapabilities = numberOfCapabilities;
        this.numberOfOurRequestedSubcriptions = numberOfOurRequestedSubcriptions;
        this.numberOfNeighbourRequestedSubscriptions = numberOfNeighbourRequestedSubscriptions;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getNumberOfCapabilities() {
        return numberOfCapabilities;
    }

    public String getNumberOfOurRequestedSubcriptions() {
        return numberOfOurRequestedSubcriptions;
    }

    public String getNumberOfNeighbourRequestedSubscriptions() {
        return numberOfNeighbourRequestedSubscriptions;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public NeighbourStatusApi getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourApi that = (NeighbourApi) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, status);
    }

    @Override
    public String toString() {
        return "NeighbourWithPathAndApi{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", definition=" + status +
                '}';
    }

}
