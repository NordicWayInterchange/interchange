package no.vegvesen.ixn.serviceprovider.model;

public class AddBiqueueAccessRequest {
    private boolean access = false;

    public AddBiqueueAccessRequest() {
    }

    public AddBiqueueAccessRequest(boolean biconsumer) {
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
        return "AddBiqueueAccessRequest {" +
                "access=" + access +
                '}';
    }
}
