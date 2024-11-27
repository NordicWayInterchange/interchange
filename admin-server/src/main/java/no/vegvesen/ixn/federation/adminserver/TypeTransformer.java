package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    public ListNeighboursResponse neighbourListToListNeighboursResponse(List<Neighbour> neighbourList) {
        return new ListNeighboursResponse(neighbourListToNeighbourApiList(neighbourList));
    }

    public List<NeighbourApi> neighbourListToNeighbourApiList(List<Neighbour> neighbourList) {
        List<NeighbourApi> neighbourApiList = new ArrayList<>();
        for (Neighbour neighbour : neighbourList) {
            neighbourApiList.add(new NeighbourApi(
                    neighbour.getName(),
                    neighbourCapabilitiesToNeighbourCapabilitiesApi(neighbour.getCapabilities()),
                    neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(neighbour.getNeighbourRequestedSubscriptions()),
                    subscriptionRequestToSubscriptionRequestApi(neighbour.getOurRequestedSubscriptions()),
                    connectionToConnectionApi(neighbour.getControlConnection()),
                    localDateTimeToEpochMili(neighbour.getLastUpdated()),
                    neighbour.isIgnore(),
                    neighbour.getControlChannelPort()
            ));
        }
        return neighbourApiList;
    }

    public ConnectionApi connectionToConnectionApi(Connection connection) {
        return new ConnectionApi(
           localDateTimeToEpochMili(connection.getBackoffStartTime()),
                connection.getBackoffAttempts(),
                connectionStatusToConnectionStatusApi(connection.getConnectionStatus()),
                localDateTimeToEpochMili(connection.getUnreachableTime()),
                localDateTimeToEpochMili(connection.getLastFailedConnectionAttempt())
        );
    }

    public ConnectionStatusApi connectionStatusToConnectionStatusApi(ConnectionStatus status){
        return ConnectionStatusApi.valueOf(status.toString());
    }
    public SubscriptionRequestApi subscriptionRequestToSubscriptionRequestApi(SubscriptionRequest subscriptionRequest) {
        return new SubscriptionRequestApi(
                subscriptionSetToSubscriptionApiSet(subscriptionRequest.getSubscriptions()),
                localDateTimeToEpochMili(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }

    public Set<SubscriptionApi> subscriptionSetToSubscriptionApiSet(Set<Subscription> subscriptionSet) {
        Set<SubscriptionApi> subscriptionApiSet = new HashSet<>();
        for (Subscription subscription : subscriptionSet) {
            subscriptionApiSet.add(new SubscriptionApi(
                    subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus()),
                    subscription.getSelector(),
                    subscription.getPath(),
                    subscription.getNumberOfPolls(),
                    subscription.getConsumerCommonName(),
                    endpointSetToEndpointApiSet(subscription.getEndpoints()),
                    subscription.getLastUpdatedTimestamp()
            ));
        }
        return subscriptionApiSet;
    }

    public Set<EndpointApi> endpointSetToEndpointApiSet(Set<Endpoint> subscriptionEndpointSet) {
        Set<EndpointApi> endpointApiSet = new HashSet<>();
        for (Endpoint endpoint : subscriptionEndpointSet) {
            endpointApiSet.add(new EndpointApi(
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

    public SubscriptionShardApi subscriptionShardToSubscriptionShardApi(SubscriptionShard subscriptionShard) {
        return new SubscriptionShardApi(
                subscriptionShard.getExchangeName()
        );
    }

    public SubscriptionStatusApi subscriptionStatusToSubscriptionStatusApi(SubscriptionStatus subscriptionStatus) {
        return SubscriptionStatusApi.valueOf(subscriptionStatus.toString());
    }

    public NeighbourSubscriptionRequestApi neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(NeighbourSubscriptionRequest subscriptionRequest) {
        return new NeighbourSubscriptionRequestApi(
                neighbourSubscriptionSetToNeighbourSubscriptionRequestApiSet(subscriptionRequest.getSubscriptions()),
                localDateTimeToEpochMili(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }

    public Set<NeighbourSubscriptionApi> neighbourSubscriptionSetToNeighbourSubscriptionRequestApiSet(Set<NeighbourSubscription> neighbourSubscriptions) {
        Set<NeighbourSubscriptionApi> neighbourSubscriptionApiSet = new HashSet<>();
        for (NeighbourSubscription neighbourSubscription : neighbourSubscriptions) {
            neighbourSubscriptionApiSet.add(new NeighbourSubscriptionApi(
                    neighbourSubscription.getUuid(),
                    NeighbourSubscriptionStatusApi.CREATED,
                    neighbourSubscription.getSelector(),
                    neighbourSubscription.getPath(),
                    neighbourSubscription.getConsumerCommonName(),
                    neighbourEndpointSetToNeighbourEndpointApiSet(neighbourSubscription.getEndpoints()),
                    neighbourSubscription.getLastUpdatedTimestamp()
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
                capabilitiesStatusToCapabilitiesStatusApi(neighbourCapabilities.getStatus()),
                neighbourCapabilitySetToNeighbourCapabilityApiSet(neighbourCapabilities.getCapabilities()),
                localDateTimeToEpochMili(neighbourCapabilities.getLastUpdated().orElse(null)),
                localDateTimeToEpochMili(neighbourCapabilities.getLastCapabilityExchange())
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
                neighbourCapability.getApplication().toApi(),
                neighbourCapability.getMetadata().toApi(),
                localDateTimeToEpochMili(neighbourCapability.getCreatedTimestamp())
        );
    }

    public CapabilitiesStatusApi capabilitiesStatusToCapabilitiesStatusApi(CapabilitiesStatus capabilitiesStatus) {
        return CapabilitiesStatusApi.valueOf(capabilitiesStatus.toString());
    }

    private long localDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
