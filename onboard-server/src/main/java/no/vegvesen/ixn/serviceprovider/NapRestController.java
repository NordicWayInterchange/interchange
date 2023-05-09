package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.GetSubscriptionsResponse;
import no.vegvesen.ixn.napcore.model.GetMatchingCapabilitiesResponse;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.Provider;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestController
public class NapRestController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final NeighbourRepository neighbourRepository;
    private final CertService certService;
    private final InterchangeNodeProperties nodeProperties;
    private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
    private Logger logger = LoggerFactory.getLogger(NapRestController.class);
    private TypeTransformer typeTransformer = new TypeTransformer();

    @Autowired
    public NapRestController(
            ServiceProviderRepository serviceProviderRepository,
            NeighbourRepository neighbourRepository,
            CertService certService,
            InterchangeNodeProperties nodeProperties) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.certService = certService;
        this.nodeProperties = nodeProperties;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{actorCommonName}/x509/csr", produces = MediaType.APPLICATION_JSON_VALUE)
    public String addCsrRequest() {
        //Check NapCore certificate
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/{actorCommonName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Subscription addSubscription(@PathVariable String actorCommonName, @RequestBody SubscriptionRequest subscriptionRequest) {
        logger.info("Subscription - Received POST from Service Provider: {}", actorCommonName);
        //Check NapCore certificate

        if (Objects.isNull(subscriptionRequest)) {
            throw new SubscriptionRequestException("Bad api object for Subscription Request, Subscription has no selector.");
        }

        LocalSubscription localSubscription = typeTransformer.transformNapSubscriptionToLocalSubscription(subscriptionRequest, nodeProperties.getName());
        if (JMSSelectorFilterFactory.isValidSelector(localSubscription.getSelector())) {
            localSubscription.setStatus(LocalSubscriptionStatus.REQUESTED);
        } else {
            localSubscription.setStatus(LocalSubscriptionStatus.ILLEGAL);
        }

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        serviceProvider.addLocalSubscription(localSubscription);

        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProvider);
        logger.debug("Updated Service Provider: {}", savedServiceProvider.toString());

        LocalSubscription savedSubscription = savedServiceProvider
                .getSubscriptions()
                .stream()
                .filter(subscription -> subscription.equals(localSubscription))
                .findFirst()
                .get();

        return null;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{actorCommonName}/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
    public GetSubscriptionsResponse getSubscriptions(@PathVariable String actorCommonName) {
        logger.info("Listing subscription for service provider {}", actorCommonName);
        //Check NapCore certificate

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);


        return null;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{actorCommonName}/subscriptions/{subscriptionId}")
    public Subscription getSubscription(@PathVariable String actorCommonName, @PathVariable String subscriptionId) {
        logger.info("Getting subscription {} for service provider {}", subscriptionId, actorCommonName);
        //Check NapCore certificate

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        LocalSubscription localSubscription = serviceProvider.getSubscriptions()
                .stream()
                .filter(s -> s.getId().equals(Integer.parseInt(subscriptionId)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Could not find subscription with ID %s for service provider %s",subscriptionId,actorCommonName)));

        return typeTransformer.transformLocalSubscriptionToNapSubscription(localSubscription);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/{actorCommonName}/subscriptions/{subscriptionId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable String actorCommonName, @PathVariable String subscriptionId) {
        logger.info("Service Provider {}, DELETE subscription {}", actorCommonName, subscriptionId);
        //Check NapCore certificate

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalSubscription(Integer.parseInt(subscriptionId));

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated Service Provider: {}", saved.toString());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{actorCommonName}/subscriptions/capabilites")
    public GetMatchingCapabilitiesResponse getMatchingSubscriptionCapabilities(@PathVariable String actorCommonName, @RequestParam(required = false) String selector) {
        logger.info("List network capabilities for serivce provider {}",actorCommonName);
        //Check NapCore certificate

        Set<CapabilitySplit> allCapabilities = getAllNeighbourCapabilities();
        allCapabilities.addAll(getAllLocalCapabilities());
        if (selector != null) {
            if (!selector.isEmpty()) {
                allCapabilities = getAllMatchingCapabilities(selector, allCapabilities);
            }
        }

        return null;
    }

    private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

    private Set<CapabilitySplit> getAllMatchingCapabilities(String selector, Set<CapabilitySplit> allCapabilities) {
        return CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, selector);
    }

    private Set<CapabilitySplit> getAllLocalCapabilities() {
        Set<CapabilitySplit> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    private Set<CapabilitySplit> getAllNeighbourCapabilities() {
        Set<CapabilitySplit> capabilities = new HashSet<>();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for (Neighbour neighbour : neighbours) {
            capabilities.addAll(neighbour.getCapabilities().getCapabilities());
        }
        return capabilities;
    }
}
