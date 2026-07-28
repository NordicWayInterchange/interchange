package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.service.exportmodel.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

public class ExportTransformer {

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        if (lastUpdated != null) {
            return ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
        return 0;
    }

    public ServiceProviderExportApi transformServiceProviderToServiceProviderExportApi(ServiceProvider serviceProvider) {
        return new ServiceProviderExportApi(
                serviceProvider.getName(),
                serviceProvider.isBiconsumer(),
                serviceProvider.getSubscriptions().stream().map(this::transformLocalSubscriptionToLocalSubscriptionExportApi).collect(Collectors.toSet()),
                serviceProvider.getCapabilities().getCapabilities().stream().map(this::transformCapabilityToCapabilityExportApi).collect(Collectors.toSet()),
                serviceProvider.getDeliveries().stream().map(this::transformDeliveryToDeliveryExportApi).collect(Collectors.toSet())
        );
    }

    public LocalSubscriptionExportApi transformLocalSubscriptionToLocalSubscriptionExportApi(LocalSubscription localSubscription) {
        return new LocalSubscriptionExportApi(
                localSubscription.getUuid(),
                localSubscription.getSelector(),
                localSubscription.getConsumerCommonName(),
                transformLocalSubscriptionStatusToLocalSubscriptionStatusExportApi(localSubscription.getStatus()),
                localSubscription.getLocalEndpoints().stream().map(this::transformLocalEndpointToLocalEndpointExportApi).collect(Collectors.toSet()),
                localSubscription.getConnections().stream().map(this::transformLocalConnectionToLocalConnectionExportApi).collect(Collectors.toSet()),
                localSubscription.getDescription()
                );
    }

