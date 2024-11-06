package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;

public class DeletePeerRequest {

    private String peerName;

    public DeletePeerRequest() {
    }

    public DeletePeerRequest(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeletePeerRequest that = (DeletePeerRequest) o;
        return Objects.equals(peerName, that.peerName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(peerName);
    }

    @Override
    public String toString() {
        return "DeletePeerRequest{" +
                "peerName='" + peerName + '\'' +
                '}';
    }
}
