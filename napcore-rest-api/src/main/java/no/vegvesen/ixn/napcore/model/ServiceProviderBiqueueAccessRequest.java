package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiqueueAccessRequest {

    private boolean access = false;

    public ServiceProviderBiqueueAccessRequest() {
    }

    public ServiceProviderBiqueueAccessRequest(Boolean biconsumer) {
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
        return "ServiceProviderBiqueueAccessRequest {" +
                "access=" + access +
                '}';
    }
}
