package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiAccessRequest {

    private Boolean access = false;

    public ServiceProviderBiAccessRequest() {
    }

    public ServiceProviderBiAccessRequest(Boolean biConsumer) {
        this.access = biConsumer;

    }

    public Boolean isAccess() {
        return access;
    }

    public void setAccess(Boolean access) {
        this.access = access;
    }


    @Override
    public String toString() {
        return "ServiceProvider {" +
                "access=" + access +
                '}';
    }
}
