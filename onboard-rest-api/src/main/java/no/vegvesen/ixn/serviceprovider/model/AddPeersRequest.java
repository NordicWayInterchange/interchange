package no.vegvesen.ixn.serviceprovider.model;

import java.util.List;
import java.util.Objects;

public class AddPeersRequest {

    List<String> peersToAdd;

    public AddPeersRequest() {}

    public AddPeersRequest(List<String> peersToAdd) {
        this.peersToAdd = peersToAdd;
    }

    public List<String> getPeersToAdd() {
        return peersToAdd;
    }

    public void setPeersToAdd(List<String> peersToAdd) {
        this.peersToAdd = peersToAdd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPeersRequest that = (AddPeersRequest) o;
        return Objects.equals(peersToAdd, that.peersToAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peersToAdd);
    }

    @Override
    public String toString() {
        return "AddPeersRequest{" +
                "peersToAdd=" + peersToAdd +
                '}';
    }
}
