package no.vegvesen.ixn.admin;

import java.util.Objects;

import java.util.Set;

public class GetAllNeighboursResponse {

    private String interchangeName;

    private Set<NeighbourApi> neighboursWithPathAndApi;

    public GetAllNeighboursResponse(String interchangeName, Set<NeighbourApi> neighboursWithPathAndApi){
        this.interchangeName = interchangeName;
        this.neighboursWithPathAndApi = neighboursWithPathAndApi;

    }

    public String getInterchangeName() {
        return interchangeName;
    }

    public void setInterchangeName(String interchangeName) {
        this.interchangeName = interchangeName;
    }

    public Set<NeighbourApi> getNeighboursWithPathAndApi() {
        return neighboursWithPathAndApi;
    }

    public void setNeighboursWithPathAndApi(Set<NeighbourApi> neighboursWithPathAndApi) {
        this.neighboursWithPathAndApi = neighboursWithPathAndApi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GetAllNeighboursResponse that = (GetAllNeighboursResponse) o;
        return Objects.equals(neighboursWithPathAndApi, that.neighboursWithPathAndApi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(neighboursWithPathAndApi);
    }

    @Override
    public String toString() {
        return "GetAllNeighboursResponse{" +
                "neighbours=" + neighboursWithPathAndApi +
                '}';
    }
}
