package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.exchange.ExchangeApi;
import no.vegvesen.ixn.federation.adminserver.model.queue.QueueApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.ServiceProviderApi;
import no.vegvesen.ixn.federation.adminserver.model.neighbour.NeighbourApi;
import no.vegvesen.ixn.federation.adminserver.properties.AdminProperties;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.qpid.Exchange;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class AdminRestController {

    private final TypeTransformer typeTransformer = new TypeTransformer();

    private final NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final CertService certService;

    private final AdminProperties adminProperties;

    private final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    private final QpidService qpidService;

    @Autowired
    public AdminRestController(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository, CertService certService, AdminProperties adminProperties, QpidService qpidService) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.certService = certService;
        this.adminProperties = adminProperties;
        this.qpidService = qpidService;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/neighbours", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<NeighbourApi> getNeighbours(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List neighbours for admin user {}", adminUser);
        List<Neighbour> neighbourList = neighbourRepository.findAll();
        return typeTransformer.neighbourListToNeighbourApiList(neighbourList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ServiceProviderApi> getServiceProviders(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List service provider for admin user {}", adminUser);
        List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAll();
        return typeTransformer.serviceProviderListToServiceProviderApiList(serviceProviderList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/subscriptions/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CapabilityApi> getMatchingSubscriptionCapabilities(@PathVariable("adminUser") String adminUser, @RequestParam(required = false, name = "selector") String selector){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List capabilities matching subscriptions for service provider for admin user {}", adminUser);

        List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAll();
        Set<Capability> localCapabilities = getAllLocalCapabilities(serviceProviderList);
        Set<NeighbourCapability> neighbourCapabilities = getAllNeighbourCapabilities();
        if (selector != null && !selector.isEmpty()) {
            localCapabilities = getAllMatchingLocalCapabilities(selector, localCapabilities);
            neighbourCapabilities = getAllMatchingNeighbourCapabilities(selector, neighbourCapabilities);
        }

        return typeTransformer.capabilitiesToGetMatchingCapabilitiesApiList(localCapabilities, neighbourCapabilities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/deliveries/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CapabilityApi> getMatchingDeliveriesCapabilities(@PathVariable("adminUser") String adminUser, @RequestParam(required = false, name = "selector") String selector){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List capabilities matching deliveries for service provider for admin user {}", adminUser);

        List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAll();
        Map<ServiceProvider, Set<Capability>> serviceProviderCapabilityMatchMap = new HashMap<>();

        for (ServiceProvider serviceProvider : serviceProviderList) {
            serviceProvider = getOrCreateServiceProvider(serviceProvider.getName());
            Set<Capability> capabilities = serviceProvider.getCapabilities().getCapabilities();
            serviceProviderCapabilityMatchMap.put(serviceProvider, new HashSet<>(capabilities));
        }

        if (selector != null && !selector.isEmpty()) {
            for (Set<Capability> capabilities : serviceProviderCapabilityMatchMap.values()) {
                Set<Capability> filteredCapabilities = getAllMatchingLocalCapabilities(selector, capabilities);
                capabilities.clear();
                capabilities.addAll(filteredCapabilities);
            }
        }

        Set<Capability> allFilteredCapabilities = new HashSet<>();
        for (Set<Capability> capabilities : serviceProviderCapabilityMatchMap.values()) {
            allFilteredCapabilities.addAll(capabilities);
        }

        return typeTransformer.capabilitiesToGetMatchingCapabilitiesApiList(allFilteredCapabilities, Collections.emptySet());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/exchanges")
    public List<ExchangeApi> getExchanges(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List exchanges for admin user {}", adminUser);
        List<Exchange> exchangesList = qpidService.getAllExchanges();
        return typeTransformer.exchangeListToExchangeApiList(exchangesList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/queues")
    public List<QueueApi> getQueues(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("List queues for admin user {}", adminUser);
        List<Queue> queuesList = qpidService.getAllQueues();
        return typeTransformer.queueListToQueueApiList(queuesList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/exchanges/{exchangeName}")
    public Boolean exchangeExists(@PathVariable("adminUser") String adminUser, @PathVariable("exchangeName") String exchangeName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("Log - exchange exists - requesting user {}", adminUser);
        return qpidService.exchangeExists(exchangeName);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/queues/{queueName}")
    public Boolean queueExists(@PathVariable("adminUser") String adminUser, @PathVariable("queueName") String queueName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("Log - queue exists - requesting user {}", adminUser);
        return qpidService.queueExists(queueName);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/bindings/{exchangeName}/{queueName}")
    public Boolean bindingExists(@PathVariable("adminUser") String adminUser, @PathVariable("exchangeName") String exchangeName, @PathVariable("queueName") String queueName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        logger.info("Log - binding exists - requesting user {}", adminUser);
        return qpidService.bindingExists(exchangeName, queueName);
    }

    private Set<Capability> getAllLocalCapabilities(List<ServiceProvider> serviceProviders) {
        Set<Capability> capabilities = new HashSet<>();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    private Set<NeighbourCapability> getAllNeighbourCapabilities() {
        Set<NeighbourCapability> capabilities = new HashSet<>();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for (Neighbour neighbour : neighbours) {
            capabilities.addAll(neighbour.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    private Set<Capability> getAllMatchingLocalCapabilities(String selector, Set<Capability> allCapabilities) {
        return CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, selector);
    }

    private Set<NeighbourCapability> getAllMatchingNeighbourCapabilities(String selector, Set<NeighbourCapability> neighbourCapabilities) {
        return CapabilityMatcher.matchNeighbourCapabilitiesToSelector(neighbourCapabilities, selector);
    }

    private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

}
