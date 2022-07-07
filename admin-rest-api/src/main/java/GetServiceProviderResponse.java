import no.vegvesen.ixn.federation.model.*;

import java.util.Set;

public class GetServiceProviderResponse {

    private String nameOfInterchange;

    private ServiceProvider serviceProvider;




    public GetServiceProviderResponse(String nameOfInterchange, ServiceProvider serviceProvider){

        this.nameOfInterchange = nameOfInterchange;
        this.serviceProvider = serviceProvider;


    }



    public String getName() {
        return nameOfInterchange;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }




}


