package no.vegvesen.ixn.napcore.model;

import java.util.Objects;
import java.util.Set;

public class AddPeersRequest {

    private Set<String> peersToAdd;

    public AddPeersRequest() {

    }

    public AddPeersRequest(Set<String> peersToAdd) {
        this.peersToAdd = peersToAdd;
    }

    public Set<String> getPeersToAdd() {
        return peersToAdd;
    }

    public void setPeersToAdd(Set<String> peersToAdd) {
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
        return Objects.hash(peersToAdd);
    }

    @Override
    public String toString() {
        return "AddPeersRequest{" +
                "peersToAdd=" + peersToAdd +
                '}';
    }
}