    public LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi transformLocalSubscriptionStatusToLocalSubscriptionStatusExportApi(LocalSubscriptionStatus status) {
        switch (status) {
            case LocalSubscriptionStatus.CREATED -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.CREATED;
            }
            case LocalSubscriptionStatus.ILLEGAL -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.ILLEGAL;
            }
            case LocalSubscriptionStatus.TEAR_DOWN -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.TEAR_DOWN;
            }
            case LocalSubscriptionStatus.RESUBSCRIBE -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.RESUBSCRIBE;
            }
            case LocalSubscriptionStatus.ERROR -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.ERROR;
            }
            default -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.REQUESTED;
            }
        }
    }

    public LocalEndpointExportApi transformLocalEndpointToLocalEndpointExportApi(LocalEndpoint endpoint) {
        return new LocalEndpointExportApi(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getSource(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate());
    }

    public LocalConnectionExportApi transformLocalConnectionToLocalConnectionExportApi(LocalConnection localConnection) {
        return new LocalConnectionExportApi(localConnection.getSource(),
                localConnection.getDestination());
    }

    public CapabilityExportApi transformCapabilityToCapabilityExportApi(Capability capability) {
        return new CapabilityExportApi(
                capability.getUuid(),
                capability.getApplication().toApi(),
                transformMetadataToMetadataExportApi(capability.getMetadata(), capability.getShardCount()),
                transformCapabilityStatusToCapabilityStatusExportApi(capability.getStatus()),
                capability.getShards().stream().map(this::transformCapabilityShardToCapabilityShardExportApi).collect(Collectors.toSet()));
    }

    public CapabilityExportApi.CapabilityStatusExportApi transformCapabilityStatusToCapabilityStatusExportApi(CapabilityStatus status) {
        switch (status) {
            case CapabilityStatus.CREATED -> {
                return CapabilityExportApi.CapabilityStatusExportApi.CREATED;
            }
            case CapabilityStatus.TEAR_DOWN -> {
                return CapabilityExportApi.CapabilityStatusExportApi.TEAR_DOWN;
            }
            default -> {
                return CapabilityExportApi.CapabilityStatusExportApi.REQUESTED;
            }
        }
    }

    public MetadataExportApi transformMetadataToMetadataExportApi(Metadata metadata, int shardCount) {
        return new MetadataExportApi(shardCount,
                metadata.getInfoUrl(),
                transformRedirectStatusToRedirectStatusExportApi(metadata.getRedirectPolicy()),
                metadata.getMaxBandwidth(),
                metadata.getMaxMessageRate(),
                metadata.getRepetitionInterval()
        );
    }

    public MetadataExportApi.RedirectStatusExportApi transformRedirectStatusToRedirectStatusExportApi(RedirectStatus status) {
        switch (status) {
            case RedirectStatus.MANDATORY -> {
                return MetadataExportApi.RedirectStatusExportApi.MANDATORY;
            }
            case RedirectStatus.NOT_AVAILABLE -> {
                return MetadataExportApi.RedirectStatusExportApi.NOT_AVAILABLE;
            }
            default -> {
                return MetadataExportApi.RedirectStatusExportApi.OPTIONAL;
            }
        }
    }

    public CapabilityShardExportApi transformCapabilityShardToCapabilityShardExportApi(CapabilityShard shard) {
        return new CapabilityShardExportApi(shard.getShardId(),
                shard.getExchangeName(),
                shard.getSelector());
    }

    public DeliveryExportApi transformDeliveryToDeliveryExportApi(LocalDelivery delivery) {
        return new DeliveryExportApi(
                delivery.getUuid(),
                delivery.getEndpoints().stream().map(this::transformDeliveryEndpointToDeliveryEndpointExportApi).collect(Collectors.toSet()),
                delivery.getSelector(),
                transformDeliveryStatusToDeliveryStatusExportApi(delivery.getStatus()),
                delivery.getDescription(),
                delivery.isDlqueue()
                );
    }

    public DeliveryEndpointExportApi transformDeliveryEndpointToDeliveryEndpointExportApi(LocalDeliveryEndpoint endpoint) {
        return new DeliveryEndpointExportApi(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getTarget(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate(),
                endpoint.getDlqName()
        );
    }

    public DeliveryExportApi.DeliveryStatusExportApi transformDeliveryStatusToDeliveryStatusExportApi(LocalDeliveryStatus status) {
        switch(status) {
            case LocalDeliveryStatus.CREATED -> {
                return DeliveryExportApi.DeliveryStatusExportApi.CREATED;
            }
            case LocalDeliveryStatus.ILLEGAL -> {
                return DeliveryExportApi.DeliveryStatusExportApi.ILLEGAL;
            }
            case LocalDeliveryStatus.NOT_VALID -> {
                return DeliveryExportApi.DeliveryStatusExportApi.NOT_VALID;
            }
            case LocalDeliveryStatus.NO_OVERLAP -> {
                return DeliveryExportApi.DeliveryStatusExportApi.NO_OVERLAP;
            }
            case LocalDeliveryStatus.ERROR -> {
                return DeliveryExportApi.DeliveryStatusExportApi.ERROR;
            }
            default -> {
                return DeliveryExportApi.DeliveryStatusExportApi.REQUESTED;
            }
        }
    }

    public NeighbourExportApi transformNeighbourToNeighbourExportApi(Neighbour neighbour) {
        return new NeighbourExportApi(neighbour.getName(),
                transformNeighbourCapabilitiesToNeighbourCapabilitiesExportApi(neighbour.getCapabilities()),
                neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().stream().map(this::transformNeighbourSubscriptionToNeighbourSubscriptionExportApi).collect(Collectors.toSet()),
                neighbour.getOurRequestedSubscriptions().getSubscriptions().stream().map(this::transformSubscriptionToSubscriptionExportApi).collect(Collectors.toSet()),
                neighbour.getControlChannelPort()
        );
    }

    public NeighbourCapabilitiesExportApi transformNeighbourCapabilitiesToNeighbourCapabilitiesExportApi(NeighbourCapabilities neighbourCapabilities) {
        return new NeighbourCapabilitiesExportApi(transformLocalDateTimeToEpochMili(neighbourCapabilities.getLastCapabilityExchange()),
                neighbourCapabilities.getCapabilities().stream().map(this::transformNeighbourCapabilityToNeighbourCapabilityExportApi).collect(Collectors.toSet()),
                transformCapabilitiesStatusToCapabilitiesStatusExportApi(neighbourCapabilities.getStatus()),
                transformLocalDateTimeToEpochMili(neighbourCapabilities.getLastUpdated().orElseGet(() -> null))
        );
    }

    public NeighbourCapabilityExportApi transformNeighbourCapabilityToNeighbourCapabilityExportApi(NeighbourCapability neighbourCapability) {
        return new NeighbourCapabilityExportApi(neighbourCapability.getApplication().toApi(),
                transformMetadataToMetadataExportApi(neighbourCapability.getMetadata(), neighbourCapability.getShardCount())
        );
    }

    public NeighbourCapabilitiesExportApi.NeighbourCapabilitiesStatusExportApi transformCapabilitiesStatusToCapabilitiesStatusExportApi(CapabilitiesStatus status) {
        switch (status) {
            case CapabilitiesStatus.KNOWN -> {
                return NeighbourCapabilitiesExportApi.NeighbourCapabilitiesStatusExportApi.KNOWN;
            }
            case CapabilitiesStatus.FAILED -> {
                return NeighbourCapabilitiesExportApi.NeighbourCapabilitiesStatusExportApi.FAILED;
            }
            default -> {
                return NeighbourCapabilitiesExportApi.NeighbourCapabilitiesStatusExportApi.UNKNOWN;
            }
        }
    }

    public NeighbourSubscriptionExportApi transformNeighbourSubscriptionToNeighbourSubscriptionExportApi(NeighbourSubscription neighbourSubscription) {
        return new NeighbourSubscriptionExportApi(
                neighbourSubscription.getUuid(),
                transformNeighbourSubscriptionStatusToNeighbourSubscriptionStatusExportApi(neighbourSubscription.getSubscriptionStatus()),
                neighbourSubscription.getSelector(),
                neighbourSubscription.getPath(),
                neighbourSubscription.getConsumerCommonName(),
                neighbourSubscription.getEndpoints().stream().map(this::transformNeighbourEndpointToNeighbourEndpointExportApi).collect(Collectors.toSet())
        );
    }

    public NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi transformNeighbourSubscriptionStatusToNeighbourSubscriptionStatusExportApi(NeighbourSubscriptionStatus status) {
        switch (status) {
            case NeighbourSubscriptionStatus.ACCEPTED -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.ACCEPTED;
            }
            case NeighbourSubscriptionStatus.CREATED -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.CREATED;
            }
            case NeighbourSubscriptionStatus.ILLEGAL -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.ILLEGAL;
            }
            case NeighbourSubscriptionStatus.NOT_VALID -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.NOT_VALID;
            }
            case NeighbourSubscriptionStatus.NO_OVERLAP -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.NO_OVERLAP;
            }
            case NeighbourSubscriptionStatus.TEAR_DOWN -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.TEAR_DOWN;
            }
            default -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.REQUESTED;
            }
        }
    }

    public NeighbourEndpointExportApi transformNeighbourEndpointToNeighbourEndpointExportApi(NeighbourEndpoint endpoint) {
        return new NeighbourEndpointExportApi(endpoint.getSource(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate());
    }

    public SubscriptionExportApi transformSubscriptionToSubscriptionExportApi(Subscription subscription) {
        return new SubscriptionExportApi(subscription.getPath(),
                subscription.getSelector(),
                subscription.getConsumerCommonName(),
                transformSubscriptionStatusToSubscriptionStatusExportApi(subscription.getSubscriptionStatus()),
                subscription.getEndpoints().stream().map(this::transformEndpointToEndpointExportApi).collect(Collectors.toSet())
                );
    }

    public SubscriptionExportApi.SubscriptionStatusExportApi transformSubscriptionStatusToSubscriptionStatusExportApi(SubscriptionStatus status) {
        switch (status) {
            case SubscriptionStatus.CREATED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.CREATED;
            }
            case SubscriptionStatus.ILLEGAL -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.ILLEGAL;
            }
            case SubscriptionStatus.NO_OVERLAP -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.NO_OVERLAP;
            }
            case SubscriptionStatus.GIVE_UP -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.GIVE_UP;
            }
            case SubscriptionStatus.FAILED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.FAILED;
            }
            case SubscriptionStatus.TEAR_DOWN -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.TEAR_DOWN;
            }
            case SubscriptionStatus.RESUBSCRIBE -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.RESUBSCRIBE;
            }
            default -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.REQUESTED;
            }
        }
    }

    public EndpointExportApi transformEndpointToEndpointExportApi(Endpoint endpoint) {
        return new EndpointExportApi(endpoint.getSource(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate()
        );
    }

    public PrivateChannelExportApi transformPrivateChannelToPrivateChannelExportApi(PrivateChannel privateChannel) {
        return new PrivateChannelExportApi(
                privateChannel.getUuid(),
                privateChannel.getServiceProviderName(),
                privateChannel.getPeers().stream().map(this::transformPeerToPeerExportApi).collect(Collectors.toSet()),
                transformPrivateChannelStatusToPrivateChannelStatusExportApi(privateChannel.getStatus()),
                transformPrivateChannelEndpointToPrivateChannelEndpointExportApi(privateChannel.getEndpoint()),
                privateChannel.getDescription()
        );
    }

    public PeerExportApi transformPeerToPeerExportApi(Peer peer) {
        return new PeerExportApi(
                peer.getName(),
                peer.getUuid(),
                peer.getStatus().toString()
        );
    }

    public PrivateChannelExportApi.PrivateChannelStatusExportApi transformPrivateChannelStatusToPrivateChannelStatusExportApi(PrivateChannelStatus status) {
        switch (status) {
            case PrivateChannelStatus.CREATED -> {
                return PrivateChannelExportApi.PrivateChannelStatusExportApi.CREATED;
            }
            case PrivateChannelStatus.TEAR_DOWN -> {
                return PrivateChannelExportApi.PrivateChannelStatusExportApi.TEAR_DOWN;
            }
            default -> {
                return PrivateChannelExportApi.PrivateChannelStatusExportApi.REQUESTED;
            }
        }
    }

    public PrivateChannelEndpointExportApi transformPrivateChannelEndpointToPrivateChannelEndpointExportApi(PrivateChannelEndpoint endpoint) {
        return new PrivateChannelEndpointExportApi(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getQueueName()
        );
    }
}
