import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.OnboardMDCUtil;
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


    public Logger getLogger() {
        return logger;
    }

    public ResourceStatus getResourceStatus(){
        return null;
    }

    public NeighbourRepository getNeighbourRepository() {
        return neighbourRepository;
    }

    /*
    KRAV 3

    Henter ut alle naboene til interchangen



     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/getNeighbours", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceProvider> getAllNeighbours() {

        Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

        List<ServiceProvider> returnServiceProviders = new ArrayList<>();

        for (ServiceProvider s : serviceProviders) {
            returnServiceProviders.add(s);
        }

        return returnServiceProviders;
    }

    /*
    KRAV 3.1


    Gjort om listCapabilities fra OnboardRestController til å ta capabilities fra en Service provider, til å ta fra en neighbour
    Antar at neighbor er en annen interchange


     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{neighbourName}/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListCapabilitiesResponse listCapabilitiesFromNeighbour(@PathVariable String neighbourName) {
        OnboardMDCUtil.setLogVariables(nodeProperties.getName(), neighbourName);
        certService.checkIfCommonNameMatchesNameInApiObject(neighbourName);
        Neighbour neighbour = neighbourRepository.findByName(neighbourName);
        ListCapabilitiesResponse response = typeTransformer.listCapabilitiesResponse(neighbourName,neighbour.getCapabilities().getCapabilities());
        OnboardMDCUtil.removeLogVariables();
        return response;
    }

 /*
    KRAV 3.2


    Bruker neighbour.getOurRequestedSubscriptions().getSubscriptions() til å få ut alle subscriptions. Er dette riktig?


     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{neighbourName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Subscription> getSubscriptionsFromNeighbour(@PathVariable String neighbourName) {
        OnboardMDCUtil.setLogVariables(nodeProperties.getName(), neighbourName);
        this.certService.checkIfCommonNameMatchesNameInApiObject(neighbourName);
        Neighbour neighbour = neighbourRepository.findByName(neighbourName);
        Set<Subscription> subscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptions();
        return subscriptions;
    }

    /*
    KRAV 4


    Prøver å få ut en service provider basert på navn.

    Hva slags informasjon får man ut her?

     */


    @RequestMapping(method = RequestMethod.GET, path = "/admin/{serviceProviderName}", produces = MediaType.APPLICATION_JSON_VALUE)
    private ServiceProvider getServiceProvider(@PathVariable String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

    /*

    Todo: Is the neighbour reachable just because its in the repository?

     */

     /*
    KRAV 3.3


    Prøver å sjekke om nabo er oppe ved å sjekke repository

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isNeighbourReachable (@PathVariable String name){

        List<Neighbour> tempListOfNeighbours = neighbourRepository.findAll();

        for (Neighbour n :
                tempListOfNeighbours) {
            if(n.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /*
    @RequestMapping(method = RequestMethod.GET, path = "/admin/capabilities/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Capability> getAllCapabilitiesFromAllServiceProviders(){
        Set<Capability> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }
     */

    /*
    Krav 4.1

    Input: Name of ServiceProvider

    Output: A set of the capabilities of the given Service provider

     */
    @RequestMapping(method = RequestMethod.GET, path = "/admin/{name}/capabilities/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<Capability> getCapabilitiesFromServiceProvider(@PathVariable String name) {
        Set<Capability> capabilities = new HashSet<>();
        capabilities.addAll(getServiceProvider(name).getCapabilities().getCapabilities());
        return capabilities;
    }

    /*
    Krav 4.2

    Input: Name of ServiceProvider

    Output: A set of the subscriptions of the given Service provider

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{serviceProviderName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ListSubscriptionsResponse getSubscriptionsFromServiceProvider(@PathVariable String serviceProviderName) {
        OnboardMDCUtil.setLogVariables(nodeProperties.getName(), serviceProviderName);
        this.certService.checkIfCommonNameMatchesNameInApiObject(serviceProviderName);
        ServiceProvider serviceProvider = getServiceProvider(serviceProviderName);
        ListSubscriptionsResponse response = typeTransformer.transformLocalSubscriptionsToListSubscriptionResponse(serviceProviderName,serviceProvider.getSubscriptions());
        OnboardMDCUtil.removeLogVariables();
        return response;
    }

    /*
    Krav 4.3

    Input: Name of ServiceProvider

    Output: A set of the deliveries of the given Service provider

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{name}/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
    public Set<LocalDelivery> getDeliveriesFromServiceProvider(@PathVariable String name){
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(name);
        Set<LocalDelivery> deliveries = new HashSet<>();
        deliveries.addAll(serviceProvider.getDeliveries());
        return deliveries;

    }

    /*
    Krav 5

    Input: Name of ServiceProvider

    Output: A set of the subscriptions of the given Service provider

     */

    //TODO: Signering av Sertifikater
    public boolean signCSRForServiceProvider(){
        return false;
    }

    /*
    Krav 4

    Input: Name of ServiceProvider

    Output: A set of all the service providers connected to the interchange.

     */

    @RequestMapping(method = RequestMethod.GET, path = "/admin/getServiceProviders", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceProvider> getAllServiceProviders() {

        Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

        List<ServiceProvider> returnServiceProviders = new ArrayList<>();

        for (ServiceProvider s : serviceProviders) {
            returnServiceProviders.add(s);
        }

        return returnServiceProviders;
    }
}
