package no.vegvesen.ixn.serviceprovider.model;

import java.util.Objects;
import java.util.Set;

public class PrivateChannelRequestApi {
    private Set<String> peers;

    private String description;

    public PrivateChannelRequestApi() {
    }

    public PrivateChannelRequestApi(Set<String> peers, String description) {
        this.peers = peers;
        this.description = description;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelRequestApi that = (PrivateChannelRequestApi) o;
        return Objects.equals(peers, that.peers) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peers, description);
    }

    @Override
    public String toString() {
        return "PrivateChannelRequestApi{" +
                "peers=" + peers +
                ", description='" + description + '\'' +
                '}';
    }
}
