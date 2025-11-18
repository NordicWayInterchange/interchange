package no.vegvesen.ixn.serviceprovider.model;

public class AddBiqueueAccessRequest {
    private Boolean access = false;

    public AddBiqueueAccessRequest() {
    }

    public AddBiqueueAccessRequest(Boolean biconsumer) {
        this.access = biconsumer;

    }

    public Boolean isAccess() {
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
