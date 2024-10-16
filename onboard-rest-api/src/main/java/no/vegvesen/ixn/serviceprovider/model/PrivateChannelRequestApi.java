package no.vegvesen.ixn.serviceprovider.model;

import java.util.Set;

public class PrivateChannelRequestApi {
    private Set<String> peers;

    public PrivateChannelRequestApi() {
    }

    public PrivateChannelRequestApi(Set<String> peers) {
        this.peers = peers;
    }

    public Set<String> getPeers() {
        return peers;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }

    public String toString(){
        return String.format("""
                PrivateChannelRequestApi{
                "peers"  %s
                }
                """, peers);
    }
}
