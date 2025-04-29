package no.vegvesen.ixn.federation.adminserver;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.vegvesen.ixn.federation.adminserver.model.endpoint.LocalDeliveryEndpointAdminApi;
import no.vegvesen.ixn.federation.adminserver.model.exchange.ExchangeApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PeerPrivateChannelApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PrivateChannelApi;
import no.vegvesen.ixn.federation.adminserver.model.match.CapabilitiesLinkedDeliveryApi;
import no.vegvesen.ixn.federation.adminserver.model.neighbour.NeighbourApi;
import no.vegvesen.ixn.federation.adminserver.model.queue.QueueApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.*;
import no.vegvesen.ixn.federation.adminserver.model.shard.CapabilityShardAdminApi;
import no.vegvesen.ixn.federation.adminserver.properties.AdminProperties;
import no.vegvesen.ixn.federation.adminserver.qpid.*;
import no.vegvesen.ixn.federation.adminserver.qpid.CapabilityApi;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.PathVariableException;
import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.OutgoingMatch;
import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.PrivateChannelRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
public class AdminRestController {

    private final TypeTransformer typeTransformer = new TypeTransformer();

    private final NeighbourRepository neighbourRepository;

    private final ServiceProviderRepository serviceProviderRepository;

    private final OutgoingMatchRepository outgoingMatchRepository;

    private final CertService certService;

    private final AdminProperties adminProperties;

    private final PrivateChannelRepository privateChannelRepository;

    private final Logger logger = LoggerFactory.getLogger(AdminRestController.class);

    private final QpidService qpidService;

    private static Pattern pattern = Pattern.compile("[a-zA-Z0-9_.@-]+");

