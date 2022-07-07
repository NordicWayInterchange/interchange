
import java.util.Objects;

import java.util.List;

import java.util.Set;

public class GetAllNeighboursResponse {

    private String interchangeName;

    private Set<NeighbourWithPathAndApi> neighbourWithPathAndApis;

    public GetAllNeighboursResponse(String interchangeName, Set<NeighbourWithPathAndApi> neighbourWithPathAndApis){
        this.interchangeName = interchangeName;
        this.neighbourWithPathAndApis = neighbourWithPathAndApis;

    }

    public String getInterchangeName() {
        return interchangeName;
    }

    public void setInterchangeName(String interchangeName) {
        this.interchangeName = interchangeName;
    }

    public Set<NeighbourWithPathAndApi> getNeighbourWithPathAndApis() {
        return neighbourWithPathAndApis;
    }

    public void setNeighbourWithPathAndApis(Set<NeighbourWithPathAndApi> neighbourWithPathAndApis) {
        this.neighbourWithPathAndApis = neighbourWithPathAndApis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAllNeighboursResponse that = (GetAllNeighboursResponse) o;
        return Objects.equals(neighbourWithPathAndApis, that.neighbourWithPathAndApis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighbourWithPathAndApis);
    }

    @Override
    public String toString() {
        return "GetAllNeighboursResponse{" +
                "neighbours=" + neighbourWithPathAndApis +
                '}';
    }
}
