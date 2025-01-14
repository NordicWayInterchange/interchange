package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.neighbour.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    public List<NeighbourApi> neighbourListToNeighbourApiList(List<Neighbour> neighbourList) {
        List<NeighbourApi> neighbourApiList = new ArrayList<>();
        for (Neighbour neighbour : neighbourList) {
            neighbourApiList.add(new NeighbourApi(
                    neighbour.getNeighbour_id(),
                    neighbour.getName(),
                    neighbourCapabilitiesToNeighbourCapabilitiesApi(neighbour.getCapabilities()),
                    neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(neighbour.getNeighbourRequestedSubscriptions()),
                    subscriptionRequestToSubscriptionRequestApi(neighbour.getOurRequestedSubscriptions()),
                    connectionStatusToConnectionStatusApi(neighbour.getControlConnection().getConnectionStatus()),
                    localDateTimeToTimestamp(neighbour.getControlConnection().getLastFailedConnectionAttempt()),
                    localDateTimeToTimestamp(neighbour.getLastUpdated()),
                    neighbour.isIgnore()
            ));
        }
        return neighbourApiList;
    }

    public List<InterchangeApi> interchangeListToInterchangeApiList(List<ServiceProvider> interchangeList) {
        List<InterchangeApi> interchangeApiList = new ArrayList<>();
        for (ServiceProvider serviceProvider : interchangeList) {
            interchangeApiList.add(new InterchangeApi(
                    localSubscriptionSetToSubscriptionApiSet(serviceProvider.getSubscriptions()),
                    null,
                    //capablitiesSetToCapabilitiesApiSet(serviceProvider.getCapabilities()),
                    localDeliveriesSetToDeliveriesApiSet(serviceProvider.getDeliveries()))

            );
        }
        return interchangeApiList;
    }


    public ConnectionStatusApi connectionStatusToConnectionStatusApi(ConnectionStatus status) {
        return ConnectionStatusApi.valueOf(status.toString());
    }

    public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(SubscriptionRequest subscriptionRequest) {
        return new SubscriptionRequestApi(
                subscriptionRequest.getSubreq_id(),
                subscriptionSetToSubscriptionApiSet(subscriptionRequest.getSubscriptions()),
                localDateTimeToTimestamp(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }

    public Set<SubscriptionApi> subscriptionSetToSubscriptionApiSet(Set<Subscription> subscriptionSet) {
        Set<SubscriptionApi> subscriptionApiSet = new HashSet<>();
        for (Subscription subscription : subscriptionSet) {
            subscriptionApiSet.add(new SubscriptionApi(
                    subscription.getId(),
                    subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus()),
                    subscription.getSelector(),
                    subscription.getPath(),
                    subscription.getNumberOfPolls(),
                    subscription.getConsumerCommonName(),
                    endpointSetToEndpointApiSet(subscription.getEndpoints()),
                    timestampMillisecondsToSeconds(subscription.getLastUpdatedTimestamp())
            ));
        }
        return subscriptionApiSet;
    }

    public Set<EndpointApi> endpointSetToEndpointApiSet(Set<Endpoint> subscriptionEndpointSet) {
        Set<EndpointApi> endpointApiSet = new HashSet<>();
        for (Endpoint endpoint : subscriptionEndpointSet) {
            endpointApiSet.add(new EndpointApi(
                    endpoint.getId(),
                    endpoint.getSource(),
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate(),
                    subscriptionShardToSubscriptionShardApi(endpoint.getShard())
            ));
        }
        return endpointApiSet;
    }

    public Set<InterchangeSubscriptionEndpointApi> localEndpointSetToEndpointApiSet(Set<LocalEndpoint> subscriptionLocalEndpointSet) {
        Set<InterchangeSubscriptionEndpointApi> endpointApiSet = new HashSet<>();
        for (LocalEndpoint endpoint : subscriptionLocalEndpointSet) {
            endpointApiSet.add(new InterchangeSubscriptionEndpointApi(
                    null,
                    endpoint.getSource(),
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return endpointApiSet;
    }

    public Set<InterchangeDeliveryEndpointApi> localDeliveryEndpointSetToEndpointApiSet(Set<LocalDeliveryEndpoint> deliveryLocalEndpointSet) {
        Set<InterchangeDeliveryEndpointApi> deliveryEndpointApiSet = new HashSet<>();
        for (LocalDeliveryEndpoint endpoint : deliveryLocalEndpointSet) {
            deliveryEndpointApiSet.add(new InterchangeDeliveryEndpointApi(
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getTarget(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return deliveryEndpointApiSet;
    }

    public Set<InterchangeCapabilityApi> capablitiesSetToCapabilitiesApiSet(Set<Capability> capabilitiesSet) {
        Set<InterchangeCapabilityApi> capabilityApiSet = new HashSet<>();
        for (Capability capability : capabilitiesSet) {
            capabilityApiSet.add(new InterchangeCapabilityApi(
                    capability.getId(),
                    capability.getApplication(),
                    capability.getMetadata(),
                    null
            ));
        }
        return capabilityApiSet;
    }

    public SubscriptionShardApi subscriptionShardToSubscriptionShardApi(SubscriptionShard subscriptionShard) {
        if(subscriptionShard != null) {
            return new SubscriptionShardApi(
                    subscriptionShard.getId(),
                    subscriptionShard.getExchangeName()
            );
        }
        else return null;
    }

    public SubscriptionStatusApi subscriptionStatusToSubscriptionStatusApi(SubscriptionStatus subscriptionStatus) {
        return SubscriptionStatusApi.valueOf(subscriptionStatus.toString());
    }

    public InterchangeSubscriptionStatusApi localSubscriptionStatusToSubscriptionStatusApi(LocalSubscriptionStatus localSubscriptionStatus) {
        return InterchangeSubscriptionStatusApi.valueOf(localSubscriptionStatus.toString());
    }

    public InterchangeDeliveryStatus localDeliveryStatusToDeliveryStatusApi(LocalDeliveryStatus localDeliveryStatus) {
        return InterchangeDeliveryStatus.valueOf(localDeliveryStatus.toString());
    }

    public NeighbourSubscriptionRequestApi neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(NeighbourSubscriptionRequest subscriptionRequest) {
        return new NeighbourSubscriptionRequestApi(
                subscriptionRequest.getSubreq_id(),
                neighbourSubscriptionSetToNeighbourSubscriptionRequestApiSet(subscriptionRequest.getSubscriptions()),
                localDateTimeToTimestamp(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }


    public Set<InterchangeSubscriptionApi> localSubscriptionSetToSubscriptionApiSet(Set<LocalSubscription> subscriptionSet) {
        Set<InterchangeSubscriptionApi> subscriptionApiSet = new HashSet<>();
        for (LocalSubscription subscription : subscriptionSet) {
            subscriptionApiSet.add(new InterchangeSubscriptionApi(
                    subscription.getId().toString(),
                    localSubscriptionStatusToSubscriptionStatusApi(subscription.getStatus()),
                    subscription.getSelector(),
                    localEndpointSetToEndpointApiSet(subscription.getLocalEndpoints()),
                    null
            ));
        }
        return subscriptionApiSet;
    }


    public Set<InterchangeDeliveryApi> localDeliveriesSetToDeliveriesApiSet(Set<LocalDelivery> deliveriesSet) {
        Set<InterchangeDeliveryApi> deliveriesApiSet = new HashSet<>();
        for (LocalDelivery delivery : deliveriesSet) {
            deliveriesApiSet.add(new InterchangeDeliveryApi(
                    delivery.getId().toString(),
                    delivery.getSelector(),
                    localDeliveryStatusToDeliveryStatusApi(delivery.getStatus()),
                    localDeliveryEndpointSetToEndpointApiSet(delivery.getEndpoints()),
                    null
            ));
        }
        return deliveriesApiSet;
    }


    public Set<NeighbourSubscriptionApi> neighbourSubscriptionSetToNeighbourSubscriptionRequestApiSet(Set<NeighbourSubscription> neighbourSubscriptions) {
        Set<NeighbourSubscriptionApi> neighbourSubscriptionApiSet = new HashSet<>();
        for (NeighbourSubscription neighbourSubscription : neighbourSubscriptions) {
            neighbourSubscriptionApiSet.add(new NeighbourSubscriptionApi(
                    neighbourSubscription.getId(),
                    NeighbourSubscriptionStatusApi.CREATED,
                    neighbourSubscription.getSelector(),
                    neighbourSubscription.getPath(),
                    neighbourSubscription.getConsumerCommonName(),
                    neighbourEndpointSetToNeighbourEndpointApiSet(neighbourSubscription.getEndpoints()),
                    timestampMillisecondsToSeconds(neighbourSubscription.getLastUpdatedTimestamp())
            ));
        }
        return neighbourSubscriptionApiSet;
    }

    public Set<NeighbourEndpointApi> neighbourEndpointSetToNeighbourEndpointApiSet(Set<NeighbourEndpoint> neighbourEndpoints) {
        Set<NeighbourEndpointApi> neighbourEndpointApiSet = new HashSet<>();
        for (NeighbourEndpoint neighbourEndpoint : neighbourEndpoints) {
            neighbourEndpointApiSet.add(new NeighbourEndpointApi(
                    neighbourEndpoint.getId(),
                    neighbourEndpoint.getSource(),
                    neighbourEndpoint.getHost(),
                    neighbourEndpoint.getPort(),
                    neighbourEndpoint.getMaxBandwidth(),
                    neighbourEndpoint.getMaxMessageRate()
            ));
        }
        return neighbourEndpointApiSet;
    }

    public NeighbourCapabilitiesApi neighbourCapabilitiesToNeighbourCapabilitiesApi(NeighbourCapabilities neighbourCapabilities) {
        return new NeighbourCapabilitiesApi(
                neighbourCapabilities.getId(),
                capabilitiesStatusToCapabilitiesStatusApi(neighbourCapabilities.getStatus()),
                neighbourCapabilitySetToNeighbourCapabilityApiSet(neighbourCapabilities.getCapabilities()),
                localDateTimeToTimestamp(neighbourCapabilities.getLastUpdated().orElse(null)),
                localDateTimeToTimestamp(neighbourCapabilities.getLastCapabilityExchange())
        );
    }

    public Set<NeighbourCapabilityApi> neighbourCapabilitySetToNeighbourCapabilityApiSet(Set<NeighbourCapability> neighbourCapabilities) {
        Set<NeighbourCapabilityApi> neighbourCapabilityApiSet = new HashSet<>();
        for (NeighbourCapability capability : neighbourCapabilities) {
            neighbourCapabilityApiSet.add(neighbourCapabilityToNeighbourCapabilityApi(capability));
        }
        return neighbourCapabilityApiSet;
    }

    public NeighbourCapabilityApi neighbourCapabilityToNeighbourCapabilityApi(NeighbourCapability neighbourCapability) {
        return new NeighbourCapabilityApi(
                neighbourCapability.getId(),
                neighbourCapability.getApplication().toApi(),
                neighbourCapability.getMetadata().toApi(),
                localDateTimeToTimestamp(neighbourCapability.getCreatedTimestamp())
        );
    }

    public CapabilitiesStatusApi capabilitiesStatusToCapabilitiesStatusApi(CapabilitiesStatus capabilitiesStatus) {
        return CapabilitiesStatusApi.valueOf(capabilitiesStatus.toString());
    }

    private Long localDateTimeToTimestamp(LocalDateTime lastUpdated) {
        Long epochSecond = null;
        if (lastUpdated != null) {
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        return epochSecond;
    }
    private Long timestampMillisecondsToSeconds(Long timestamp){
        return timestamp/1000;
    }
}
