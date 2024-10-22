package no.vegvesen.ixn.napcore.model;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelRequest {

    private Set<String> peers;

    public PrivateChannelRequest() {

    }

    public PrivateChannelRequest(Set<String> peers) {
        this.peers = peers;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelRequest that = (PrivateChannelRequest) o;
        return Objects.equals(peers, that.peers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peers);
    }

    @Override
    public String toString() {
        return "PrivateChannelRequest{" +
                "peers='" + peers + '\'' +
                '}';
    }
}
