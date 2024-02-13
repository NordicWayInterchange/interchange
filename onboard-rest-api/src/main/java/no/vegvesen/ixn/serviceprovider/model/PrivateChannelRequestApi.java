package no.vegvesen.ixn.serviceprovider.model;

public class PrivateChannelRequestApi {
    private String peerName;

    public PrivateChannelRequestApi() {
    }

    public PrivateChannelRequestApi(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }
    public String toString(){
        return String.format("""
                PrivateChannelRequestApi{
                "peerName"  %s
                }
                """, peerName);
    }
}
