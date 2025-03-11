package no.vegvesen.ixn.federation.adminserver.model.serviceProvider;

import org.jetbrains.annotations.NotNull;

public class ServiceProviderNameApi  implements Comparable<ServiceProviderNameApi> {

    private Integer id;
    private String name;

    public ServiceProviderNameApi(
            Integer id,
            String name) {
        this.id = id;
        this.name = name;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(@NotNull ServiceProviderNameApi serviceProviderNameApi) {
        if(id == null && serviceProviderNameApi.id == null) {
            return 0;
        }

        if(serviceProviderNameApi.id == null){
            return 1;
        }

        if(id == null) {
            return -1;
        }
        return Long.compare(id, serviceProviderNameApi.id);
    }

    @Override
    public String toString() {
        return "serviceProviderApi{" +
                "id=" + id +
                "name='" + name +
                '}';
    }

}
