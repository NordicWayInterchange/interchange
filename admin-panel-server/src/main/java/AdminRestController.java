
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryException;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.*;
import java.util.stream.Collectors;

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

    public ListCapabilitiesResponse getCapabilitiesFromNeighbour(){
        return null;
    }

    public ListOfSubscriptions getSubscriptionsFromNeighbour(){
        return null;
    }

    public boolean isNeighbourReachable (){
        return false;
    }

    public Set<Capability> getCapabilitiesFromServiceProvider(){
        Set<Capability> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    public ListOfSubscriptions getSubscriptionsFromServiceProvider(){
        return null;
    }

    public ListOfDeliveries getDeliveriesFromServiceProvider(){
        return null;
    }

    //TODO: Signering av Sertifikater
    public boolean signCSRForServiceProvider(){
        return false;
    }


    // TODO: Remove
    @RequestMapping(method = RequestMethod.GET, path = "/getServiceProviders", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceProvider> getServiceProviders() {

        Iterable<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();

        List<ServiceProvider> returnServiceProviders = new ArrayList<>();

        for (ServiceProvider s : serviceProviders) {
            returnServiceProviders.add(s);
        }

        return returnServiceProviders;
    }
}
