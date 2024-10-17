package no.vegvesen.ixn.napcore.model;

public class PrivateChannelRequest {

    private String peerName;

    public PrivateChannelRequest() {

    }

    public PrivateChannelRequest(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    @Override
    public String toString() {
        return "PrivateChannelRequest{" +
                "peerName='" + peerName + '\'' +
                '}';
    }
}
