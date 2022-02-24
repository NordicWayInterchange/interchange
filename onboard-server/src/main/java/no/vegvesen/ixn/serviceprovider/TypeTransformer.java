package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {

    public static AddCapabilitiesResponse addCapabilitiesResponse(String serviceProviderName, Set<Capability> capabilities) {
        return new AddCapabilitiesResponse(
               serviceProviderName,
               capabilitySetToLocalActorCapability(serviceProviderName,capabilities)
        );
    }

    private static Set<LocalActorCapability> capabilitySetToLocalActorCapability(String serviceProviderName, Set<Capability> capabilities) {
        Set<LocalActorCapability> result = new HashSet<>();
        for (Capability capability : capabilities) {
            result.add(capabilityToLocalCapability(serviceProviderName,capability));
        }
        return result;
    }

    private static LocalActorCapability capabilityToLocalCapability(String serviceProviderName, Capability capability) {
        String id = capability.getId().toString();
        return new LocalActorCapability(
                id,
                createCapabilitiesPath(serviceProviderName, id),
                capability.toApi());
    }

    public FetchCapabilitiesResponse fetchCapabilitiesResponse(Set<Capability> capabilities) {
        Set<FetchCapability> fetchCapabilities = new HashSet<>();
        for (Capability capability : capabilities) {
            fetchCapabilities.add(new FetchCapability(capability.toApi()));
        }
        return new FetchCapabilitiesResponse(fetchCapabilities);
    }


    public ListSubscriptionsResponse transformLocalSubscriptionsToListSubscriptionResponse(String name, Set<LocalSubscription> subscriptions) {
        return new ListSubscriptionsResponse(name,transformLocalSubscriptionsToLocalActorSubscription(name,subscriptions));

    }

    private Set<LocalActorSubscription> transformLocalSubscriptionsToLocalActorSubscription(String name, Set<LocalSubscription> subscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : subscriptions) {
            String sub_id = subscription.getId() == null ? null : subscription.getId().toString();
            result.add(new LocalActorSubscription(
                    sub_id,
                    createSubscriptionPath(name,sub_id),
                    subscription.getSelector(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(subscription.getStatus())));
        }
        return result;
    }


    public LocalSubscription transformAddSubscriptionToLocalSubscription(AddSubscription addSubscription) {
        return new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                addSubscription.getSelector());
    }

    public LocalDelivery transformDeliveryToLocalDelivery(SelectorApi delivery) {
        return new LocalDelivery(delivery.getSelector(), LocalDeliveryStatus.REQUESTED);
    }

    public AddDeliveriesResponse transformToDeliveriesResponse(String serviceProviderName, Set<LocalDelivery> localDeliveries) {
        return new AddDeliveriesResponse(serviceProviderName,
                transformLocalDeliveryToDelivery(
                        serviceProviderName,
                        localDeliveries));
    }

    public Set<Delivery> transformLocalDeliveryToDelivery(String serviceProviderName, Set<LocalDelivery> localDeliveries) {
        Set<Delivery> result = new HashSet<>();
        for (LocalDelivery delivery : localDeliveries){
            result.add(new Delivery(
                    delivery.getId().toString(),
                    createDeliveryPath(serviceProviderName, delivery.getId().toString()),
                    delivery.getSelector(),
                    transformLocalDateTimeToEpochMili(delivery.getLastUpdatedTimestamp()),
                    transformLocalDeliveryStatusToDeliveryStatus(delivery.getStatus())
                    )
            );
        }
        return result;
    }

    public ListDeliveriesResponse transformToListDeliveriesResponse(String serviceProviderName, Set<LocalDelivery> localDeliveries) {
        return new ListDeliveriesResponse(serviceProviderName,
                transformLocalDeliveryToDelivery(
                        serviceProviderName,
                        localDeliveries
                )
        );
    }

    public AddSubscriptionsResponse transformLocalSubscriptionsToSubscriptionPostResponseApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        return new AddSubscriptionsResponse(serviceProviderName,
                transformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(
                        serviceProviderName,
                        localSubscriptions)
        );
    }

    public Set<LocalActorSubscription> transformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : localSubscriptions) {
            String subscriptionId = subscription.getId().toString();
            result.add(new LocalActorSubscription(
                    subscriptionId,
                    createSubscriptionPath(serviceProviderName, subscriptionId),
                    subscription.getSelector(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(subscription.getStatus())
                    )
            );
        }
        return result;
    }

    private String createSubscriptionPath(String serviceProviderName, String subscriptionId) {
        return String.format("/%s/subscriptions/%s", serviceProviderName, subscriptionId);
    }

    private static String createCapabilitiesPath(String serviceProviderName, String capabilityId) {
        return String.format("/%s/capabilities/%s", serviceProviderName, capabilityId);
    }

    private static String createDeliveryPath(String serviceProviderName, String deliveryId) {
        return String.format("/%s/deliveries/%s", serviceProviderName, deliveryId);
    }

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public GetSubscriptionResponse transformLocalSubscriptionToGetSubscriptionResponse(String serviceProviderName, LocalSubscription localSubscription) {
        return new GetSubscriptionResponse(
                localSubscription.getId().toString(),
                createSubscriptionPath(serviceProviderName,localSubscription.getId().toString()),
                localSubscription.getSelector(),
                transformLocalDateTimeToEpochMili(localSubscription.getLastUpdated()),
                transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(localSubscription.getStatus()),
                transformLocalEndpointsToLocalEndpointApis(localSubscription.getLocalEndpoints())
        );
    }

    public GetDeliveryResponse transformLocalDeliveryToGetDeliveryResponse(String serviceProviderName, LocalDelivery localDelivery) {
        String id = localDelivery.getId().toString();
        return new GetDeliveryResponse(
                id,
                transformLocalDeliveryEndpointToDeliveryEndpoint(localDelivery.getEndpoints()),
                createDeliveryPath(serviceProviderName, id),
                localDelivery.getSelector(),
                transformLocalDateTimeToEpochMili(localDelivery.getLastUpdatedTimestamp()),
                transformLocalDeliveryStatusToDeliveryStatus(localDelivery.getStatus())

        );
    }
    private Set<LocalEndpointApi> transformLocalEndpointsToLocalEndpointApis(Set<LocalEndpoint> localEndpoints) {
        Set<LocalEndpointApi> result = new HashSet<>();
        for (LocalEndpoint localEndpoint : localEndpoints) {
            result.add(new LocalEndpointApi(localEndpoint.getHost(),
                    localEndpoint.getPort(),
                    localEndpoint.getSource(),
                    localEndpoint.getMaxBandwidth(),
                    localEndpoint.getMaxMessageRate()));
        }
        return result;
    }

    private Set<DeliveryEndpoint> transformLocalDeliveryEndpointToDeliveryEndpoint(Set<LocalDeliveryEndpoint> endpoints) {
        Set<DeliveryEndpoint> result = new HashSet<>();
        for (LocalDeliveryEndpoint endpoint : endpoints) {
            result.add(new DeliveryEndpoint(
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getTarget(),
                    endpoint.getSelector(),
                    null,
                    null
            ));
        }
        return result;
    }

    private LocalActorSubscriptionStatusApi transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(LocalSubscriptionStatus status) {
        switch (status) {
            //case ASSIGN_QUEUE:
            case REQUESTED:
                return LocalActorSubscriptionStatusApi.REQUESTED;
            case CREATED:
                return LocalActorSubscriptionStatusApi.CREATED;
            case TEAR_DOWN:
                return LocalActorSubscriptionStatusApi.NOT_VALID;
            default:
                return LocalActorSubscriptionStatusApi.ILLEGAL;
        }
    }

    private DeliveryStatus transformLocalDeliveryStatusToDeliveryStatus(LocalDeliveryStatus status) {
        switch (status) {
            case REQUESTED:
                return DeliveryStatus.REQUESTED;
            case CREATED:
                return DeliveryStatus.CREATED;
            case NOT_VALID:
                return DeliveryStatus.NOT_VALID;
            default:
                return DeliveryStatus.ILLEGAL;
        }
    }

    public Set<Capability> capabilitiesRequestToCapabilities(CapabilityToCapabilityApiTransformer capabilityApiTransformer, AddCapabilitiesRequest capabilitiesRequest) {
        Set<Capability> capabilities = new HashSet<>();
        for (CapabilityApi capabilityApi : capabilitiesRequest.getCapabilities()) {
            Capability capability = capabilityApiTransformer.capabilityApiToCapability(capabilityApi);
            capabilities.add(capability);

        }
        return capabilities;
    }

    public ListCapabilitiesResponse listCapabilitiesResponse(String serviceProviderName, Set<Capability> capabilities) {
        return new ListCapabilitiesResponse(
                serviceProviderName,
                capabilitySetToLocalActorCapability(serviceProviderName,capabilities)
        );
    }

    public GetCapabilityResponse getCapabilityResponse(String serviceProviderName, Capability capability) {
        String capabilityId = capability.getId().toString();
        return new GetCapabilityResponse(
                capabilityId,
                createCapabilitiesPath(serviceProviderName,capabilityId),
                capability.toApi()
        );
    }

    public static List<String> makeHostAndPortOfUrl(String url){
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1){
            port = 5671;
        }
        return new ArrayList<>(Arrays.asList(host, String.valueOf(port)));
    }
}
