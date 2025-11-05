package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiAccessResponse {

    private String name;

    private Boolean access = false;

    public ServiceProviderBiAccessResponse() {
    }

    public ServiceProviderBiAccessResponse(String name,
                                           Boolean biConsumer) {
        this.name = name;
        this.access = biConsumer;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}

