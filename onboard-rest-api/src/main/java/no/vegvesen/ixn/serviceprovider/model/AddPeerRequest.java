package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class AddPeerRequest {
    String peerToAdd;

    public AddPeerRequest() {
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
        return Objects.hashCode(peerToAdd);
    }

    @Override
    public String toString() {
        return "AddPeerRequest{" +
                "peerToAdd='" + peerToAdd + '\'' +
                '}';
    }
}
