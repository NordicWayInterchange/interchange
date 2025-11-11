package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiQueueAccessResponse {

    private String name;

    private boolean access = false;

    public ServiceProviderBiQueueAccessResponse() {
    }

    public ServiceProviderBiQueueAccessResponse(String name,
                                                boolean biconsumer) {
        this.name = name;
        this.access = biconsumer;
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
        return "ServiceProviderBiAccessResponse {" +
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}

