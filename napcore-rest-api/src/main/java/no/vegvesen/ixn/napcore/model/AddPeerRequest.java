package no.vegvesen.ixn.napcore.model;

import java.util.Objects;

public class AddPeerRequest {

    private String peerToAdd;

    public AddPeerRequest() {

    }

    public AddPeerRequest(String peerToAdd) {
        this.peerToAdd = peerToAdd;
    }

    public String getPeerToAdd() {
        return peerToAdd;
    }

    public void setPeerToAdd(String peerToAdd) {
        this.peerToAdd = peerToAdd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddPeerRequest that = (AddPeerRequest) o;
        return Objects.equals(peerToAdd, that.peerToAdd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerToAdd);
    }

    @Override
    public String toString() {
        return "AddPeerRequest{" +
                "peerToAdd='" + peerToAdd + '\'' +
                '}';
    }
}
