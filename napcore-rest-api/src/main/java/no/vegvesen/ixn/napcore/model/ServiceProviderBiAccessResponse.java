package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiAccessResponse {

    private String name;

    private boolean access = false;

    public ServiceProviderBiAccessResponse() {
    }

    public ServiceProviderBiAccessResponse(String name,
                                           boolean biConsumer) {
        this.name = name;
        this.access = biConsumer;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(boolean access) {
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