    @Autowired
    public AdminRestController(NeighbourRepository neighbourRepository, ServiceProviderRepository serviceProviderRepository,
                               CertService certService, AdminProperties adminProperties, PrivateChannelRepository privateChannelRepository, QpidService qpidService, OutgoingMatchRepository outgoingMatchRepository) {
        this.neighbourRepository = neighbourRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.certService = certService;
        this.adminProperties = adminProperties;
        this.privateChannelRepository = privateChannelRepository;
        this.qpidService = qpidService;
        this.outgoingMatchRepository = outgoingMatchRepository;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/neighbours", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Neighbours")
    @Operation(summary = "Get neighbours")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.LISTNEIGHBOURSRESPONSE)))})
    public List<NeighbourApi> getNeighbours(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("List neighbours for admin user {}", adminUser);
        List<Neighbour> neighbourList = neighbourRepository.findAll();
        return typeTransformer.neighbourListToNeighbourApiList(neighbourList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Service providers")
    @Operation(summary = "Get service providers")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.LISTSERVICEPROVIDERSRESPONSE)))})
    public List<ServiceProviderApi> getServiceProviders(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("List service providers for admin user {}", adminUser);
        List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAll();
        return typeTransformer.serviceProviderListToServiceProviderApiList(serviceProviderList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/subscriptions/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Service providers")
    @Operation(summary = "Get capabilities matching subscription")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETSUBSCRIPTIONCAPABILITYRESPONSE)))})
    public List<MatchingCapabilityApi> getMatchingSubscriptionCapabilities(@PathVariable("adminUser") String adminUser, @RequestParam(required = false, name = "selector") String selector){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("List network capabilities matching subscription for service providers for admin user {}", adminUser);
        List<ServiceProvider> serviceProviderList = serviceProviderRepository.findAll();
        Set<Capability> localCapabilities = getAllLocalCapabilities(serviceProviderList);
        Set<NeighbourCapability> neighbourCapabilities = getAllNeighbourCapabilities();
        if (selector != null && !selector.isEmpty()) {
            localCapabilities = getAllMatchingLocalCapabilities(selector, localCapabilities);
            neighbourCapabilities = getAllMatchingNeighbourCapabilities(selector, neighbourCapabilities);
        }

        return typeTransformer.capabilitiesToGetMatchingCapabilitiesApiList(localCapabilities, neighbourCapabilities);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Service providers")
    @Operation(summary = "Get capabilities matching delivery")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETDELIVERYCAPABILITYRESPONSE)))})
    public List<MatchingCapabilityApi> getMatchingDeliveryCapabilities(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @RequestParam(required = false, name = "selector") String selector){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("List local capabilities matching delivery for service provider {} for admin user {}", actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        Set<Capability> allCapabilities = serviceProvider.getCapabilities().getCapabilities();
        if(selector != null){
            if(!selector.isEmpty()){
                allCapabilities = getAllMatchingLocalCapabilities(selector, allCapabilities);
            }
        }

        return typeTransformer.capabilitiesToGetMatchingCapabilitiesApiList(allCapabilities, Collections.emptySet());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/privatechannels", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Get private channels for the specified service provider")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETPRIVATECHANNELSRESPONSE)))})
    public List<PrivateChannelApi> getPrivateChannels(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("List private channels for service provider {} for admin user {}", actorCommonName, adminUser);
        List<PrivateChannel> privateChannels = privateChannelRepository.findAllByServiceProviderName(actorCommonName);
        return typeTransformer.privateChannelListToPrivateChannelApiList(privateChannels);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/privatechannels/peer", produces = MediaType.APPLICATION_JSON_VALUE)
    @Tag(name = "Private channels")
    @Operation(summary = "Get private channels with actorCommonName as peer")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETPEERPRIVATECHANNELS)))})
    public List<PeerPrivateChannelApi> getPeerPrivateChannels(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName){
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("List private channels with actorCommonName as peer for service provider {} for admin user {} where peer name is {}", actorCommonName, adminUser, actorCommonName);

        List<PrivateChannel> privateChannels = privateChannelRepository.findAllByPeerName(actorCommonName);

        return privateChannels.stream().map(typeTransformer::PrivateChannelListToPeerPrivateChannelApiList).sorted().collect(Collectors.toList());
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/exchanges")
    @Tag(name = "Exchanges")
    @Operation(summary = "Get queues from qpid")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETEXCHANGESRESPONSE)))})
    public List<ExchangeApi> getExchanges(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("List exchanges for admin user {}", adminUser);
        List<Exchange> exchangesList = qpidService.getAllExchanges();
        return typeTransformer.exchangeListToExchangeApiList(exchangesList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/queues")
    @Tag(name = "Queues")
    @Operation(summary = "Get queues from qpid")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETQUEUESRESPONSE)))})
    public List<QueueApi> getQueues(@PathVariable("adminUser") String adminUser) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("List queues for admin user {}", adminUser);
        List<Queue> queuesList = qpidService.getAllQueues();
        return typeTransformer.queueListToQueueApiList(queuesList);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/exchanges/{exchangeName}")
    @Tag(name = "Exchanges")
    @Operation(summary = "Does exchange exist")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETEXCHANGEEXISTSSRESPONSE)))})
    public Boolean exchangeExists(@PathVariable("adminUser") String adminUser, @PathVariable("exchangeName") String exchangeName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("Log - exchange exists - requesting user {}", adminUser);
        return qpidService.exchangeExists(exchangeName);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/queues/{queueName}")
    @Tag(name = "Queues")
    @Operation(summary = "Does queue exist")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETQUEUEEXISTSRESPONSE)))})
    public Boolean queueExists(@PathVariable("adminUser") String adminUser, @PathVariable("queueName") String queueName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("Log - queue exists - requesting user {}", adminUser);
        return qpidService.queueExists(queueName);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/bindings/{exchangeName}/{queueName}")
    @Tag(name = "Binding")
    @Operation(summary = "Does binding exist")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETBINDINGEXISTSRESPONSE)))})
    public Boolean bindingExists(@PathVariable("adminUser") String adminUser, @PathVariable("exchangeName") String exchangeName, @PathVariable("queueName") String queueName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);

        logger.info("Log - binding exists - requesting user {}", adminUser);
        return qpidService.bindingExists(exchangeName, queueName);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get delivery ids for the specified service provider")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETDELIVERYIDSRESPONSE)))})
    public List<String> getDeliveryIdsForEachServiceProvider(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - List delivery ids for service provider {} for admin user {}", actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        return typeTransformer.getDeliveryIds(serviceProvider.getDeliveries());
    }


    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/{deliveryId}")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get delivery based on provided delivery id for the specified service provider")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETDELIVERIESRESPONSE)))})
    public LocalDeliveryApi getDeliveryBasedOnDeliveryId(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - Delivery with deliveryId {} for service provider {} for admin user {}", deliveryId, actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        LocalDelivery localDelivery = serviceProvider.getDelivery(deliveryId);

        return typeTransformer.localDeliveryToDeliveriesApi(localDelivery);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/{deliveryId}/endpoints")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get delivery's endpoints based on provided delivery id for the specified service provider")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETENDPOINTSRESPONSE)))})
    public List<LocalDeliveryEndpointAdminApi> getLocalDeliveryEndpoints(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - List delivery's endpoints for service provider {} for admin user {}", actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        LocalDelivery delivery = serviceProvider.findDeliveryByUuid(deliveryId);
        deliveryExists(deliveryId, delivery);
        return qpidService.getLocalDeliveryEndpointApiList(delivery);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/{deliveryId}/matches")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get capabilities match a delivery for the specified service provider and delivery id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETCAPABILITIESMATCHRESPONSE)))})
    public CapabilitiesLinkedDeliveryApi getDeliveriesExchangeBindingToMatchingCapabilities(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - List capabilities match a delivery with id {} for service provider {} for admin user {}", deliveryId, actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        LocalDelivery delivery = serviceProvider.findDeliveryByUuid(deliveryId);
        deliveryExists(deliveryId, delivery);
        List<OutgoingMatch> allByLocalDeliveryUuid = outgoingMatchRepository.findAllByLocalDelivery_Uuid(deliveryId);
        return qpidService.getCapabilitiesLinkedDelivery(delivery, allByLocalDeliveryUuid);
    }


    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/{deliveryId}/matches/{capabilityId}")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get capability matches a delivery for the specified service provider, delivery id and capability id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETCAPABILITYMATCHRESPONSE)))})
    public CapabilityApi getCapabilitiesMatchedDeliveryBasedOnCapabilityId(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId,
                                                                           @PathVariable("capabilityId") String capabilityId) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - List the capability with id {} matches a delivery with id {} for service provider {} for admin user {}", capabilityId, deliveryId, actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        LocalDelivery delivery = serviceProvider.findDeliveryByUuid(deliveryId);
        deliveryExists(deliveryId, delivery);
        Capability capability = serviceProvider.getCapability(capabilityId);
        if (capability == null) {
            throw new NotFoundException("Capability with id " + capabilityId + " not found");
        }
        OutgoingMatch matchedByCapabilityAndDelivery = outgoingMatchRepository.findByCapability_UuidAndLocalDelivery_Uuid(capabilityId, deliveryId);

        if (matchedByCapabilityAndDelivery == null) {
            throw new NotFoundException("No match found for capability with" + capabilityId + " and delivery with id " + deliveryId);
        }

        return typeTransformer.capabilitiesMatchedDeliveryBasedOnCapabilityId(matchedByCapabilityAndDelivery);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/admin/{adminUser}/serviceproviders/{actorCommonName}/deliveries/{deliveryId}/matches/{capabilityId}/{shardId}")
    @Tag(name = "Deliveries")
    @Operation(summary = "Get capability shard matches a delivery for the specified service provider, shard id, delivery id and capability id")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = ExampleAdminApiObjects.GETCAPABILITYSHARDMATCHRESPONSE)))})
    public CapabilityShardAdminApi getCapabilitiesMatchedDeliveryBasedOnShardId(@PathVariable("adminUser") String adminUser, @PathVariable("actorCommonName") String actorCommonName, @PathVariable("deliveryId") String deliveryId,
                                                                                @PathVariable("capabilityId") String capabilityId, @PathVariable("shardId") String shardId) {
        this.certService.checkIfCommonNameMatchesNameInApiObject(adminProperties.getName());
        validatePathVariable(adminUser);
        validatePathVariable(actorCommonName);

        logger.info("Log - List capability shard with id {} matches a delivery with id {} for service provider {} for admin user {}", shardId, deliveryId, actorCommonName, adminUser);
        ServiceProvider serviceProvider = serviceProviderExists(actorCommonName);

        LocalDelivery delivery = serviceProvider.findDeliveryByUuid(deliveryId);
        deliveryExists(deliveryId, delivery);
        OutgoingMatch matchedByCapabilityAndDelivery = outgoingMatchRepository.findByCapability_UuidAndLocalDelivery_Uuid(capabilityId, deliveryId);

        if (matchedByCapabilityAndDelivery == null) {
            throw new NotFoundException("No match found for capability with" + capabilityId + " and delivery with id " + deliveryId);
        }

        Optional<CapabilityShard> shard = matchedByCapabilityAndDelivery.getCapability().getShard(Integer.valueOf(shardId));
        boolean exchangeExists = false;
        if (shard.isPresent()) {
            CapabilityShard capabilityShard = shard.get();
            String exchangeName = capabilityShard.getExchangeName();
            exchangeExists = qpidService.exchangeExists(exchangeName);
            return typeTransformer.capabilitiesMatchedDeliveryBasedOnShardId(capabilityShard,exchangeExists);
        } else {
            logger.info("Shard {} for capability {} is not found",shardId,capabilityId);
            throw new NotFoundException(String.format("Shard %s for capability %s is not found",shardId,capabilityId));
        }

    }

    private ServiceProvider serviceProviderExists(String actorCommonName) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByName(actorCommonName);
        if (serviceProvider == null) {
            throw new NotFoundException("Service provider " + actorCommonName + " not found");
        }
        return serviceProvider;
    }

    private static void deliveryExists(String deliveryId, LocalDelivery delivery) {
        if (delivery == null) {
            throw new NotFoundException("Delivery with id " + deliveryId + " not found");
        }
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

    private void validatePathVariable(String pathVariable){
        Matcher matcher = pattern.matcher(pathVariable);
        if(!matcher.matches()){
            throw new PathVariableException(String.format("Path variable %s contains illegal characters", pathVariable));
        }
    }

}
