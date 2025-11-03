package no.vegvesen.ixn.napcore.model;

import java.util.Arrays;

public class ServiceProviderResponse {

    private String name;

    private boolean access = false;

    public ServiceProviderResponse() {
    }

    public ServiceProviderResponse(String name,
                                   boolean biConsumer) {
        this.name = name;
        this.access = biConsumer;

    }


    @Override
    public String toString() {
        return "ServiceProvider {" +
                "name='" + name + '\'' +
                ", access=" + access +
                '}';
    }
}

