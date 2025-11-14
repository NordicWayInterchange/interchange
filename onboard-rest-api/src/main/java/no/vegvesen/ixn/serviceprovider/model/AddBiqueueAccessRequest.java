package no.vegvesen.ixn.serviceprovider.model;

public class AddBiqueueAccessRequest {
    private boolean access = false;

    public AddBiqueueAccessRequest() {
    }

    public AddBiqueueAccessRequest(Boolean biconsumer) {
        this.access = biconsumer;

    }

    public boolean isAccess() {
        return access;
    }

    public void setAccess(Boolean access) {
        this.access = access;
    }


    @Override
    public String toString() {
        return "AddBiqueueAccessRequest {" +
                "access=" + access +
                '}';
    }
}
