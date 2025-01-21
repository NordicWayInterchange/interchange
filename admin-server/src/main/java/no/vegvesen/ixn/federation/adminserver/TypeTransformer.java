package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.neighbour.*;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.*;
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

    public List<ServiceProviderApi> serviceProviderListToServiceProviderApiList(List<ServiceProvider> serviceProviderList) {
        List<ServiceProviderApi> serviceProviderApiList = new ArrayList<>();
        for (ServiceProvider serviceProvider : serviceProviderList) {
            serviceProviderApiList.add(new ServiceProviderApi(
                    serviceProvider.getName(),
                    localSubscriptionSetToSubscriptionApiSet(serviceProvider.getSubscriptions()),
                    capabilitiesSetToCapabilitiesApiSet(serviceProvider.getCapabilities().getCapabilities()),
                    localDeliveriesSetToDeliveriesApiSet(serviceProvider.getDeliveries()))
            );
        }
        return serviceProviderApiList;
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

    public Set<LocalSubscriptionEndpointApi> localEndpointSetToEndpointApiSet(Set<LocalEndpoint> subscriptionLocalEndpointSet) {
        Set<LocalSubscriptionEndpointApi> endpointApiSet = new HashSet<>();
        for (LocalEndpoint endpoint : subscriptionLocalEndpointSet) {
            endpointApiSet.add(new LocalSubscriptionEndpointApi(
                    endpoint.getId(),
                    endpoint.getSource(),
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return endpointApiSet;
    }

    public Set<LocalDeliveryEndpointApi> localDeliveryEndpointSetToEndpointApiSet(Set<LocalDeliveryEndpoint> deliveryLocalEndpointSet) {
        Set<LocalDeliveryEndpointApi> deliveryEndpointApiSet = new HashSet<>();
        for (LocalDeliveryEndpoint endpoint : deliveryLocalEndpointSet) {
            deliveryEndpointApiSet.add(new LocalDeliveryEndpointApi(
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getTarget(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return deliveryEndpointApiSet;
    }

    public Set<CapabilityApi> capabilitiesSetToCapabilitiesApiSet(Set<Capability> capabilities) {

        Set<CapabilityApi> capabilityApiSet = new HashSet<>();
        for (Capability capability : capabilities) {
            capabilityApiSet.add(new CapabilityApi(
                    capability.getApplication(),
                    capability.getMetadata(),
                    capability.getShards(),
                    capability.getStatus(),
                    localDateTimeToTimestamp(capability.getCreatedTimestamp())
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

    public LocalSubscriptionStatusApi localSubscriptionStatusToSubscriptionStatusApi(LocalSubscriptionStatus localSubscriptionStatus) {
        return LocalSubscriptionStatusApi.valueOf(localSubscriptionStatus.toString());
    }

    public ServiceProviderDeliveryStatus localDeliveryStatusToDeliveryStatusApi(LocalDeliveryStatus localDeliveryStatus) {
        return ServiceProviderDeliveryStatus.valueOf(localDeliveryStatus.toString());
    }

    public NeighbourSubscriptionRequestApi neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(NeighbourSubscriptionRequest subscriptionRequest) {
        return new NeighbourSubscriptionRequestApi(
                subscriptionRequest.getSubreq_id(),
                neighbourSubscriptionSetToNeighbourSubscriptionRequestApiSet(subscriptionRequest.getSubscriptions()),
                localDateTimeToTimestamp(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }


    public Set<LocalSubscriptionApi> localSubscriptionSetToSubscriptionApiSet(Set<LocalSubscription> subscriptionSet) {
        Set<LocalSubscriptionApi> subscriptionApiSet = new HashSet<>();
        for (LocalSubscription subscription : subscriptionSet) {
            subscriptionApiSet.add(new LocalSubscriptionApi(
                    subscription.getId().toString(),
                    localSubscriptionStatusToSubscriptionStatusApi(subscription.getStatus()),
                    subscription.getSelector(),
                    subscription.getConsumerCommonName(),
                    subscription.getDescription(),
                    subscription.getErrorMessage(),
                    subscription.getConnections(),
                    localEndpointSetToEndpointApiSet(subscription.getLocalEndpoints()),
                    localDateTimeToTimestamp(subscription.getLastUpdated())
            ));
        }
        return subscriptionApiSet;
    }


    public Set<LocalDeliveryApi> localDeliveriesSetToDeliveriesApiSet(Set<LocalDelivery> deliveriesSet) {
        Set<LocalDeliveryApi> deliveriesApiSet = new HashSet<>();
        for (LocalDelivery delivery : deliveriesSet) {
            deliveriesApiSet.add(new LocalDeliveryApi(
                    delivery.getId().toString(),
                    delivery.getSelector(),
                    localDeliveryStatusToDeliveryStatusApi(delivery.getStatus()),
                    localDeliveryEndpointSetToEndpointApiSet(delivery.getEndpoints()),
                    localDateTimeToTimestamp(delivery.getLastUpdatedTimestamp())
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
