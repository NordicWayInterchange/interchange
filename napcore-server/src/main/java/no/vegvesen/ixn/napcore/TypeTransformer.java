package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.federation.api.v1_0.capability.ApplicationApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
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
        CapabilityToCapabilityApiTransformer transformer = new CapabilityToCapabilityApiTransformer();
        return new OnboardingCapability(
                capability.getId().toString(),
                transformer.applicationToApplicationApi(capability.getApplication()),
                transformer.metadataToMetadataApi(capability.getMetadata()));
    }

    public List<OnboardingCapability> transformCapabilitiesToOnboardingCapabilities(Set<no.vegvesen.ixn.federation.model.capability.Capability> capabilities){
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
        LocalDateTime lastUpdated = localDelivery.getLastUpdatedTimestamp();
        Long epochSecond = null;
        if(lastUpdated != null){
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toEpochSecond();
        }

        Delivery delivery = new Delivery(
                localDelivery.getId().toString(),
                localDelivery.getSelector(),
                transformLocalDeliveryStatusToNapDeliveryStatus(localDelivery.getStatus()),
                transformLocalDeliveryEndpointsToNapEndpoints(localDelivery.getEndpoints()),
                epochSecond
        );

        return delivery;
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
            case TEAR_DOWN -> DeliveryStatus.TEAR_DOWN;
            case NOT_VALID -> DeliveryStatus.NOT_VALID;
            case NO_OVERLAP -> DeliveryStatus.NO_OVERLAP;
            case ERROR -> DeliveryStatus.ERROR;
        };
    }

    // TODO change empty string to LocalDeliveryEndpoints selector when object is changed
    public List<DeliveryEndpoint> transformLocalDeliveryEndpointsToNapEndpoints(Set<LocalDeliveryEndpoint> localDeliveryEndpoints){
        List<DeliveryEndpoint> endpoints = new ArrayList<>();
        for(LocalDeliveryEndpoint endpoint : localDeliveryEndpoints){
            endpoints.add(new DeliveryEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getTarget(), null, endpoint.getMaxBandwidth(), endpoint.getMaxMessageRate()));
        }
        return endpoints;
    }

    public Subscription transformLocalSubscriptionToNapSubscription(LocalSubscription localSubscription) {
        LocalDateTime lastUpdated = localSubscription.getLastUpdated();
        Long epochSecond = null;
        if (lastUpdated != null) {
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        Subscription subscription = new Subscription(
                localSubscription.getId(),
                transformLocalSubscriptionStatusToNapSubscriptionStatus(localSubscription.getStatus()),
                localSubscription.getSelector(),
                transformLocalEndpointsToNapSubscriptionEndpoints(localSubscription.getLocalEndpoints()),
                epochSecond

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
        switch(status) {
            case REQUESTED:
                return SubscriptionStatus.REQUESTED;
            case CREATED:
                return SubscriptionStatus.CREATED;
            case TEAR_DOWN:
                return SubscriptionStatus.NOT_VALID;
            default:
                return SubscriptionStatus.ILLEGAL;
        }
    }

    public List<no.vegvesen.ixn.napcore.model.Capability> transformCapabilitiesToGetMatchingCapabilitiesResponse(Set<no.vegvesen.ixn.federation.model.capability.Capability> capabilities) {
        List<no.vegvesen.ixn.napcore.model.Capability> matchingCapabilities = new ArrayList<>();
        for (no.vegvesen.ixn.federation.model.capability.Capability capability : capabilities) {
            matchingCapabilities.add(new no.vegvesen.ixn.napcore.model.Capability(
                    capability.getApplication().toApi(),
                    capability.getMetadata().toApi()
            ));
        }
        return matchingCapabilities;
    }
}
