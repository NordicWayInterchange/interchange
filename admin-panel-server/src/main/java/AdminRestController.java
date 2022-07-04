import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

public class AdminRestController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final NeighbourRepository neighbourRepository;
    private final CertService certService;
    private final InterchangeNodeProperties nodeProperties;

    private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
    private Logger logger = LoggerFactory.getLogger(AdminRestController.class);
    private TypeTransformer typeTransformer = new TypeTransformer();


    @Autowired
    public AdminRestController(ServiceProviderRepository serviceProviderRepository,
                               NeighbourRepository neighbourRepository,
                               CertService certService,
                               InterchangeNodeProperties nodeProperties) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.certService = certService;
        this.nodeProperties = nodeProperties;
    }

    /*
    KRAV 3

    Henter ut alle naboene til interchangen

    Input: get request

    Output: A list of neighbours connected to the interchange

    QUESTION:
     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/neighbour", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetAllNeighboursResponse getAllNeighbours() {
        //TODO: Add certificate check for admin
        GetAllNeighboursResponse response = typeTransformer.getAllNeighboursResponse(neighbourRepository);
        return response;
    }

    /*
    KRAV 3.1

    Gjort om listCapabilities fra OnboardRestController til å ta capabilities fra en Service provider, til å ta fra en neighbour
    Antar at neighbor er en annen interchange

    Input: Name of neighbour

    Output: A set of the capabilities of the given neighbour

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/neighbour/{neighbourName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListNeighbourCapabilitiesResponse getCapabilitiesFromNeighbour(@PathVariable String neighbourName) {
        //TODO: Add certificate check for admin
        Neighbour neighbour = neighbourRepository.findByName(neighbourName);
        Set<Capability> capabilities = neighbour.getCapabilities().getCapabilities();

        //Funker dette???
        ListNeighbourCapabilitiesResponse response = typeTransformer.listNeighbourCapabilitiesResponse(neighbourName, capabilities);
        //TODO: Transform to response
        return response;
    }


 /*
    KRAV 3.2

    Bruker neighbour.getOurRequestedSubscriptions().getSubscriptions() til å få ut alle subscriptions. Er dette riktig?

    Input: Name of neighbour

    Output: A set of the subscriptions of the given neighbour

     QUESTION: Skal Subscriptions gjøres om til LocalActorSubscription?

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/neighbour/{neighbourName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListNeighbourSubscriptionResponse getSubscriptionsFromNeighbour(@PathVariable String neighbourName) {
        //TODO: Add certificate check for admin
        Neighbour neighbour = neighbourRepository.findByName(neighbourName);
        Set<Subscription> ourSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions();
        Set<Subscription> theirSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getSubscriptions();

        //TODO: Transform to response
       ListNeighbourSubscriptionResponse response = typeTransformer.transformOurAndTheirSubscriptionsToListSubscriptionResponse(neighbourName, ourSubscriptions, theirSubscriptions);
        return response;
    }

    /*
    Todo: Is the neighbour reachable just because its in the repository?
     */

     /*
    KRAV 3.3


    Prøver å sjekke om nabo er oppe ved å sjekke repository

    Input: Name of neighbour

    Output: A boolean indicating if a neighbour is reachable

     QUESTION: Is the neighbour reachable just because its in the repository?

    */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/neighbour/{neighbourName}/isReachable", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isNeighbourReachable (@PathVariable String neighbourName){

        List<Neighbour> tempListOfNeighbours = neighbourRepository.findAll();


        for (Neighbour n :
                tempListOfNeighbours) {
            if(n.getName().equals(neighbourName)) {
                return true;
            }
        }
        return true;
    }

     /*
    Krav 4

    Input: Name of ServiceProvider

    Output: A set of all the service providers connected to the interchange.

     QUESTION: What format should the response containing list of serviceProviders be in?

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/serviceProvider", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetAllServiceProvidersResponse getAllServiceProviders() {
        //TODO: Add certificate check for admin
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        return new GetAllServiceProvidersResponse(serviceProviders);
    }

    /*
    KRAV 4

    Prøver å få ut en service provider basert på navn.

    Hva slags informasjon får man ut her?

    Input: Name of service provider

    Output: A service provider object

     QUESTION: What format should the ServiceProvider response be in?

     */


    @RequestMapping(method = RequestMethod.GET, path = "/admin/serviceProvider/{serviceProviderName}", produces = MediaType.APPLICATION_JSON_VALUE)
    private GetServiceProviderResponse getServiceProvider(@PathVariable String serviceProviderName) {
        //TODO: Add certificate check for admin
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }

        return new GetServiceProviderResponse(serviceProvider);
    }


    /*
    Krav 4.1

    Input: Name of ServiceProvider

    Output: A set of the capabilities of the given Service provider

     */
    @RequestMapping(method = RequestMethod.GET, path = "/admin/serviceProvider/{serviceProviderName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListCapabilitiesResponse getCapabilitiesFromServiceProvider(@PathVariable String serviceProviderName) {
        //TODO: Add certificate check for admin
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        Set<Capability> capabilities = serviceProvider.getCapabilities().getCapabilities();
        ListCapabilitiesResponse response = typeTransformer.listCapabilitiesResponse(serviceProviderName, capabilities);
        return response;
    }
    /*

    Krav 4.2

    Input: Name of ServiceProvider

    Output: A set of the subscriptions of the given Service provider

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/serviceProvider/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListSubscriptionsResponse getSubscriptionsFromServiceProvider(@PathVariable String serviceProviderName) {
        //TODO: Add certificate check for admin
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        ListSubscriptionsResponse response = typeTransformer.transformLocalSubscriptionsToListSubscriptionResponse(serviceProviderName,serviceProvider.getSubscriptions());
        return response;
    }

    /*
    Krav 4.3

    Input: Name of ServiceProvider

    Output: A set of the deliveries of the given Service provider

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/serviceProvider/serviceProvider/{serviceProviderName}/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListDeliveriesResponse getDeliveriesFromServiceProvider(@PathVariable String serviceProviderName){
        //TODO: Add certificate check for admin
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        ListDeliveriesResponse response = typeTransformer.transformToListDeliveriesResponse(serviceProviderName, serviceProvider.getDeliveries());
        return response;

    }

    /*
    Krav 5

    Input:

    Output:

     */

    //TODO: Signering av Sertifikater
    public void signCSRForServiceProvider(){
        return;
    }
}
