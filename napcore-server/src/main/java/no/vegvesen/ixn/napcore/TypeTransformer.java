package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.napcore.model.Subscription;
import no.vegvesen.ixn.napcore.model.SubscriptionRequest;
import no.vegvesen.ixn.napcore.model.SubscriptionStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    public no.vegvesen.ixn.federation.model.capability.Capability transformCapabilitiesRequestToCapability(CapabilitiesRequest request){
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        return new no.vegvesen.ixn.federation.model.capability.Capability(
                transformer.applicationApiToApplication(request.getApplication()),
                transformer.metadataApiToMetadata(request.getMetadata())
        );
    }

    public OnboardingCapability transformCapabilityToOnboardingCapability(no.vegvesen.ixn.federation.model.capability.Capability capability){
        return new OnboardingCapability(
                capability.getId().toString(),
                capability.getApplication().toApi(),
                capability.getMetadata().toApi(),
                transformLocalDateTimeToTimestamp(capability.getCreatedTimestamp()));
    }

    public List<OnboardingCapability> transformCapabilityListToOnboardingCapabilityList(Set<no.vegvesen.ixn.federation.model.capability.Capability> capabilities){
        List<OnboardingCapability> onboardingCapabilities = new ArrayList<>();
        for(no.vegvesen.ixn.federation.model.capability.Capability capability : capabilities){
            onboardingCapabilities.add(transformCapabilityToOnboardingCapability(capability));
        }
        return onboardingCapabilities;
    }

    public LocalSubscription transformNapSubscriptionToLocalSubscription(SubscriptionRequest subscription, String nodeName) {
        return new LocalSubscription(subscription.getSelector(), nodeName);
    }

    public LocalDelivery transformNapDeliveryToLocalDelivery(DeliveryRequest delivery){
        return new LocalDelivery(delivery.getSelector());
    }

    public Delivery transformLocalDeliveryToNapDelivery(LocalDelivery localDelivery){
        return new Delivery(
                localDelivery.getUuid(),
                localDelivery.getSelector(),
                transformLocalDeliveryStatusToNapDeliveryStatus(localDelivery.getStatus()),
                transformLocalDeliveryEndpointsToNapEndpoints(localDelivery.getEndpoints()),
                transformLocalDateTimeToTimestamp(localDelivery.getLastUpdatedTimestamp())
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
            endpoints.add(new DeliveryEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getTarget(), null, endpoint.getMaxBandwidth(), endpoint.getMaxMessageRate()));
        }
        return endpoints;
    }

    public Subscription transformLocalSubscriptionToNapSubscription(LocalSubscription localSubscription) {
        Subscription subscription = new Subscription(
                localSubscription.getUuid(),
                transformLocalSubscriptionStatusToNapSubscriptionStatus(localSubscription.getStatus()),
                localSubscription.getSelector(),
                transformLocalEndpointsToNapSubscriptionEndpoints(localSubscription.getLocalEndpoints()),
                transformLocalDateTimeToTimestamp(localSubscription.getLastUpdated())

        );
        return subscription;
    }

    public List<Subscription> transformLocalSubscriptionsToNapSubscriptions(Set<LocalSubscription> localSubscriptions) {
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
            matchingCapabilities.add(new no.vegvesen.ixn.napcore.model.Capability(
                    capability.getApplication().toApi(),
                    capability.getMetadata().toApi(),
                    transformLocalDateTimeToTimestamp(capability.getCreatedTimestamp())
            ));
        }
        for (NeighbourCapability neighbourCapability : neighbourCapabilities) {
            matchingCapabilities.add(new no.vegvesen.ixn.napcore.model.Capability(
                    neighbourCapability.getApplication().toApi(),
                    neighbourCapability.getMetadata().toApi(),
                    transformLocalDateTimeToTimestamp(neighbourCapability.getCreatedTimestamp())
            ));
        }
        return matchingCapabilities;
    }

    public Long transformLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        Long epochSecond = null;
        if (localDateTime != null) {
            epochSecond = localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        return epochSecond;
    }
}
