package no.vegvesen.ixn.federation.forwarding;

public class StoreDetails {

    private String path;
    private String password;
    private String storeType;
    public StoreDetails(String path, String password, String storeType) {
        this.path = path;
        this.password = password;
        this.storeType = storeType;
    }

    public String getPath() {
        return path;
    }

    public String getPassword() {
        return password;
    }

    public String getStoreType() {
        return storeType;
    }

}
