package no.vegvesen.ixn.serviceprovider.model;

public class BiQueueAccessResponse {
    private String name;

    private boolean access = false;

    public BiQueueAccessResponse() {
    }

    public BiQueueAccessResponse(String name,
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
        return "GetBiQueueAccessResponse {" +
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}
