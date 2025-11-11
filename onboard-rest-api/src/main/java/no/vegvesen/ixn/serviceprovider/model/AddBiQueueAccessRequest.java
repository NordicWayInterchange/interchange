package no.vegvesen.ixn.serviceprovider.model;

public class AddBiQueueAccessRequest {
    private boolean access = false;

    public AddBiQueueAccessRequest() {
    }

    public AddBiQueueAccessRequest(boolean biconsumer) {
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
        return "AddBiQueueAccessRequest {" +
                "access=" + access +
                '}';
    }
}
