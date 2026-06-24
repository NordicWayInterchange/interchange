package no.vegvesen.ixn.federation.service;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.service.importmodel.*;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.stream.Collectors;

public class ImportTransformer {

    public CapabilityToCapabilityApiTransformer capabilityTransformer = new CapabilityToCapabilityApiTransformer();

    public LocalDateTime convertLongToLocalDateTime(long importTimestamp) {
        Instant instant = Instant.ofEpochMilli(importTimestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }

    public ServiceProvider transformServiceProviderImportApiToServiceProvider(ServiceProviderImportApi serviceProvider) {
        return new ServiceProvider(serviceProvider.getName(),
                serviceProvider.getBiconsumer(),
                new Capabilities(serviceProvider.getCapabilities().stream().map(this::transformCapabilityImportApiToCapability).collect(Collectors.toSet())),
                serviceProvider.getSubscriptions().stream().map(this::transformLocalSubscriptionImportApiToLocalSubscription).collect(Collectors.toSet()),
                serviceProvider.getDeliveries().stream().map(this::transformDeliveryImportApiToLocalDelivery).collect(Collectors.toSet()),
                convertLongToLocalDateTime(serviceProvider.getSubscriptionsUpdated())
        );
    }

    public LocalSubscription transformLocalSubscriptionImportApiToLocalSubscription(LocalSubscriptionImportApi localSubscription) {
        return new LocalSubscription(
                localSubscription.getUuid(),
                LocalSubscriptionStatus.REQUESTED,
                localSubscription.getSelector(),
                localSubscription.getConsumerCommonName(),
                localSubscription.getLocalConnections().stream().map(this::transformLocalConnectionImportApiToLocalConnection).collect(Collectors.toSet()),
                localSubscription.getLocalEndpoints().stream().map(this::transformLocalEndpointImportApiToLocalEndpoint).collect(Collectors.toSet()),
                localSubscription.getDescription()
        );
    }

    public LocalSubscriptionStatus transformLocalSubscriptionStatusImportApiToLocalSubscriptionStatus(LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi status) {
        switch (status) {
            case LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.CREATED -> {
                return LocalSubscriptionStatus.CREATED;
            }
            case LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.ILLEGAL -> {
                return LocalSubscriptionStatus.ILLEGAL;
            }
            case LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.TEAR_DOWN -> {
                return LocalSubscriptionStatus.TEAR_DOWN;
            }
            case LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.RESUBSCRIBE -> {
                return LocalSubscriptionStatus.RESUBSCRIBE;
            }
            case LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.ERROR -> {
                return LocalSubscriptionStatus.ERROR;
            }
            default -> {
                return LocalSubscriptionStatus.REQUESTED;
            }
        }
    }

    public LocalEndpoint transformLocalEndpointImportApiToLocalEndpoint(LocalEndpointImportApi endpoint) {
        return new LocalEndpoint(endpoint.getSource(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate());
    }

    public LocalConnection transformLocalConnectionImportApiToLocalConnection(LocalConnectionImportApi localConnection) {
        return new LocalConnection(localConnection.getSource(),
                localConnection.getDestination());
    }

    public Capability transformCapabilityImportApiToCapability(CapabilityImportApi capability) {
        return new Capability(
                capability.getUuid(),
                capabilityTransformer.applicationApiToApplication(capability.getApplication()),
                transformMetadataImportApiToMetadata(capability.getMetadata()),
                capability.getShards().stream().map(this::transformCapabilityShardImportApiToCapabilityShard).collect(Collectors.toList())
        );
    }

    public CapabilityStatus transformCapabilityStatusImportApiToCapabilityStatus(CapabilityImportApi.CapabilityStatusImportApi status) {
        switch (status) {
            case CapabilityImportApi.CapabilityStatusImportApi.CREATED -> {
                return CapabilityStatus.CREATED;
            }
            case CapabilityImportApi.CapabilityStatusImportApi.TEAR_DOWN -> {
                return CapabilityStatus.TEAR_DOWN;
            }
            default -> {
                return CapabilityStatus.REQUESTED;
            }
        }
    }

    public Metadata transformMetadataImportApiToMetadata(MetadataImportApi metadata) {
        return new Metadata(metadata.getInfoUrl(),
                metadata.getShardCount(),
                transformRedirectStatusImportApiToRedirectStatus(metadata.getRedirectPolicy()),
                metadata.getMaxBandwidth(),
                metadata.getMaxMessageRate(),
                metadata.getRepetitionInterval());
    }

    public RedirectStatus transformRedirectStatusImportApiToRedirectStatus(MetadataImportApi.RedirectStatusImportApi redirectStatus) {
        switch (redirectStatus) {
            case MetadataImportApi.RedirectStatusImportApi.MANDATORY -> {
                return RedirectStatus.MANDATORY;
            }
            case MetadataImportApi.RedirectStatusImportApi.NOT_AVAILABLE -> {
                return RedirectStatus.NOT_AVAILABLE;
            }
            default -> {
                return RedirectStatus.OPTIONAL;
            }
        }
    }

    public CapabilityShard transformCapabilityShardImportApiToCapabilityShard(CapabilityShardImportApi shard) {
        return new CapabilityShard(shard.getShardId(),
                shard.getExchangeName(),
                shard.getSelector());
    }

    public LocalDelivery transformDeliveryImportApiToLocalDelivery(DeliveryImportApi delivery) {
        return new LocalDelivery(
                delivery.getUuid(),
                delivery.getEndpoints().stream().map(this::transformLocalDeliveryEndpointImportApiToLocalDeliveryEndpoint).collect(Collectors.toSet()),
                delivery.getSelector(),
                LocalDeliveryStatus.REQUESTED,
                delivery.getDescription(),
                delivery.getDlqueue()
        );
    }

    public LocalDeliveryEndpoint transformLocalDeliveryEndpointImportApiToLocalDeliveryEndpoint(DeliveryEndpointImportApi endpoint) {
        return new LocalDeliveryEndpoint(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getTarget(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate(),
                endpoint.getDlqName()
        );
    }

    public LocalDeliveryStatus transformLocalDeliveryStatusImportApiToLocalDeliveryStatus(DeliveryImportApi.DeliveryStatusImportApi status) {
        switch (status) {
            case DeliveryImportApi.DeliveryStatusImportApi.CREATED -> {
                return LocalDeliveryStatus.CREATED;
            }
            case DeliveryImportApi.DeliveryStatusImportApi.ILLEGAL -> {
                return LocalDeliveryStatus.ILLEGAL;
            }
            case DeliveryImportApi.DeliveryStatusImportApi.NOT_VALID -> {
                return LocalDeliveryStatus.NOT_VALID;
            }
            case DeliveryImportApi.DeliveryStatusImportApi.NO_OVERLAP -> {
                return LocalDeliveryStatus.NO_OVERLAP;
            }
            case DeliveryImportApi.DeliveryStatusImportApi.ERROR -> {
                return LocalDeliveryStatus.ERROR;
            }
            default -> {
                return LocalDeliveryStatus.REQUESTED;
            }
        }
    }

    public Neighbour transformNeighbourImportApiToNeighbour(NeighbourImportApi neighbour) {
        return new Neighbour(neighbour.getName(),
                transformNeighbourCapabilitiesImportApiToNeighbourCapabilities(neighbour.getCapabilities()),
                new NeighbourSubscriptionRequest(neighbour.getNeighbourSubscriptions().stream().map(this::transformNeighbourSubscriptionImportApiToNeighbourSubscription).collect(Collectors.toSet())),
                new SubscriptionRequest(neighbour.getOurSubscriptions().stream().map(this::transformSubscriptionImportApiToSubscription).collect(Collectors.toSet())),
                neighbour.getControlChannelPort()
        );
    }

    public NeighbourCapabilities transformNeighbourCapabilitiesImportApiToNeighbourCapabilities(NeighbourCapabilitiesImportApi capabilities) {
        NeighbourCapabilities newCapabilities =  new NeighbourCapabilities(
                transformCapabilitiesStatusImportApiToCapabilitiesStatus(capabilities.getStatus()),
                capabilities.getCapabilities().stream().map(this::transformNeighbourCapabilityImportApiToNeighbourCapability).collect(Collectors.toSet()),
                convertLongToLocalDateTime(capabilities.getLastUpdated())
        );
        newCapabilities.setLastCapabilityExchange(convertLongToLocalDateTime(capabilities.getLastCapabilityExchange()));
        return newCapabilities;
    }

    public NeighbourCapability transformNeighbourCapabilityImportApiToNeighbourCapability(NeighbourCapabilityImportApi neighbourCapability) {
        return new NeighbourCapability(
                capabilityTransformer.applicationApiToApplication(neighbourCapability.getApplication()),
                transformMetadataImportApiToMetadata(neighbourCapability.getMetadata())
        );
    }

    public CapabilitiesStatus transformCapabilitiesStatusImportApiToCapabilitiesStatus(NeighbourCapabilitiesImportApi.CapabilitiesStatusImportApi status) {
        switch (status) {
            case NeighbourCapabilitiesImportApi.CapabilitiesStatusImportApi.KNOWN -> {
                return CapabilitiesStatus.KNOWN;
            }
            case NeighbourCapabilitiesImportApi.CapabilitiesStatusImportApi.FAILED -> {
                return CapabilitiesStatus.FAILED;
            }
            default -> {
                return CapabilitiesStatus.UNKNOWN;
            }
        }
    }

    public NeighbourSubscription transformNeighbourSubscriptionImportApiToNeighbourSubscription(NeighbourSubscriptionImportApi neighbourSubscription) {
        NeighbourSubscription neighbourSubscription1 = new NeighbourSubscription(
                neighbourSubscription.getUuid(), transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(neighbourSubscription.getStatus()),
                neighbourSubscription.getSelector(),
                neighbourSubscription.getPath(),
                neighbourSubscription.getConsumerCommonName(),
                neighbourSubscription.getEndpoints().stream().map(this::transformNeighbourEndpointImportApiToNeighbourEndpoint).collect(Collectors.toSet())
        );
        neighbourSubscription1.setUuid(neighbourSubscription.getUuid());
        return neighbourSubscription1;
    }

    public NeighbourSubscriptionStatus transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi status) {
        switch (status) {
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.ACCEPTED -> {
                return NeighbourSubscriptionStatus.ACCEPTED;
            }
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.CREATED -> {
                return NeighbourSubscriptionStatus.CREATED;
            }
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.ILLEGAL -> {
                return NeighbourSubscriptionStatus.ILLEGAL;
            }
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.NOT_VALID -> {
                return NeighbourSubscriptionStatus.NOT_VALID;
            }
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.NO_OVERLAP -> {
                return NeighbourSubscriptionStatus.NO_OVERLAP;
            }
            case NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi.TEAR_DOWN -> {
                return NeighbourSubscriptionStatus.TEAR_DOWN;
            }
            default -> {
                return NeighbourSubscriptionStatus.REQUESTED;
            }
        }
    }

    public NeighbourEndpoint transformNeighbourEndpointImportApiToNeighbourEndpoint(NeighbourEndpointImportApi endpoint) {
        return new NeighbourEndpoint(
                endpoint.getSource(),
                endpoint.getHost(),
                endpoint.getPort()
        );
    }

    public Subscription transformSubscriptionImportApiToSubscription(SubscriptionImportApi subscription) {
        return new Subscription(
                transformSubscriptionStatusImportApiToSubscriptionStatus(subscription.getStatus()),
                subscription.getSelector(),
                subscription.getPath(),
                subscription.getConsumerCommonName(),
                subscription.getEndpoints().stream().map(this::transformEndpointApiToEndpoint).collect(Collectors.toSet())
        );
    }

    public SubscriptionStatus transformSubscriptionStatusImportApiToSubscriptionStatus(SubscriptionImportApi.SubscriptionStatusImportApi status) {
        switch (status) {
            case SubscriptionImportApi.SubscriptionStatusImportApi.CREATED -> {
                return SubscriptionStatus.CREATED;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.ILLEGAL -> {
                return SubscriptionStatus.ILLEGAL;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.NO_OVERLAP -> {
                return SubscriptionStatus.NO_OVERLAP;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.GIVE_UP -> {
                return SubscriptionStatus.GIVE_UP;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.FAILED -> {
                return SubscriptionStatus.FAILED;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.TEAR_DOWN -> {
                return SubscriptionStatus.TEAR_DOWN;
            }
            case SubscriptionImportApi.SubscriptionStatusImportApi.RESUBSCRIBE -> {
                return SubscriptionStatus.RESUBSCRIBE;
            }
            default -> {
                return SubscriptionStatus.REQUESTED;
            }
        }
    }

    public Endpoint transformEndpointApiToEndpoint(EndpointImportApi endpoint) {
        return new Endpoint(
                endpoint.getSource(),
                endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getMaxMessageRate(),
                endpoint.getMaxBandwidth()
        );
    }

    public PrivateChannel transformPrivateChannelImportApiToPrivateChannel(PrivateChannelImportApi privateChannel) {
        return new PrivateChannel(
                privateChannel.getPeers().stream().map(this::transformPeerImportApiToPeer).collect(Collectors.toSet()),
                PrivateChannelStatus.REQUESTED,
                privateChannel.getDescription(),
                transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(privateChannel.getEndpoint()),
                privateChannel.getServiceProviderName()
        );
    }

    public Peer transformPeerImportApiToPeer(PeerImportApi peer) {
        return new Peer(
                peer.getUuid(),
                peer.getName(),
                PeerStatus.REQUESTED
        );
    }

    public PrivateChannelStatus transformPrivateChannelStatusImportApiToPrivateChannelStatus(PrivateChannelImportApi.PrivateChannelStatusImportApi status) {
        switch(status) {
            case PrivateChannelImportApi.PrivateChannelStatusImportApi.CREATED -> {
                return PrivateChannelStatus.CREATED;
            }
            case PrivateChannelImportApi.PrivateChannelStatusImportApi.TEAR_DOWN -> {
                return PrivateChannelStatus.TEAR_DOWN;
            }
            default -> {
                return PrivateChannelStatus.REQUESTED;
            }
        }
    }

    public PrivateChannelEndpoint transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(PrivateChannelEndpointImportApi endpoint) {
        return new PrivateChannelEndpoint(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getQueueName());
    }
}
