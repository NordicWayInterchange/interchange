package no.vegvesen.ixn.federation.adminserver.model;

import java.util.List;

public class ListNeighboursResponse {

    private List<NeighbourApi> neighbours;

    public ListNeighboursResponse(){

    }

    public ListNeighboursResponse(List<NeighbourApi> neighbours) {
        this.neighbours = neighbours;
    }

    public List<NeighbourApi> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<NeighbourApi> neighbours) {
        this.neighbours = neighbours;
    }

    @Override
    public String toString() {
        return "ListNeighboursResponse{" +
                "neighbours=" + neighbours +
                '}';
    }
}
