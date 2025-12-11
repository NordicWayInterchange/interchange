package no.vegvesen.ixn.serviceprovider.model;

public class BiqueueAccessResponse {
    private String name;

    private Boolean access = false;

    public BiqueueAccessResponse() {
    }

    public BiqueueAccessResponse(String name,
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
        return "BiqueueAccessResponse {" +
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}
