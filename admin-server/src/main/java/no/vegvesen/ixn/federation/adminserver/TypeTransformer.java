package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.exchange.ExchangeApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PrivateChannelApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PrivateChannelEndpointApi;
import no.vegvesen.ixn.federation.adminserver.model.privateChannel.PrivateChannelStatusApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.LocalConnectionApi;
import no.vegvesen.ixn.federation.adminserver.model.neighbour.*;
import no.vegvesen.ixn.federation.adminserver.model.queue.QueueApi;
import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.*;
import no.vegvesen.ixn.federation.adminserver.qpid.Exchange;
import no.vegvesen.ixn.federation.adminserver.qpid.Queue;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
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
                    neighbour.isIgnore(),
                    connectionToConnectionApi(neighbour.getControlConnection()),
                    neighbour.getControlChannelPort()
            ));
        }
        return neighbourApiList;
    }

    public ConnectionApi connectionToConnectionApi(Connection connection) {
        return new ConnectionApi(
                connection.getId(),
                connection.getBackoffStart(),
                connection.getBackoffAttempts(),
                connectionStatusToConnectionStatusApi(connection.getConnectionStatus()),
                connection.getUnreachableTime(),
                connection.getLastFailedConnectionAttempt()
        );
    }

    public List<ServiceProviderApi> serviceProviderListToServiceProviderApiList(List<ServiceProvider> serviceProviderList) {
        List<ServiceProviderApi> serviceProviderApiList = new ArrayList<>();
        for (ServiceProvider serviceProvider : serviceProviderList) {
            serviceProviderApiList.add(new ServiceProviderApi(
                    serviceProvider.getId(),
                    serviceProvider.getName(),
                    localSubscriptionSetToSubscriptionApiList(serviceProvider.getSubscriptions()),
                    capabilitiesSetToCapabilitiesApiList(serviceProvider.getCapabilities().getCapabilities()),
                    localDeliveriesSetToDeliveriesApiList(serviceProvider.getDeliveries()))
            );
        }
        return serviceProviderApiList.stream().sorted().toList();
    }

    public List<CapabilityApi> capabilitiesToGetMatchingCapabilitiesApiList(Set<Capability> capabilities, Set<NeighbourCapability> neighbourCapabilities ) {

        List<CapabilityApi> matchingCapabilities = new ArrayList<>();
        for (Capability capability : capabilities) {
            matchingCapabilities.add(new CapabilityApi(
                    capability.getId(),
                    capability.getApplication().toApi(),
                    capability.getMetadata().toApi(),
                    capabilityShardSetToCapabilityShardSetApi(capability.getShards()),
                    capabilityStatusToCapabilityStatusApi(capability.getStatus()),
                    localDateTimeToTimestamp(capability.getCreatedTimestamp())
            ));
        }
        for (NeighbourCapability neighbourCapability : neighbourCapabilities) {
            matchingCapabilities.add(new CapabilityApi(
                    neighbourCapability.getId(),
                    neighbourCapability.getApplication().toApi(),
                    neighbourCapability.getMetadata().toApi(),
                    null,
                    null,
                    localDateTimeToTimestamp(neighbourCapability.getCreatedTimestamp())
            ));
        }
        return matchingCapabilities.stream().sorted().toList();
    }

    public List<PrivateChannelApi> privateChannelListToPrivateChannelApiList(List<PrivateChannel> privateChannelList) {
        List<PrivateChannelApi> privateChannelApiList = new ArrayList<>();
        for (PrivateChannel privateChannel : privateChannelList) {
            privateChannelApiList.add(new PrivateChannelApi(
                    privateChannel.getUuid(),
                    privateChannel.getPeers(),
                    privateChannelStatusToPrivateChannelStatusApi(privateChannel.getStatus()),
                    privateChannel.getDescription(),
                    privateChannelEndpointToPrivateChannelEndpointApi(privateChannel.getEndpoint()),
                    privateChannel.getServiceProviderName(),
                    localDateTimeToTimestamp(privateChannel.getLastUpdated())
            ));
        }
        return privateChannelApiList.stream().sorted().toList();
    }

    public List<ExchangeApi> exchangeListToExchangeApiList(List<Exchange> exchangeList) {
        List<ExchangeApi> exchangeApiList = new ArrayList<>();
        for (Exchange exchange : exchangeList) {
            exchangeApiList.add(new ExchangeApi(
                    exchange.getName(),
                    exchange.getId(),
                    exchange.isDurable(),
                    exchange.getType(),
                    exchange.getBindings())
            );
        }
        return exchangeApiList;
    }

    public List<QueueApi> queueListToQueueApiList(List<Queue> queueList) {
        List<QueueApi> queueApiList = new ArrayList<>();
        for (Queue queue : queueList) {
            queueApiList.add(new QueueApi(
                    queue.getName(),
                    queue.getId(),
                    queue.getDurable(),
                    queue.getMaximumMessageTtl(),
                    queue.getEnsureNondestructiveConsumers())
            );
        }
        return queueApiList;
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

    public List<SubscriptionApi> subscriptionSetToSubscriptionApiSet(Set<Subscription> subscriptionSet) {
        List<SubscriptionApi> subscriptionApiSet = new ArrayList<>();
        for (Subscription subscription : subscriptionSet) {
            subscriptionApiSet.add(new SubscriptionApi(
                    subscription.getId(),
                    subscriptionStatusToSubscriptionStatusApi(subscription.getSubscriptionStatus()),
                    subscription.getSelector(),
                    subscription.getPath(),
                    subscription.getNumberOfPolls(),
                    subscription.getConsumerCommonName(),
                    endpointSetToEndpointApiSet(subscription.getEndpoints()),
                    subscription.getLastUpdatedTimestamp()
            ));
        }
        return subscriptionApiSet.stream().sorted().toList();
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

    public PrivateChannelEndpointApi privateChannelEndpointToPrivateChannelEndpointApi(PrivateChannelEndpoint privateChannelEndpoint) {
        return new PrivateChannelEndpointApi(privateChannelEndpoint.getHost(), privateChannelEndpoint.getPort(), privateChannelEndpoint.getQueueName());
    }

    public List<CapabilityApi> capabilitiesSetToCapabilitiesApiList(Set<Capability> capabilities) {
        List<CapabilityApi> capabilityApiList = new ArrayList<>();
        for (Capability capability : capabilities) {
            capabilityApiList.add(new CapabilityApi(
                    capability.getId(),
                    capability.getApplication().toApi(),
                    capability.getMetadata().toApi(),
                    capabilityShardSetToCapabilityShardSetApi(capability.getShards()),
                    capabilityStatusToCapabilityStatusApi(capability.getStatus()),
                    localDateTimeToTimestamp(capability.getCreatedTimestamp())
            ));
        }
        return capabilityApiList.stream().sorted().toList();
    }

    public CapabilityStatusApi capabilityStatusToCapabilityStatusApi(CapabilityStatus capabilityStatus) {
        return CapabilityStatusApi.valueOf(capabilityStatus.toString());
    }

    public SubscriptionShardApi subscriptionShardToSubscriptionShardApi(SubscriptionShard subscriptionShard) {
        if (subscriptionShard != null) {
            return new SubscriptionShardApi(
                    subscriptionShard.getId(),
                    subscriptionShard.getExchangeName()
            );
        } else return null;
    }

    public SubscriptionStatusApi subscriptionStatusToSubscriptionStatusApi(SubscriptionStatus subscriptionStatus) {
        return SubscriptionStatusApi.valueOf(subscriptionStatus.toString());
    }

    public LocalSubscriptionStatusApi localSubscriptionStatusToSubscriptionStatusApi(LocalSubscriptionStatus localSubscriptionStatus) {
        return LocalSubscriptionStatusApi.valueOf(localSubscriptionStatus.toString());
    }

    public LocalDeliveryStatusApi localDeliveryStatusToDeliveryStatusApi(LocalDeliveryStatus localDeliveryStatus) {
        return LocalDeliveryStatusApi.valueOf(localDeliveryStatus.toString());
    }

    public PrivateChannelStatusApi privateChannelStatusToPrivateChannelStatusApi(PrivateChannelStatus privateChannelStatus) {
        return PrivateChannelStatusApi.valueOf(privateChannelStatus.toString());
    }

    public NeighbourSubscriptionRequestApi neighbourSubscriptionRequestToNeighbourSubscriptionRequestApi(NeighbourSubscriptionRequest subscriptionRequest) {
        return new NeighbourSubscriptionRequestApi(
                subscriptionRequest.getSubreq_id(),
                neighbourSubscriptionSetToNeighbourSubscriptionApiList(subscriptionRequest.getSubscriptions()),
                localDateTimeToTimestamp(subscriptionRequest.getSuccessfulRequest().orElse(null))
        );
    }


    public List<LocalSubscriptionApi> localSubscriptionSetToSubscriptionApiList(Set<LocalSubscription> subscriptionSet) {
        List<LocalSubscriptionApi> subscriptionApiList = new ArrayList<>();
        for (LocalSubscription subscription : subscriptionSet) {
            subscriptionApiList.add(new LocalSubscriptionApi(
                    subscription.getId().toString(),
                    localSubscriptionStatusToSubscriptionStatusApi(subscription.getStatus()),
                    subscription.getSelector(),
                    subscription.getConsumerCommonName(),
                    subscription.getDescription(),
                    subscription.getErrorMessage(),
                    localConnectionToLocalConnectionApiSet(subscription.getConnections()),
                    localEndpointSetToEndpointApiSet(subscription.getLocalEndpoints()),
                    localDateTimeToTimestamp(subscription.getLastUpdated())
            ));
        }
        return subscriptionApiList.stream().sorted().toList();
    }

    public Set<LocalConnectionApi> localConnectionToLocalConnectionApiSet(Set<LocalConnection> localConnectionSet) {
        Set<LocalConnectionApi> localConnectionApiSet = new HashSet<>();
        for (LocalConnection localConnection : localConnectionSet) {
            localConnectionApiSet.add(new LocalConnectionApi(localConnection.getId(), localConnection.getSource(), localConnection.getDestination()));
        }
        return localConnectionApiSet;
    }

    public List<LocalDeliveryApi> localDeliveriesSetToDeliveriesApiList(Set<LocalDelivery> deliveriesSet) {
        List<LocalDeliveryApi> deliveriesApiList = new ArrayList<>();
        for (LocalDelivery delivery : deliveriesSet) {
            deliveriesApiList.add(new LocalDeliveryApi(
                    delivery.getId().toString(),
                    delivery.getSelector(),
                    localDeliveryStatusToDeliveryStatusApi(delivery.getStatus()),
                    localDeliveryEndpointSetToEndpointApiSet(delivery.getEndpoints()),
                    delivery.getDescription(),
                    localDateTimeToTimestamp(delivery.getLastUpdatedTimestamp())
            ));
        }
        return deliveriesApiList.stream().sorted().toList();
    }


    public List<NeighbourSubscriptionApi> neighbourSubscriptionSetToNeighbourSubscriptionApiList(Set<NeighbourSubscription> neighbourSubscriptions) {
        List<NeighbourSubscriptionApi> neighbourSubscriptionApiList = new ArrayList<>();
        for (NeighbourSubscription neighbourSubscription : neighbourSubscriptions) {
            neighbourSubscriptionApiList.add(new NeighbourSubscriptionApi(
                    neighbourSubscription.getId(),
                    NeighbourSubscriptionStatusApi.CREATED,
                    neighbourSubscription.getSelector(),
                    neighbourSubscription.getPath(),
                    neighbourSubscription.getConsumerCommonName(),
                    neighbourEndpointSetToNeighbourEndpointApiSet(neighbourSubscription.getEndpoints()),
                    neighbourSubscription.getLastUpdatedTimestamp()
            ));
        }
        return neighbourSubscriptionApiList.stream().sorted().toList();
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

    public List<NeighbourCapabilityApi> neighbourCapabilitySetToNeighbourCapabilityApiSet(Set<NeighbourCapability> neighbourCapabilities) {
        List<NeighbourCapabilityApi> neighbourCapabilityApiList = new ArrayList<>();
        for (NeighbourCapability capability : neighbourCapabilities) {
            neighbourCapabilityApiList.add(neighbourCapabilityToNeighbourCapabilityApi(capability));
        }
        return neighbourCapabilityApiList.stream().sorted().toList();
    }

    public Set<CapabilityShardApi> capabilityShardSetToCapabilityShardSetApi(List<CapabilityShard> capabilityShards) {
        Set<CapabilityShardApi> capabilityShardApiSet = new HashSet<>();
        for (CapabilityShard capabilityShard : capabilityShards) {
            capabilityShardApiSet.add(capabilityShardToCapabilityShardApi(capabilityShard));
        }
        return capabilityShardApiSet;
    }

    public CapabilityShardApi capabilityShardToCapabilityShardApi(CapabilityShard capabilityShard) {
        return new CapabilityShardApi(
                capabilityShard.getShardId(),
                capabilityShard.getExchangeName(),
                capabilityShard.getSelector()
        );
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
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return epochSecond;
    }
}
