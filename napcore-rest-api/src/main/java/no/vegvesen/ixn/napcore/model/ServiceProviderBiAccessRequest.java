package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiAccessRequest {

    private Boolean access = false;

    public ServiceProviderBiAccessRequest() {
    }

    public ServiceProviderBiAccessRequest(Boolean biconsumer) {
        this.access = biconsumer;

    }

    public Boolean isAccess() {
        return access;
    }

    public void setAccess(Boolean access) {
        this.access = access;
    }


    @Override
    public String toString() {
        return "ServiceProviderBiAccessRequest {" +
                "access=" + access +
                '}';
    }
}
