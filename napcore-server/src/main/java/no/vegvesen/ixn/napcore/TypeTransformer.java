package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.Peer;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.model.PrivateChannelEndpoint;
import no.vegvesen.ixn.napcore.model.PrivateChannelStatus;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.SubscriptionStatus;
import no.vegvesen.ixn.shared.capability.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class TypeTransformer {

    public no.vegvesen.ixn.federation.model.capability.Capability transformCapabilitiesRequestToCapability(CapabilitiesRequest request){
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        return new no.vegvesen.ixn.federation.model.capability.Capability(
                transformer.applicationApiToApplication(request.getApplication()),
                transformer.metadataApiToMetadata(request.getMetadata())
        );
    }

    public OnboardingCapability transformCapabilityToOnboardingCapability(no.vegvesen.ixn.federation.model.capability.Capability capability, boolean hasDelivery){
        Metadata metadata = capability.getMetadata();
        return new OnboardingCapability(
                capability.getUuid(),
                applicationApiToApplicationApi(capability.getApplication()),
                metadataToMetadataApi(metadata),
                hasDelivery,
                transformLocalDateTimeToTimestamp(capability.getCreatedTimestamp()));
    }

    private static MetadataApi metadataToMetadataApi(Metadata metadata) {
        return new MetadataApi(metadata.getShardCount(), metadata.getInfoUrl(), redirectStatusToRedirectStatusApi(metadata.getRedirectPolicy()), metadata.getMaxBandwidth(), metadata.getMaxMessageRate(), metadata.getRepetitionInterval());
    }

    private static RedirectStatusApi redirectStatusToRedirectStatusApi(RedirectStatus statusApi) {
        if (statusApi != null) {
            return switch (statusApi) {
                case MANDATORY -> RedirectStatusApi.MANDATORY;
                case NOT_AVAILABLE -> RedirectStatusApi.NOT_AVAILABLE;
                default -> RedirectStatusApi.OPTIONAL;
            };
        }
        return RedirectStatusApi.OPTIONAL;
    }

    public LocalSubscription transformNapSubscriptionToLocalSubscription(SubscriptionRequest subscription, String nodeName) {
        return new LocalSubscription(subscription.getSelector(), nodeName, subscription.getDescription());
    }

    public LocalDelivery transformNapDeliveryToLocalDelivery(DeliveryRequest delivery, String hostname, int port){
        Boolean dlqueue = delivery.isDlqueue();
        return new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(
                        new LocalDeliveryEndpoint(
                                hostname,
                                port,
                                "del-" + UUID.randomUUID(),
                                Objects.equals(dlqueue,Boolean.TRUE) ? "dlq-" + UUID.randomUUID() : null
                        )
                ),
                delivery.getSelector(),
                LocalDeliveryStatus.REQUESTED,
                delivery.getDescription(),
                dlqueue
        );
    }

    public Delivery transformLocalDeliveryToNapDelivery(LocalDelivery localDelivery){
        return new Delivery(
                localDelivery.getUuid(),
                localDelivery.getSelector(),
                transformLocalDeliveryStatusToNapDeliveryStatus(localDelivery.getStatus()),
                transformLocalDeliveryEndpointsToNapEndpoints(localDelivery.getEndpoints()),
                transformLocalDateTimeToTimestamp(localDelivery.getLastUpdatedTimestamp()),
                localDelivery.getDescription(),
                localDelivery.isDlqueue()
        );
    }

    public List<Delivery> transformLocalDeliveriesToNapDeliveries(Set<LocalDelivery> localDeliveries){
        List<Delivery> deliveries = new ArrayList<>();
        for(LocalDelivery localDelivery : localDeliveries){
            deliveries.add(transformLocalDeliveryToNapDelivery(localDelivery));
        }
        return deliveries;
    }

    public DeliveryStatus transformLocalDeliveryStatusToNapDeliveryStatus(LocalDeliveryStatus localDeliveryStatus){
        return switch(localDeliveryStatus){
            case REQUESTED -> DeliveryStatus.REQUESTED;
            case CREATED -> DeliveryStatus.CREATED;
            case ILLEGAL -> DeliveryStatus.ILLEGAL;
            case TEAR_DOWN -> DeliveryStatus.ILLEGAL;
            case NOT_VALID -> DeliveryStatus.NOT_VALID;
            case NO_OVERLAP -> DeliveryStatus.NO_OVERLAP;
            case ERROR -> DeliveryStatus.ILLEGAL;
        };
    }

    // TODO change empty string to LocalDeliveryEndpoint's selector when object is changed
    public List<DeliveryEndpoint> transformLocalDeliveryEndpointsToNapEndpoints(Set<LocalDeliveryEndpoint> localDeliveryEndpoints){
        List<DeliveryEndpoint> endpoints = new ArrayList<>();
        for(LocalDeliveryEndpoint endpoint : localDeliveryEndpoints){
            endpoints.add(new DeliveryEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getTarget(), null, endpoint.getMaxBandwidth(), endpoint.getMaxMessageRate(), endpoint.getDlqName()));
        }
        return endpoints;
    }

    public Subscription transformLocalSubscriptionToNapSubscription(LocalSubscription localSubscription) {
        Subscription subscription = new Subscription(
                localSubscription.getUuid(),
                transformLocalSubscriptionStatusToNapSubscriptionStatus(localSubscription.getStatus()),
                localSubscription.getSelector(),
                transformLocalEndpointsToNapSubscriptionEndpoints(localSubscription.getLocalEndpoints()),
                transformLocalDateTimeToTimestamp(localSubscription.getLastUpdated()),
                localSubscription.getDescription()
        );
        return subscription;
    }

    public List<Subscription> transformLocalSubscriptionsToNapSubscriptions(List<LocalSubscription> localSubscriptions) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (LocalSubscription localSubscription : localSubscriptions) {
            subscriptions.add(transformLocalSubscriptionToNapSubscription(localSubscription));
        }
        return subscriptions;
    }

    public Set<SubscriptionEndpoint> transformLocalEndpointsToNapSubscriptionEndpoints(Set<LocalEndpoint> localEndpoints) {
        Set<SubscriptionEndpoint> endpoints = new HashSet<>();
        for (LocalEndpoint endpoint : localEndpoints) {
            endpoints.add( new SubscriptionEndpoint(
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getSource(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return endpoints;
    }

    public SubscriptionStatus transformLocalSubscriptionStatusToNapSubscriptionStatus(LocalSubscriptionStatus status) {
        return switch (status) {
            case REQUESTED -> SubscriptionStatus.REQUESTED;
            case CREATED -> SubscriptionStatus.CREATED;
            case TEAR_DOWN -> SubscriptionStatus.NOT_VALID;
            default -> SubscriptionStatus.ILLEGAL;
        };
    }

    public List<no.vegvesen.ixn.napcore.model.Capability> transformCapabilitiesToGetMatchingCapabilitiesResponse(Set<no.vegvesen.ixn.federation.model.capability.Capability> capabilities, Set<NeighbourCapability> neighbourCapabilities) {
        List<no.vegvesen.ixn.napcore.model.Capability> matchingCapabilities = new ArrayList<>();
        for (no.vegvesen.ixn.federation.model.capability.Capability capability : capabilities) {
            Metadata metadata = capability.getMetadata();
            matchingCapabilities.add(new no.vegvesen.ixn.napcore.model.Capability(
                    applicationApiToApplicationApi(capability.getApplication()),
                    metadataToMetadataApi(metadata),
                    transformLocalDateTimeToTimestamp(capability.getCreatedTimestamp())
            ));
        }
        for (NeighbourCapability neighbourCapability : neighbourCapabilities) {
            Metadata metadata = neighbourCapability.getMetadata();
            matchingCapabilities.add(new no.vegvesen.ixn.napcore.model.Capability(
                    applicationApiToApplicationApi(neighbourCapability.getApplication()),
                    metadataToMetadataApi(metadata),
                    transformLocalDateTimeToTimestamp(neighbourCapability.getCreatedTimestamp())
            ));
        }
        return matchingCapabilities;
    }

    public PrivateChannelResponse transformPrivateChannelToPrivateChannelResponse(PrivateChannel privateChannel) {
        return new PrivateChannelResponse(
                privateChannel.getUuid(),
                privateChannel.getPeers().stream().filter(p -> !p.getStatus().equals(PeerStatus.TEAR_DOWN)).map(Peer::getName).collect(Collectors.toSet()),
                transformPrivateChannelStatus(privateChannel.getStatus()),
                privateChannel.getDescription(),
                transformPrivateChannelEndpoint(privateChannel.getEndpoint()),
                transformLocalDateTimeToTimestamp(privateChannel.getLastUpdated())
        );
    }

    public PeerPrivateChannel transformPrivateChannelToPeerPrivateChannel(PrivateChannel privateChannel) {
        return new PeerPrivateChannel(
                privateChannel.getUuid(),
                privateChannel.getServiceProviderName(),
                transformPrivateChannelStatus(privateChannel.getStatus()),
                privateChannel.getDescription(),
                transformPrivateChannelEndpoint(privateChannel.getEndpoint()),
                transformLocalDateTimeToTimestamp(privateChannel.getLastUpdated())
        );
    }

    public ServiceProviderBiqueueAccessResponse transformBiconsumerAccess(ServiceProvider serviceProvider) {
        Boolean biconsumer = serviceProvider.isBiconsumer();
        return new ServiceProviderBiqueueAccessResponse(
                serviceProvider.getName(),
                biconsumer == null ? Boolean.FALSE : biconsumer
        );
    }

    public ServiceProviderBiqueueAccessResponse transformAddBiconsumerAccess(ServiceProvider serviceProvider, ServiceProviderBiqueueAccessRequest serviceProviderBiqueueAccessRequest) {
        return new ServiceProviderBiqueueAccessResponse(
                serviceProvider.getName(),
                serviceProviderBiqueueAccessRequest.isAccess()
        );
    }

    public PrivateChannelStatus transformPrivateChannelStatus(no.vegvesen.ixn.federation.model.PrivateChannelStatus status) {
        return switch (status) {
            case REQUESTED -> PrivateChannelStatus.REQUESTED;
            case CREATED -> PrivateChannelStatus.CREATED;
            case TEAR_DOWN -> PrivateChannelStatus.NOT_VALID;
            default -> PrivateChannelStatus.ILLEGAL;
        };
    }

    public PrivateChannelEndpoint transformPrivateChannelEndpoint(no.vegvesen.ixn.federation.model.PrivateChannelEndpoint endpoint) {
        return new PrivateChannelEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getQueueName());
    }

    public Long transformLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        Long epochSecond = null;
        if (localDateTime != null) {
            epochSecond = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        return epochSecond;
    }

    List<OnboardingCapability> transformCapabilityListToOnboardingCapabilityList(Set<CapabilityAndDelivery> capabilitiesAndDeliveries, NapRestController napRestController) {
        List<OnboardingCapability> onboardingCapabilities = new ArrayList<>();
        for(CapabilityAndDelivery capabilityAndDelivery : capabilitiesAndDeliveries){
            onboardingCapabilities.add(transformCapabilityToOnboardingCapability(capabilityAndDelivery.capability(),capabilityAndDelivery.hasDelivery()));
        }
        return onboardingCapabilities;
    }

    private static ApplicationApi applicationApiToApplicationApi(Application application) {
        return switch (application) {
            case DatexApplication d -> new DatexApplicationApi(
                    d.getPublisherId(),
                    d.getPublicationId(),
                    d.getOriginatingCountry(),
                    d.getProtocolVersion(),
                    d.getQuadTree(),
                    d.getPublicationType(),
                    d.getPublisherName()
            );
            case DenmApplication d ->  new DenmApplicationApi(
                    d.getPublisherId(),
                    d.getPublicationId(),
                    d.getOriginatingCountry(),
                    d.getProtocolVersion(),
                    d.getQuadTree(),
                    d.getCauseCode()
            );
            case IvimApplication i ->  new IvimApplicationApi(
                    i.getPublisherId(),
                    i.getPublicationId(),
                    i.getOriginatingCountry(),
                    i.getProtocolVersion(),
                    i.getQuadTree()
            );
            case SpatemApplication sp ->  new SpatemApplicationApi(
                    sp.getPublisherId(),
                    sp.getPublicationId(),
                    sp.getOriginatingCountry(),
                    sp.getProtocolVersion(),
                    sp.getQuadTree()
            );
            case MapemApplication mapem ->  new MapemApplicationApi(
                    mapem.getPublisherId(),
                    mapem.getPublicationId(),
                    mapem.getOriginatingCountry(),
                    mapem.getProtocolVersion(),
                    mapem.getQuadTree()
            );
            case SremApplication srem ->  new SremApplicationApi(
                    srem.getPublisherId(),
                    srem.getPublicationId(),
                    srem.getOriginatingCountry(),
                    srem.getProtocolVersion(),
                    srem.getQuadTree()
            );
            case SsemApplication ssem ->  new SsemApplicationApi(
                    ssem.getPublisherId(),
                    ssem.getPublicationId(),
                    ssem.getOriginatingCountry(),
                    ssem.getProtocolVersion(),
                    ssem.getQuadTree()
            );
            case CamApplication cam ->  new CamApplicationApi(
                    cam.getPublisherId(),
                    cam.getPublicationId(),
                    cam.getOriginatingCountry(),
                    cam.getProtocolVersion(),
                    cam.getQuadTree()
            );
            default -> throw new IllegalArgumentException("Unknown application api");
        };

    }

    public record CapabilityAndDelivery(Capability capability, boolean hasDelivery) {}
}
