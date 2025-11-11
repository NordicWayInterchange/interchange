package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiAccessRequest {

    private boolean access = false;

    public ServiceProviderBiAccessRequest() {
    }

    public ServiceProviderBiAccessRequest(boolean biconsumer) {
        this.access = biconsumer;

    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
        this.access = access;
    }


    @Override
    public String toString() {
        return "ServiceProviderBiAccessRequest {" +
                "access=" + access +
                '}';
    }
}
