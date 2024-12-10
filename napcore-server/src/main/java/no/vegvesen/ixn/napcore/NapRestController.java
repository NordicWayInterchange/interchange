package no.vegvesen.ixn.napcore;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.vegvesen.ixn.cert.CertSigner;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.capability.CapabilityValidator;
import no.vegvesen.ixn.federation.capability.JMSSelectorFilterFactory;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.DeliveryPostException;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.federation.exceptions.PathVariableException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.PrivateChannelEndpoint;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.*;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class NapRestController {

    private final ServiceProviderRepository serviceProviderRepository;

    private final NeighbourRepository neighbourRepository;

    private final PrivateChannelRepository privateChannelRepository;

    private final CertService certService;

    private final NapCoreProperties napCoreProperties;

    private final CapabilityToCapabilityApiTransformer capabilityToCapabilityApiTransformer;

    private Logger logger = LoggerFactory.getLogger(NapRestController.class);

    private TypeTransformer typeTransformer = new TypeTransformer();

    private static Pattern pattern = Pattern.compile("[a-zA-Z0-9_.@-]+");

    private CertSigner certSigner;

    @Autowired
    public NapRestController(
            ServiceProviderRepository serviceProviderRepository,
            NeighbourRepository neighbourRepository,
            PrivateChannelRepository privateChannelRepository,
            CertService certService,
            NapCoreProperties napCoreProperties,
            CertSigner certSigner, CapabilityToCapabilityApiTransformer capabilityToCapabilityApiTransformer) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.neighbourRepository = neighbourRepository;
        this.privateChannelRepository = privateChannelRepository;
        this.certService = certService;
        this.napCoreProperties = napCoreProperties;
        this.certSigner = certSigner;
        this.capabilityToCapabilityApiTransformer = capabilityToCapabilityApiTransformer;
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/x509/csr"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Certificates")
    @Operation(summary = "Generate certificate")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "\t\n" +
            "OK: returns the signed client certificate and CA certificates. " +
            "The first element contains the client certificate, following any intermediate certificates and finally the root certificate. " +
            "The certificates are base64 encoded PEM files (see RFC-7468: https://www.rfc-editor.org/rfc/rfc7468).", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "")))})
    public CertificateSignResponse addCsrRequest(@PathVariable("actorCommonName") String actorCommonName, @RequestBody CertificateSignRequest signRequest) {
        validatePathVariable(actorCommonName);
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
    @Tag(name = "Subscriptions")
    @Operation(summary = "Add subscription")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.ADDSUBSCRIPTIONRESPONSE)))})
    public Subscription addSubscription(@PathVariable("actorCommonName") String actorCommonName, @RequestBody SubscriptionRequest subscriptionRequest) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Subscription - Received POST from Service Provider: {}", actorCommonName);

        if (Objects.isNull(subscriptionRequest) || Objects.isNull(subscriptionRequest.getSelector())) {
            throw new SubscriptionRequestException("Bad api object for Subscription Request, Subscription is missing selector.");
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
        logger.debug("Updated Service Provider: {}", savedServiceProvider);

        LocalSubscription savedSubscription = savedServiceProvider
                .getSubscriptions()
                .stream()
                .filter(subscription -> subscription.equals(localSubscription))
                .findFirst()
                .get();

        return typeTransformer.transformLocalSubscriptionToNapSubscription(savedSubscription);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions", "/nap/{actorCommonName}/subscriptions/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Subscriptions")
    @Operation(summary = "Get subscriptions")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.LISTSUBSCRIPTIONSRESPONSE)))})
    public List<Subscription> getSubscriptions(@PathVariable("actorCommonName") String actorCommonName) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing subscription for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        List<Subscription> subscriptions= typeTransformer.transformLocalSubscriptionsToNapSubscriptions(serviceProvider.getSubscriptions());
        Collections.sort(subscriptions);
        return subscriptions;
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions/{subscriptionId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Subscriptions")
    @Operation(summary = "Get subscription")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETSUBSCRIPTIONRESPONSE)))})
    public Subscription getSubscription(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("subscriptionId") String subscriptionId) {
        validatePathVariable(actorCommonName);
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
    @Tag(name = "Subscriptions")
    @Operation(summary = "Delete subscription")
    public void deleteSubscription(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("subscriptionId") String subscriptionId) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE subscription {}", actorCommonName, subscriptionId);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalSubscription(subscriptionId);

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated Service Provider: {}", saved);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/subscriptions/capabilities" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Subscriptions")
    @Operation(summary = "Get capabilities matching subscription")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETSUBSCRIPTIONCAPABILITYRESPONSE)))})
    public List<no.vegvesen.ixn.napcore.model.Capability> getMatchingSubscriptionCapabilities(@PathVariable("actorCommonName") String actorCommonName, @RequestParam(required = false, name = "selector") String selector) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List network capabilities for serivce provider {}",actorCommonName);

        Set<Capability> localCapabilities = getAllLocalCapabilities();
        Set<NeighbourCapability> neighbourCapabilities = getAllNeighbourCapabilities();
        if (selector != null) {
            if (!selector.isEmpty()) {
                localCapabilities = getAllMatchingLocalCapabilities(selector, localCapabilities);
                neighbourCapabilities = getAllMatchingNeighbourCapabilities(selector, neighbourCapabilities);
            }
        }
        List<no.vegvesen.ixn.napcore.model.Capability> capabilities = typeTransformer.transformCapabilitiesToGetMatchingCapabilitiesResponse(localCapabilities, neighbourCapabilities);
        Collections.sort(capabilities);
        return capabilities;
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/deliveries"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Deliveries")
    @Operation(summary = "Add delivery")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.ADDDELIVERIESRESPONSE)))})
    public Delivery addDelivery(@PathVariable("actorCommonName") String actorCommonName, @RequestBody DeliveryRequest deliveryRequest){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Delivery - Received POST From Service Provider {}", actorCommonName);

        if(Objects.isNull(deliveryRequest) || Objects.isNull(deliveryRequest.getSelector())){
            throw new DeliveryPostException("Bad api object for Delivery Request, Delivery is missing selector");
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
    @Tag(name = "Deliveries")
    @Operation(summary = "Get delivery")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETDELIVERYRESPONSE)))})
    public Delivery getDelivery(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId){
        validatePathVariable(actorCommonName);
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
    @Tag(name = "Deliveries")
    @Operation(summary = "Get deliveries")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETDELIVERIESRESPONSE)))})
    public List<Delivery> getDeliveries(@PathVariable("actorCommonName") String actorCommonName){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing deliveries for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        List<Delivery> deliveries = typeTransformer.transformLocalDeliveriesToNapDeliveries(serviceProvider.getDeliveries());
        Collections.sort(deliveries);
        return deliveries;
    }

    @RequestMapping(method = RequestMethod.DELETE, path={"/nap/{actorCommonName}/deliveries/{deliveryId}"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Deliveries")
    @Operation(summary = "Delete delivery")
    public void deleteDelivery(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE delivery {}", actorCommonName, deliveryId);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.removeLocalDelivery(deliveryId);

        ServiceProvider saved = serviceProviderRepository.save(serviceProviderToUpdate);
        logger.debug("Updated service provider: {}", saved);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/deliveries/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Deliveries")
    @Operation(summary = "Get capabilities matching delivery")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETDELIVERYCAPABILITYRESPONSE)))})
    public List<no.vegvesen.ixn.napcore.model.Capability> getMatchingDeliveryCapabilities(@PathVariable("actorCommonName") String actorCommonName, @RequestParam(required = false, name="selector") String selector){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List local capabilities for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        Set<Capability> allCapabilities = serviceProvider.getCapabilities().getCapabilities();
        if(selector != null){
            if(!selector.isEmpty()){
                allCapabilities = getAllMatchingLocalCapabilities(selector, allCapabilities);
            }
        }
        List<no.vegvesen.ixn.napcore.model.Capability> capabilities = typeTransformer.transformCapabilitiesToGetMatchingCapabilitiesResponse(allCapabilities, Collections.emptySet());
        Collections.sort(capabilities);
        return capabilities;
    }

    @RequestMapping(method = RequestMethod.POST, path = {"/nap/{actorCommonName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Capabilities")
    @Operation(summary = "Add capability")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.ADDCAPABILITYRESPONSE)))})
    public OnboardingCapability addCapability(@PathVariable("actorCommonName") String actorCommonName, @RequestBody CapabilitiesRequest capabilitiesRequest){
        validatePathVariable(actorCommonName);
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

        if(!CapabilityValidator.isQuadTreeValid(capabilityToAdd.getApplication().getQuadTree())){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s has invalid quadtree %s", capabilitiesRequest, capabilitiesRequest.getApplication().getQuadTree()));
        }

        Set<String> capabilityProperties = CapabilityValidator.capabilityIsValid(capabilityToCapabilityApiTransformer.capabilityToCapabilityApi(capabilityToAdd));
        if(!capabilityProperties.isEmpty()){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s is missing properties %s", capabilitiesRequest, capabilityProperties));
        }

        if(!CapabilityValidator.capabilityHasValidProperties(new CapabilityApi(capabilitiesRequest.getApplication(), capabilitiesRequest.getMetadata()))){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s contains properties with illegal characters.", capabilityToAdd));
        }

        if(!CapabilityValidator.isShardCountValid(capabilitiesRequest.getMetadata())){
            throw new CapabilityPostException(String.format("Bad api object. The posted capability %s has an invalid shardCount", capabilityToAdd));
        }

        serviceProviderToUpdate.getCapabilities().addCapability(capabilityToAdd);
        ServiceProvider savedServiceProvider = serviceProviderRepository.save(serviceProviderToUpdate);
        Capability savedCapability = savedServiceProvider.getCapabilities().getCapabilities()
                .stream()
                .filter(a->a.equals(capabilityToAdd))
                .findFirst().get();

        logger.info("Returning updated Service Provider: {}", savedServiceProvider);
        return typeTransformer.transformCapabilityToOnboardingCapability(savedCapability);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/capabilities"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Capabilities")
    @Operation(summary = "Get capabilities")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.LISTCAPABILITIESRESPONSE)))})
    public List<OnboardingCapability> getCapabilities(@PathVariable("actorCommonName") String actorCommonName){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("List capabilities for service provider {}", actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        List<OnboardingCapability> capabilities = typeTransformer.transformCapabilityListToOnboardingCapabilityList(serviceProvider.getCapabilities().getCreatedCapabilities());
        Collections.sort(capabilities);
        return capabilities;
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/capabilities/{capabilityId}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Capabilities")
    @Operation(summary = "Get capability")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETCAPABILITYRESPONSE)))})
    public OnboardingCapability getCapability(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("capabilityId") String capabilityId){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Get capability {} for service provider {}", capabilityId, actorCommonName);

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);
        Capability capability = serviceProvider.getCreatedCapability(capabilityId);
        return typeTransformer.transformCapabilityToOnboardingCapability(capability);
    }

    @RequestMapping(method=RequestMethod.GET, path = {"/nap/{actorCommonName}/capabilities/publicationids"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Capabilities")
    @Operation(summary = "Get publicationIds")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETPUBLICATIONIDSRESPONSE)))})
    public Set<String> getPublicationIds(@PathVariable("actorCommonName") String actorCommonName){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Received request for publicationIds from Service Provider: {}", actorCommonName);

        return allPublicationIds();
    }

    @RequestMapping(method = RequestMethod.DELETE, path = {"/nap/{actorCommonName}/capabilities/{capabilityId}"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Capabilities")
    @Operation(summary = "Delete capability")
    public void deleteCapability(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("capabilityId") String capabilityId){
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Received request to delete capability {} from Service Provider: {}", capabilityId, actorCommonName);

        ServiceProvider serviceProviderToUpdate = getOrCreateServiceProvider(actorCommonName);
        serviceProviderToUpdate.getCapabilities().removeCapability(capabilityId);
        serviceProviderRepository.save(serviceProviderToUpdate);
        logger.info("Updated service provider {}", serviceProviderToUpdate);
    }


    @RequestMapping(method = RequestMethod.POST, path = "/nap/{actorCommonName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Add private channel")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.ADDPRIVATECHANNELRESPONSE)))})
    public PrivateChannelResponse addPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @RequestBody PrivateChannelRequest request) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("PrivateChannels - Received POST from Service Provider: {}", actorCommonName);

        if (request == null || request.getPeers() == null) {
            throw new PrivateChannelException("Private channel or peers can not be null");
        }

        ServiceProvider serviceProvider = getOrCreateServiceProvider(actorCommonName);

        serviceProviderRepository.save(serviceProvider);

        if (request.getPeers().contains(actorCommonName)) {
            throw new PrivateChannelException("Can't add private channel with serviceProviderName in list of peers");
        }

        Set<Peer> peers = request.getPeers().stream().map(Peer::new).collect(Collectors.toSet());
        PrivateChannel privateChannel = new PrivateChannel(peers, PrivateChannelStatus.REQUESTED, request.getDescription(), actorCommonName);

        String queueName = "priv-"+UUID.randomUUID();
        PrivateChannelEndpoint endpoint = new PrivateChannelEndpoint(napCoreProperties.getName(), Integer.parseInt(napCoreProperties.getMessageChannelPort()), queueName);
        privateChannel.setEndpoint(endpoint);
        privateChannel.setLastUpdated(LocalDateTime.now());

        PrivateChannel savedPrivateChannel = privateChannelRepository.save(privateChannel);
        return typeTransformer.transformPrivateChannelToPrivateChannelResponse(savedPrivateChannel);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/nap/{actorCommonName}/privatechannels/{privateChannelId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Private channels")
    @Operation(summary = "Delete private channel")
    public void deletePrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Service Provider {}, DELETE private channel {}", actorCommonName, privateChannelId);

        PrivateChannel privateChannelToDelete = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, privateChannelId);
        if (privateChannelToDelete == null) {
            throw new NotFoundException("The private channel to delete is not in the Service Provider private channels. Cannot delete private channel that don't exist.");
        }

        privateChannelToDelete.setStatus(PrivateChannelStatus.TEAR_DOWN);
        privateChannelToDelete.setLastUpdated(LocalDateTime.now());
        PrivateChannel updatedPrivateChannel = privateChannelRepository.save(privateChannelToDelete);

        logger.debug("Saved updated private channel {}", updatedPrivateChannel);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/privatechannels"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Get private channels")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETPRIVATECHANNELSRESPONSE)))})
    public List<PrivateChannelResponse> getPrivateChannels(@PathVariable("actorCommonName") String actorCommonName) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Listing private channels for service provider {}", actorCommonName);

        List<PrivateChannel> privateChannels = privateChannelRepository.findAllByServiceProviderName(actorCommonName);
        List<PrivateChannelResponse> response = new ArrayList<>(privateChannels.stream().map(p -> typeTransformer.transformPrivateChannelToPrivateChannelResponse(p)).toList());
        Collections.sort(response);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/nap/{actorCommonName}/privatechannels/{privateChannelId}")
    @Tag(name = "Private channels")
    @Operation(summary = "Get private channel")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETPRIVATECHANNELRESPONSE)))})
    public PrivateChannelResponse getPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Get private channel {} for service provider {}", privateChannelId, actorCommonName);

        PrivateChannel privateChannel = privateChannelRepository.findByServiceProviderNameAndUuid(actorCommonName, privateChannelId);
        if (privateChannel == null) {
            throw new NotFoundException(String.format("Could not find private channel with id %s", privateChannelId));
        }

        return typeTransformer.transformPrivateChannelToPrivateChannelResponse(privateChannel);
    }

    @RequestMapping(method = RequestMethod.GET, path = {"/nap/{actorCommonName}/privatechannels/peer"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Get private channels with actorCommonName as peer")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETPEERPRIVATECHANNELS)))})
    public List<PeerPrivateChannel> getPeerPrivateChannels(@PathVariable("actorCommonName") String actorCommonName) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Get private channels where peer name is {}", actorCommonName);

        List<PrivateChannel> privateChannels = privateChannelRepository.findAllByPeerName(actorCommonName);
        List<PeerPrivateChannel> response = new ArrayList<>(privateChannels.stream().map(p -> typeTransformer.transformPrivateChannelToPeerPrivateChannel(p)).collect(Collectors.toList()));
        Collections.sort(response);
        return response;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/nap/{actorCommonName}/privatechannels/peer/{privateChannelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Get peer private channel")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleApiObjects.GETPEERPRIVATECHANNEL)))})
    public PeerPrivateChannel getPeerPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Get private channel for peer {} where id is {}", actorCommonName, privateChannelId);

        PrivateChannel privateChannel = privateChannelRepository.findByUuidAndPeerName(privateChannelId, actorCommonName);
        if (privateChannel == null) {
            throw new NotFoundException(String.format("Could not find private channel with id %s for peer %s", privateChannelId, actorCommonName));
        }

        return typeTransformer.transformPrivateChannelToPeerPrivateChannel(privateChannel);
    }

    @RequestMapping(method = RequestMethod.PATCH, path = "/nap/{actorCommonName}/privatechannels/peer/{privateChannelId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Private channels")
    @Operation(summary="Add peer to existing private channel")
    public void addPeerToPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId, @RequestBody AddPeerRequest request) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Add peers to private channel where id is {}", privateChannelId);

        if (request == null || request.getPeerToAdd() == null) {
            throw new PrivateChannelException("Cannot add peer when request is empty");
        }

        if(request.getPeerToAdd().equals(actorCommonName)){
            throw new PrivateChannelException("Private channel can not have actorCommonName as peer");
        }

        PrivateChannel privateChannel = privateChannelRepository.findByServiceProviderNameAndUuidAndStatus(actorCommonName, privateChannelId, PrivateChannelStatus.CREATED);
        if (privateChannel == null) {
            throw new NotFoundException(String.format("Could not find private channel with id %s", privateChannelId));
        }

        Set<String> peersInChannel = privateChannel.getPeers().stream().map(Peer::getName).collect(Collectors.toSet());
        if (!peersInChannel.contains(request.getPeerToAdd())) {
            privateChannel.addPeer(new Peer(request.getPeerToAdd()));
            privateChannel.setLastUpdated(LocalDateTime.now());
            PrivateChannel updatedPrivateChannel = privateChannelRepository.save(privateChannel);
            logger.debug("Saved updated private channel {}", updatedPrivateChannel);
        } else {
            logger.debug("Peer is already in private channel with id {}", privateChannelId);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/nap/{actorCommonName}/privatechannels/peer/{privateChannelId}/{peerName}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Private channels")
    @Operation(summary="Delete peer from existing private channel")
    public void deletePeerFromPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId, @PathVariable("peerName") String peerName) {
        validatePathVariable(actorCommonName);
        validatePathVariable(peerName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Delete peer from private channel where id is {} by owner {}", privateChannelId, actorCommonName);

        PrivateChannel privateChannel = privateChannelRepository.findByServiceProviderNameAndUuidAndStatus(actorCommonName, privateChannelId, PrivateChannelStatus.CREATED);
        if (privateChannel == null) {
            throw new NotFoundException(String.format("Could not find private channel with id %s", privateChannelId));
        }

        Peer peerToUpdate = privateChannel.getPeers().stream().filter(peer -> peer.getName().equals(peerName)).findFirst().orElse(null);

        if (peerToUpdate == null) {
            throw new NotFoundException(String.format("Could not find peer with name %s in private channel with id %s", peerName, privateChannelId));
        }

        peerToUpdate.setStatus(PeerStatus.TEAR_DOWN);
        privateChannel.setLastUpdated(LocalDateTime.now());
        PrivateChannel updatedPrivateChannel = privateChannelRepository.save(privateChannel);
        logger.debug("Saved updated private channel {}", updatedPrivateChannel);
    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/nap/{actorCommonName}/privatechannels/peer/{privateChannelId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @Tag(name = "Private channels")
    @Operation(summary="Remove yourself from private channel where you are member")
    public void peerDeletePeerFromPrivateChannel(@PathVariable("actorCommonName") String actorCommonName, @PathVariable("privateChannelId") String privateChannelId) {
        validatePathVariable(actorCommonName);
        this.certService.checkIfCommonNameMatchesNapName(napCoreProperties.getNap());
        logger.info("Delete peer from private channel where id is {} by peer {}", privateChannelId, actorCommonName);

        PrivateChannel privateChannel = privateChannelRepository.findByUuidAndPeerName(privateChannelId, actorCommonName);
        if (privateChannel == null) {
            throw new NotFoundException(String.format("Could not find private channel with id %s for peer %s", privateChannelId, actorCommonName));
        }

        Peer peerToUpdate = privateChannel.getPeers().stream().filter(peer -> peer.getName().equals(actorCommonName)).findFirst().get();
        peerToUpdate.setStatus(PeerStatus.TEAR_DOWN);
        privateChannel.setLastUpdated(LocalDateTime.now());
        PrivateChannel updatedPrivateChannel = privateChannelRepository.save(privateChannel);
        logger.debug("Saved updated private channel {}", updatedPrivateChannel);
    }

    private void validatePathVariable(String pathVariable){
        Matcher matcher = pattern.matcher(pathVariable);
        if(!matcher.matches()){
            throw new PathVariableException(String.format("Path variable %s contains illegal characters", pathVariable));
        }
    }

    private ServiceProvider getOrCreateServiceProvider(String serviceProviderName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(serviceProviderName);
        if (serviceProvider == null) {
            serviceProvider = new ServiceProvider(serviceProviderName);
        }
        return serviceProvider;
    }

    private Set<String> allPublicationIds() {
        Set<String> allPublicationIds = getAllLocalCapabilities().stream()
                .map(c -> c.getApplication().getPublicationId())
                .collect(Collectors.toSet());
        Set<String> neighbourPublicationIds = getAllNeighbourCapabilities().stream()
                .map(c -> c.getApplication().getPublicationId())
                .collect(Collectors.toSet());
        allPublicationIds.addAll(neighbourPublicationIds);
        return allPublicationIds;
    }

    private Set<Capability> getAllMatchingLocalCapabilities(String selector, Set<Capability> allCapabilities) {
        return CapabilityMatcher.matchLocalCapabilitiesToSelector(allCapabilities, selector);
    }

    private Set<NeighbourCapability> getAllMatchingNeighbourCapabilities(String selector, Set<NeighbourCapability> neighbourCapabilities) {
        return CapabilityMatcher.matchNeighbourCapabilitiesToSelector(neighbourCapabilities, selector);
    }

    private Set<Capability> getAllLocalCapabilities() {
        Set<Capability> capabilities = new HashSet<>();
        List<ServiceProvider> serviceProviders = serviceProviderRepository.findAll();
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
}
