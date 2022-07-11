package no.vegvesen.ixn.admin;

import no.vegvesen.ixn.federation.model.*;

import java.util.Set;

public class GetServiceProviderResponse {

    private String nameOfInterchange;

    private ServiceProvider serviceProvider;


    public GetServiceProviderResponse(){



    }


    public GetServiceProviderResponse(String nameOfInterchange, ServiceProvider serviceProvider){

        this.nameOfInterchange = nameOfInterchange;
        this.serviceProvider = serviceProvider;


    }

    public String getNameOfInterchange() {
        return nameOfInterchange;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }
}


