package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;

public class PeerExportApi {
    private String name;
    private String uuid;
    private String peerStatus;

    public PeerExportApi() {
    }

    public PeerExportApi(String name, String uuid, String peerStatus) {
        this.name = name;
        this.uuid = uuid;
        this.peerStatus = peerStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPeerStatus() {
        return peerStatus;
    }

    public void setPeerStatus(String peerStatus) {
        this.peerStatus = peerStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PeerExportApi that = (PeerExportApi) o;
        return Objects.equals(name, that.name) && Objects.equals(uuid, that.uuid) && Objects.equals(peerStatus, that.peerStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid, peerStatus);
    }
}
