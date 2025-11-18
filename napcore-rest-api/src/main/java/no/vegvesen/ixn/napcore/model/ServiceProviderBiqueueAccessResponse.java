package no.vegvesen.ixn.napcore.model;

public class ServiceProviderBiqueueAccessResponse {

    private String name;

    private Boolean access = false;

    public ServiceProviderBiqueueAccessResponse() {
    }

    public ServiceProviderBiqueueAccessResponse(String name,
                                                Boolean biconsumer) {
        this.name = name;
        this.access = biconsumer;
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
        return "ServiceProviderBiqueueAccessResponse {" +
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}

