package no.vegvesen.ixn.napcore.model;

import java.util.Objects;

public class PeerRequest {

    private String peerName;

    public PeerRequest() {

    }

    public PeerRequest(String peerName) {
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
        PeerRequest that = (PeerRequest) o;
        return Objects.equals(peerName, that.peerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerName);
    }

    @Override
    public String toString() {
        return "PeerRequest{" +
                "peerName='" + peerName + '\'' +
                '}';
    }
}
