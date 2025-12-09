package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiqueueAccessRequest {

    private Boolean access = false;

    public ServiceProviderBiqueueAccessRequest() {
    }

    public ServiceProviderBiqueueAccessRequest(Boolean biconsumer) {
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
        return "ServiceProviderBiqueueAccessRequest {" +
                "access=" + access +
                '}';
    }
}
