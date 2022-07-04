import java.util.Objects;
import java.util.Set;

public class GetAllNeighboursResponse {

    private Set<NeighbourWithPathAndApi> neighbours;

    public GetAllNeighboursResponse(Set<NeighbourWithPathAndApi> neighbours){

        this.neighbours = neighbours;

    }

    public Set<NeighbourWithPathAndApi> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Set<NeighbourWithPathAndApi> neighbours) {
        this.neighbours = neighbours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAllNeighboursResponse that = (GetAllNeighboursResponse) o;
        return Objects.equals(neighbours, that.neighbours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbours);
    }

    @Override
    public String toString() {
        return "GetAllNeighboursResponse{" +
                "neighbours=" + neighbours +
                '}';
    }
}
