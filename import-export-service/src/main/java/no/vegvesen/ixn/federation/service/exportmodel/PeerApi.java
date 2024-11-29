package no.vegvesen.ixn.federation.service.exportmodel;

public class PeerApi {
    private final String name;
    private String uuid;
    private String peerStatus;

    public PeerApi(String name) {
        this.name = name;
    }

    public PeerApi(String name, String uuid, String peerStatus) {
        this.name = name;
        this.uuid = uuid;
        this.peerStatus = peerStatus;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPeerStatus() {
        return peerStatus;
    }
}
