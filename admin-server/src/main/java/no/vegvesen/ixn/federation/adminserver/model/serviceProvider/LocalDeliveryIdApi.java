package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

public class LocalDeliveryIdApi implements Comparable<LocalDeliveryIdApi> {
    private String id;

    public LocalDeliveryIdApi(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public int compareTo(LocalDeliveryIdApi o) {
        if(id == null && o.id == null){
            return 0;
        }

        if(o.id == null){
            return -1;
        }

        if(id == null){
            return 1;
        }
        return id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return "ServiceProviderDeliveryApi{" +
                "id=" + id +
                "}";
    }
}
