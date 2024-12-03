package no.vegvesen.ixn.federation.service.importmodel;

public class PeerImportApl {
    private String name;
    private String uuid;
    private String peerStatus;

    public PeerImportApl() {
    }

    public PeerImportApl(String name, String uuid, String peerStatus) {
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
