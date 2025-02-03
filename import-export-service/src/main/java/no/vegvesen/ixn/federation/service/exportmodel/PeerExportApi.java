package no.vegvesen.ixn.federation.service.exportmodel;

import java.util.Objects;

public class PeerExportApi {
    private String name;
    private String uuid;
    private String peerstatus;

    public PeerExportApi() {
    }

    public PeerExportApi(String name, String uuid, String peerstatus) {
        this.name = name;
        this.uuid = uuid;
        this.peerstatus = peerstatus;
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

    public String getPeerstatus() {
        return peerstatus;
    }

    public void setPeerstatus(String peerstatus) {
        this.peerstatus = peerstatus;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PeerExportApi that = (PeerExportApi) o;
        return Objects.equals(name, that.name) && Objects.equals(uuid, that.uuid) && Objects.equals(peerstatus, that.peerstatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid, peerstatus);
    }
}
