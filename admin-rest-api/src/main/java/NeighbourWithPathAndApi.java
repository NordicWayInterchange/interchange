import java.util.Objects;

public class NeighbourWithPathAndApi {
    private String id;
    private String path;
    private NeighbourStatusApi definition;

    public NeighbourWithPathAndApi(String id, String path, NeighbourStatusApi definition) {
        this.id = id;
        this.path = path;
        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeighbourWithPathAndApi that = (NeighbourWithPathAndApi) o;
        return Objects.equals(id, that.id) && Objects.equals(path, that.path) && definition == that.definition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, path, definition);
    }

    @Override
    public String toString() {
        return "NeighbourWithPathAndApi{" +
                "id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", definition=" + definition +
                '}';
    }

}
