package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {

    public static AddCapabilitiesResponse addCapabilitiesResponse(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, Set<CapabilitySplit> capabilities) {
        return new AddCapabilitiesResponse(
               serviceProviderName,
               capabilitySetToLocalActorCapability(capabilityApiTransformer, serviceProviderName,capabilities)
        );
    }

    private static Set<LocalActorCapability> capabilitySetToLocalActorCapability(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, Set<CapabilitySplit> capabilities) {
        Set<LocalActorCapability> result = new HashSet<>();
        for (CapabilitySplit capability : capabilities) {
            result.add(capabilityToLocalCapability(capabilityApiTransformer, serviceProviderName,capability));
        }
        return result;
    }

    private static LocalActorCapability capabilityToLocalCapability(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, CapabilitySplit capability) {
        String id = capability.getId().toString();
        return new LocalActorCapability(
                id,
                createCapabilitiesPath(serviceProviderName, id),
                capabilityApiTransformer.capabilitySplitToCapabilitySplitApi(capability));

    }


    public FetchMatchingCapabilitiesResponse transformCapabilitiesToFetchMatchingCapabilitiesResponse(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, String selector, Set<CapabilitySplit> capabilities) {
        Set<CapabilitySplitApi> fetchCapabilities = new HashSet<>();
        for (CapabilitySplit capability : capabilities) {
            fetchCapabilities.add(capabilityApiTransformer.capabilitySplitToCapabilitySplitApi(capability));
        }
        if (selector == null || selector.isEmpty()) {
            return new FetchMatchingCapabilitiesResponse(serviceProviderName, fetchCapabilities);
        } else {
            return new FetchMatchingCapabilitiesResponse(serviceProviderName, selector, fetchCapabilities);
        }
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
                    createSubscriptionPath(name,sub_id, subscription.getStatus()),
                    subscription.getSelector(),
                    subscription.getConsumerCommonName(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(subscription.getStatus())));
        }
        return result;
    }


    public LocalSubscription transformAddSubscriptionToLocalSubscription(AddSubscription addSubscription, String serviceProviderName, String nodeName) {
        String consumerCommonName;
        if (addSubscription.getConsumerCommonName() == null) {
            consumerCommonName = nodeName;
        } else {
            if (!addSubscription.getConsumerCommonName().equals(serviceProviderName)) {
                consumerCommonName = nodeName;
            } else {
                consumerCommonName = serviceProviderName;
            }
        }
        LocalSubscription newSubscription = new LocalSubscription(addSubscription.getSelector(),consumerCommonName);
        return newSubscription;
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
                    createDeliveryPath(serviceProviderName, delivery.getId().toString(), delivery.getStatus()),
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
                    createSubscriptionPath(serviceProviderName, subscriptionId, subscription.getStatus()),
                    subscription.getSelector(),
                    subscription.getConsumerCommonName(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStatusToLocalActorSubscriptionStatusApi(subscription.getStatus())
                    )
            );
        }
        return result;
    }

    private String createSubscriptionPath(String serviceProviderName, String subscriptionId, LocalSubscriptionStatus status) {
        if (!status.equals(LocalSubscriptionStatus.ILLEGAL)) {
            return String.format("/%s/subscriptions/%s", serviceProviderName, subscriptionId);
        } else {
            return "";
        }
    }

    private static String createCapabilitiesPath(String serviceProviderName, String capabilityId) {
        return String.format("/%s/capabilities/%s", serviceProviderName, capabilityId);
    }

    private static String createDeliveryPath(String serviceProviderName, String deliveryId, LocalDeliveryStatus status) {
        if (!status.equals(LocalDeliveryStatus.ILLEGAL)) {
            return String.format("/%s/deliveries/%s", serviceProviderName, deliveryId);
        } else {
            return "";
        }
    }

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public GetSubscriptionResponse transformLocalSubscriptionToGetSubscriptionResponse(String serviceProviderName, LocalSubscription localSubscription) {
        return new GetSubscriptionResponse(
                localSubscription.getId().toString(),
                createSubscriptionPath(serviceProviderName,localSubscription.getId().toString(), localSubscription.getStatus()),
                localSubscription.getSelector(),
                localSubscription.getConsumerCommonName(),
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
                createDeliveryPath(serviceProviderName, id, localDelivery.getStatus()),
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
                    0,
                    0
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
            case NO_OVERLAP:
                return DeliveryStatus.NO_OVERLAP;
            default:
                return DeliveryStatus.ILLEGAL;
        }
    }

    public Set<CapabilitySplit> capabilitiesRequestToCapabilities(CapabilityToCapabilityApiTransformer capabilityApiTransformer, AddCapabilitiesRequest capabilitiesRequest) {
        Set<CapabilitySplit> capabilities = new HashSet<>();
        for (CapabilitySplitApi capabilityApi : capabilitiesRequest.getCapabilities()) {
            CapabilitySplit capability = capabilityApiTransformer.capabilitySplitApiToCapabilitySplit(capabilityApi);
            capabilities.add(capability);

        }
        return capabilities;
    }

    public ListCapabilitiesResponse listCapabilitiesResponse(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, Set<CapabilitySplit> capabilities) {
        return new ListCapabilitiesResponse(
                serviceProviderName,
                capabilitySetToLocalActorCapability(capabilityApiTransformer, serviceProviderName,capabilities)
        );
    }

    public GetCapabilityResponse getCapabilityResponse(CapabilityToCapabilityApiTransformer capabilityApiTransformer, String serviceProviderName, CapabilitySplit capability) {
        String capabilityId = capability.getId().toString();
        return new GetCapabilityResponse(
                capabilityId,
                createCapabilitiesPath(serviceProviderName,capabilityId),
                capabilityApiTransformer.capabilitySplitToCapabilitySplitApi(capability)
        );
    }

    public GetPrivateChannelResponse transformPrivateChannelToGetPrivateChannelResponse(PrivateChannel privateChannel){
        if(privateChannel.getEndpoint() != null) {
            PrivateChannelEndpointApi endpointApi = new PrivateChannelEndpointApi(privateChannel.getEndpoint().getHost(),privateChannel.getEndpoint().getPort(),privateChannel.getEndpoint().getQueueName());
            return new GetPrivateChannelResponse(privateChannel.getId(), privateChannel.getPeerName(), endpointApi, privateChannel.getServiceProviderName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()));
        }
        else{
            return new GetPrivateChannelResponse(privateChannel.getId(),privateChannel.getPeerName(), privateChannel.getServiceProviderName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()));
        }
        }

    public AddPrivateChannelResponse transformPrivateChannelListToAddPrivateChannelsResponse(String serviceProviderName, List<PrivateChannel> privateChannelList){
        AddPrivateChannelResponse response = new AddPrivateChannelResponse(serviceProviderName);
        for(PrivateChannel privateChannel : privateChannelList){
                response.getPrivateChannels().add(new PrivateChannelResponseApi(privateChannel.getPeerName(),PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()), privateChannel.getId()));
        }
        return response;
    }

    public ListPrivateChannelsResponse transformPrivateChannelListToListPrivateChannels(String serviceProviderName,List<PrivateChannel> privateChannelList){
        ArrayList<PrivateChannelResponseApi> returnList = new ArrayList<>();
        for(PrivateChannel privateChannel : privateChannelList){
            returnList.add(new PrivateChannelResponseApi(privateChannel.getPeerName(),PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()), privateChannel.getId()));
        }
        return new ListPrivateChannelsResponse(serviceProviderName, returnList);
    }

    public ListPeerPrivateChannels transformPrivateChannelListToListPrivateChannelsWithServiceProvider(String serviceProviderName, List<PrivateChannel> privateChannelList){
        List<PeerPrivateChannelApi> privateChannelsApis = new ArrayList<>();

        for (PrivateChannel privateChannel : privateChannelList) {
            if(privateChannel.getEndpoint() != null) {
                PrivateChannelEndpointApi endpoint = new PrivateChannelEndpointApi(privateChannel.getEndpoint().getHost(),privateChannel.getEndpoint().getPort(),privateChannel.getEndpoint().getQueueName());
                privateChannelsApis.add(new PeerPrivateChannelApi(privateChannel.getId(), privateChannel.getServiceProviderName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString()), endpoint));
            }
            else{
                privateChannelsApis.add(new PeerPrivateChannelApi(privateChannel.getId(), privateChannel.getServiceProviderName(), PrivateChannelStatusApi.valueOf(privateChannel.getStatus().toString())));
            }
        }
        return new ListPeerPrivateChannels(serviceProviderName, privateChannelsApis);
    }
}
