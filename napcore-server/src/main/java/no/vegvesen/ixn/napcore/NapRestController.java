package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.CapabilityValidator;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.properties.NapCoreProperties;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class NapRestController {

    private final ServiceProviderRepository serviceProviderRepository;
    private final NeighbourRepository neighbourRepository;
    private final CertService certService;
    private final NapCoreProperties napCoreProperties;
    private final CapabilityToCapabilityApiTransformer capabilityToCapabilityApiTransformer;
    private CapabilityToCapabilityApiTransformer capabilityApiTransformer = new CapabilityToCapabilityApiTransformer();
    private Logger logger = LoggerFactory.getLogger(NapRestController.class);
    private TypeTransformer typeTransformer = new TypeTransformer();

    private CertSigner certSigner;

    @Autowired
    public NapRestController(
            ServiceProviderRepository serviceProviderRepository,
            NeighbourRepository neighbourRepository,
            CertService certService,
            NapCoreProperties napCoreProperties,
            CertSigner certSigner, CapabilityToCapabilityApiTransformer capabilityToCapabilityApiTransformer) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.certService = certService;
        this.napCoreProperties = napCoreProperties;
        this.certSigner = certSigner;
        this.capabilityToCapabilityApiTransformer = capabilityToCapabilityApiTransformer;
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/x509/csr"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public CertificateSignResponse addCsrRequest(@PathVariable("actorCommonName") String actorCommonName, @RequestBody CertificateSignRequest signRequest) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("CSR - signing new cert for actor {}", actorCommonName);
        List<String> certs;
        String csr = new String(Base64.getDecoder().decode(signRequest.getCsr()));
        try {
            certs = certSigner.sign(csr,actorCommonName);
        } catch (IOException | OperatorCreationException | CertificateException | NoSuchAlgorithmException |
                 SignatureException | InvalidKeyException | NoSuchProviderException e) {
            logger.info("Error signing CSR for Service Provider {}, {}", actorCommonName, e);
            throw new SignExeption("Could not sign csr",e);
        }
        List<String> encodedCerts = certs.stream().map(s -> Base64.getEncoder().encodeToString(s.getBytes())).collect(Collectors.toList());
        return new CertificateSignResponse(encodedCerts);
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/subscriptions"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Subscription addSubscription(@PathVariable("actorCommonName") String actorCommonName, @RequestBody SubscriptionRequest subscriptionRequest) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Subscription - Received POST from Service Provider: {}", actorCommonName);

        if (Objects.isNull(subscriptionRequest) || Objects.isNull(subscriptionRequest.getSelector())) {
            throw new SubscriptionRequestException("Bad api object for Subscription Request, Subscription has no selector.");
        }

        LocalSubscription localSubscription = typeTransformer.transformNapSubscriptionToLocalSubscription(subscriptionRequest, napCoreProperties.getName());
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

        return typeTransformer.transformLocalSubscriptionToNapSubscription(savedSubscription);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions", "/nap/{actorCommonName}/subscriptions/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Subscription> getSubscriptions(@PathVariable("actorCommonName") String actorCommonName) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing subscription for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);

        return typeTransformer.transformLocalSubscriptionsToNapSubscriptions(serviceProvider.getSubscriptions());
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions/{subscriptionId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Subscription getSubscription(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("subscriptionId") String subscriptionId) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Getting subscription {} for service provider {}", subscriptionId, actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        LocalSubscription localSubscription = serviceProvider.getSubscriptions()
                .stream()
                .filter(s -> s.getUuid().equals(subscriptionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Could not find subscription with ID %s for service provider %s",subscriptionId,actorCommonName)));

        return typeTransformer.transformLocalSubscriptionToNapSubscription(localSubscription);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = {"/nap/{actorCommonName}/subscriptions/{subscriptionId}"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteSubscription(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("subscriptionId") String subscriptionId) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE subscription {}", actorCommonName, subscriptionId);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalSubscription(subscriptionId);

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated Service Provider: {}", saved.toString());
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions/capabilities" }, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<no.vegvesen.ixn.napcore.model.Capability> getMatchingSubscriptionCapabilities(@PathVariable("actorCommonName") String actorCommonName, @RequestParam(required = false, name = "selector") String selector) {
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List network capabilities for serivce provider {}",actorCommonName);

        Set<Capability> allCapabilities = getAllNeighbourCapabilities();
        allCapabilities.addAll(getAllLocalCapabilities());
        if (selector != null) {
            if (!selector.isEmpty()) {
                allCapabilities = getAllMatchingCapabilities(selector, allCapabilities);
            }
        }

        return typeTransformer.transformCapabilitiesToGetMatchingCapabilitiesResponse(allCapabilities);
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/deliveries"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Delivery addDelivery(@PathVariable("actorCommonName") String actorCommonName, @RequestBody DeliveryRequest deliveryRequest){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Delivery - Received POST From Service Provider {}", actorCommonName);

        if(Objects.isNull(deliveryRequest) || Objects.isNull(deliveryRequest.getSelector())){
            throw new DeliveryPostException("Bad api object for Delivery Request, Delivery has no selector");
        }
        LocalDelivery localDelivery = typeTransformer.transformNapDeliveryToLocalDelivery(deliveryRequest);

        if(JMSSelectorFilterFactory.isValidSelector(localDelivery.getSelector())){
            localDelivery.setStatus(LocalDeliveryStatus.REQUESTED);
        }
        else{
            localDelivery.setStatus(LocalDeliveryStatus.ILLEGAL);
        }

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        serviceProvider.addDelivery(localDelivery);

        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProvider);
        logger.debug("Updated Service Provider: {}", savedServiceProvider);

        LocalDelivery savedDelivery = savedServiceProvider
                .getDeliveries()
                .stream()
                .filter(delivery -> delivery.equals(localDelivery))
                .findFirst()
                .get();

        return typeTransformer.transformLocalDeliveryToNapDelivery(savedDelivery);
    }

    @RequestMapping(method = RequestMethod.GET, path={"/nap/{actorCommonName}/deliveries/{deliveryId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public Delivery getDelivery(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Getting subscription {} for service provider {}", deliveryId, actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        LocalDelivery localDelivery = serviceProvider.getDeliveries().stream()
                .filter(d->d.getUuid().equals(deliveryId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Could not find delivery with Id %s for service provider %s", deliveryId, actorCommonName)));

        return typeTransformer.transformLocalDeliveryToNapDelivery(localDelivery);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/deliveries"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Delivery> getDeliveries(@PathVariable("actorCommonName") String actorCommonName){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing deliveries for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);

        return typeTransformer.transformLocalDeliveriesToNapDeliveries(serviceProvider.getDeliveries());
    }

    @RequestMapping(method = RequestMethod.DELETE, path={"/nap/{actorCommonName}/deliveries/{deliveryId}"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteDelivery(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE delivery {}", actorCommonName, deliveryId);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalDelivery(deliveryId);

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated service provider: {}", saved);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/deliveries/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<no.vegvesen.ixn.napcore.model.Capability> getMatchingDeliveryCapabilities(@PathVariable("actorCommonName") String actorCommonName, @RequestParam(required = false, name="selector") String selector){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List local capabilities for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        Set<Capability> allCapabilities = serviceProvider.getCapabilities().getCapabilities();
        if(selector != null){
            if(!selector.isEmpty()){
                allCapabilities = getAllMatchingCapabilities(selector, allCapabilities);
            }
        }

        return typeTransformer.transformCapabilitiesToGetMatchingCapabilitiesResponse(allCapabilities);
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public OnboardingCapability addCapability(@PathVariable("actorCommonName") String actorCommonName, @RequestBody CapabilitiesRequest capabilitiesRequest){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Capability - Received POST from Service Provider: {}", actorCommonName);

        if(Objects.isNull(capabilitiesRequest) || Objects.isNull(capabilitiesRequest.getApplication()) || Objects.isNull(capabilitiesRequest.getMetadata())){
            throw new CapabilityPostException("Bad api object for Capability Request, object can not be null");
        }

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        Capability capabilityToAdd = typeTransformer.transformCapabilitiesRequestToCapability(capabilitiesRequest);
        if(allPublicationIds().contains(capabilityToAdd.getApplication().getPublicationId())){
            throw new CapabilityPostException(String.format("Bad api object. The publicationId for capability %s must be unique", capabilitiesRequest));
        }
        if(!CapabilityValidator.quadTreeIsValid(capabilityToAdd)){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s has invalid quadtree %s", capabilitiesRequest, capabilitiesRequest.getApplication().getQuadTree()));
        }
        Set<String> capabilityProperties = CapabilityValidator.capabilityIsValid(capabilityToCapabilityApiTransformer.capabilityToCapabilityApi(capabilityToAdd));
        if(!capabilityProperties.isEmpty()){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s is missing properties %s", capabilitiesRequest, capabilityProperties));
        }

        serviceProviderToUpdate.getCapabilities().addDataType(capabilityToAdd);
        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProviderToUpdate);
        Capability savedCapability = savedServiceProvider.getCapabilities().getCapabilities()
                .stream()
                .filter(a->a.equals(capabilityToAdd))
                .findFirst().get();

        logger.info("Returning updated Service Provider: {}", savedServiceProvider);
        return typeTransformer.transformCapabilityToOnboardingCapability(savedCapability);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<OnboardingCapability> getCapabilities(@PathVariable("actorCommonName") String actorCommonName){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List capabilities for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);

        return typeTransformer.transformCapabilityListToOnboardingCapabilityList(serviceProvider.getCapabilities().getCreatedCapabilities());
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/capabilities/{capabilityId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public OnboardingCapability getCapability(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("capabilityId") String capabilityId){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Get capability {} for service provider {}", capabilityId, actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        Capability capability = serviceProvider.getCreatedCapability(parseInteger(capabilityId));
        return typeTransformer.transformCapabilityToOnboardingCapability(capability);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = {"/nap/{actorCommonName}/capabilities/{capabilityId}"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCapability(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("capabilityId") String capabilityId){
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Received request to delete capability {} from Service Provider: {}", capabilityId, actorCommonName);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.getCapabilities().removeDataType(parseInteger(capabilityId));
        serviceProviderRepository.save(serviceProviderToUpdate);
        logger.info("Updated service provider {}", serviceProviderToUpdate);
    }

    private Integer parseInteger(String unparsedInt){
        try {
            return Integer.parseInt(unparsedInt);
        }
        catch (Exception e){
            throw new NotFoundException(String.format("Could not find capability with Id %s", unparsedInt));
        }
    }

    private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

    private Set<String> allPublicationIds(){
        Set<Capability> allCapabilities = getAllLocalCapabilities();
        allCapabilities.addAll(getAllNeighbourCapabilities());
        return allCapabilities.stream().map(a->a.getApplication().getPublicationId()).collect(Collectors.toSet());
    }

    private Set<Capability> getAllMatchingCapabilities(String selector, Set<Capability> allCapabilities) {
        return CapabilityMatcher.matchCapabilitiesToSelector(allCapabilities, selector);
    }

    private Set<Capability> getAllLocalCapabilities() {
        Set<Capability> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
        for (ServiceProvider otherServiceProvider : serviceProviders) {
            capabilities.addAll(otherServiceProvider.getCapabilities().getCapabilities());
        }
        return capabilities;
    }

    private Set<Capability> getAllNeighbourCapabilities() {
        Set<Capability> capabilities = new HashSet<>();
        List<Neighbour> neighbours = neighbourRepository.findAll();
        for (Neighbour neighbour : neighbours) {
            capabilities.addAll(Capability.transformNeighbourCapabilityToCapability(neighbour.getCapabilities().getCapabilities()));
        }
        return capabilities;
    }
}
