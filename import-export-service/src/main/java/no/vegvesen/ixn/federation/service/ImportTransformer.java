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
                new Capabilities(serviceProvider.getCapabilities().stream().map(this::transformCapabilityImportApiToCapability).collect(Collectors.toSet())),
                serviceProvider.getSubscriptions().stream().map(this::transformLocalSubscriptionImportApiToLocalSubscription).collect(Collectors.toSet()),
                serviceProvider.getDeliveries().stream().map(this::transformDeliveryImportApiToLocalDelivery).collect(Collectors.toSet()),
                convertLongToLocalDateTime(serviceProvider.getSubscriptionsUpdated())
        );
    }

    public LocalSubscription transformLocalSubscriptionImportApiToLocalSubscription(LocalSubscriptionImportApi localSubscription) {
        LocalSubscription newLocalSubscription = new LocalSubscription(//transformLocalSubscriptionStatusImportApiToLocalSubscriptionStatus(localSubscription.getStatus()),
                LocalSubscriptionStatus.REQUESTED,
                localSubscription.getSelector(),
                localSubscription.getConsumerCommonName()
        );
        newLocalSubscription.setLocalEndpoints(localSubscription.getLocalEndpoints().stream().map(this::transformLocalEndpointImportApiToLocalEndpoint).collect(Collectors.toSet()));
        newLocalSubscription.setConnections(localSubscription.getLocalConnections().stream().map(this::transformLocalConnectionImportApiToLocalConnection).collect(Collectors.toSet()));
        return newLocalSubscription;
    }

    public LocalSubscriptionStatus transformLocalSubscriptionStatusImportApiToLocalSubscriptionStatus(LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi status) {
        switch (status) {
            case CREATED -> {
                return LocalSubscriptionStatus.CREATED;
            }
            case ILLEGAL -> {
                return LocalSubscriptionStatus.ILLEGAL;
            }
            case TEAR_DOWN -> {
                return LocalSubscriptionStatus.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
                return LocalSubscriptionStatus.RESUBSCRIBE;
            }
            case ERROR -> {
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
        Capability newCapability = new Capability(capabilityTransformer.applicationApiToApplication(capability.getApplication()),
                transformMetadataImportApiToMetadata(capability.getMetadata())
        );
        //newCapability.setStatus(transformCapabilityStatusImportApiToCapabilityStatus(capability.getStatus()));
        newCapability.setShards(capability.getShards().stream().map(this::transformCapabilityShardImportApiToCapabilityShard).collect(Collectors.toList()));
        return newCapability;
    }

    public CapabilityStatus transformCapabilityStatusImportApiToCapabilityStatus(CapabilityImportApi.CapabilityStatusImportApi status) {
        switch (status) {
            case CREATED -> {
                return CapabilityStatus.CREATED;
            }
            case TEAR_DOWN -> {
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
            case MANDATORY -> {
                return RedirectStatus.MANDATORY;
            }
            case NOT_AVAILABLE -> {
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
        return new LocalDelivery(delivery.getEndpoints().stream().map(this::transformLocalDeliveryEndpointImportApiToLocalDeliveryEndpoint).collect(Collectors.toSet()),
                delivery.getSelector(),
                //transformLocalDeliveryStatusImportApiToLocalDeliveryStatus(delivery.getStatus())
                LocalDeliveryStatus.REQUESTED
        );
    }

    public LocalDeliveryEndpoint transformLocalDeliveryEndpointImportApiToLocalDeliveryEndpoint(DeliveryEndpointImportApi endpoint) {
        return new LocalDeliveryEndpoint(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getTarget(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate()
        );
    }

    public LocalDeliveryStatus transformLocalDeliveryStatusImportApiToLocalDeliveryStatus(DeliveryImportApi.DeliveryStatusImportApi status) {
        switch (status) {
            case CREATED -> {
                return LocalDeliveryStatus.CREATED;
            }
            case ILLEGAL -> {
                return LocalDeliveryStatus.ILLEGAL;
            }
            case NOT_VALID -> {
                return LocalDeliveryStatus.NOT_VALID;
            }
            case NO_OVERLAP -> {
                return LocalDeliveryStatus.NO_OVERLAP;
            }
            case ERROR -> {
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
            case KNOWN -> {
                return CapabilitiesStatus.KNOWN;
            }
            case FAILED -> {
                return CapabilitiesStatus.FAILED;
            }
            default -> {
                return CapabilitiesStatus.UNKNOWN;
            }
        }
    }

    public NeighbourSubscription transformNeighbourSubscriptionImportApiToNeighbourSubscription(NeighbourSubscriptionImportApi neighbourSubscription) {
        return new NeighbourSubscription(
                transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(neighbourSubscription.getStatus()),
                neighbourSubscription.getSelector(),
                neighbourSubscription.getPath(),
                neighbourSubscription.getConsumerCommonName(),
                neighbourSubscription.getEndpoints().stream().map(this::transformNeighbourEndpointImportApiToNeighbourEndpoint).collect(Collectors.toSet())
        );
    }

    public NeighbourSubscriptionStatus transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi status) {
        switch (status) {
            case ACCEPTED -> {
                return NeighbourSubscriptionStatus.ACCEPTED;
            }
            case CREATED -> {
                return NeighbourSubscriptionStatus.CREATED;
            }
            case ILLEGAL -> {
                return NeighbourSubscriptionStatus.ILLEGAL;
            }
            case NOT_VALID -> {
                return NeighbourSubscriptionStatus.NOT_VALID;
            }
            case NO_OVERLAP -> {
                return NeighbourSubscriptionStatus.NO_OVERLAP;
            }
            case GIVE_UP -> {
                return NeighbourSubscriptionStatus.GIVE_UP;
            }
            case FAILED -> {
                return NeighbourSubscriptionStatus.FAILED;
            }
            case UNREACHABLE -> {
                return NeighbourSubscriptionStatus.UNREACHABLE;
            }
            case TEAR_DOWN -> {
                return NeighbourSubscriptionStatus.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
                return NeighbourSubscriptionStatus.RESUBSCRIBE;
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
            case ACCEPTED -> {
                return SubscriptionStatus.ACCEPTED;
            }
            case CREATED -> {
                return SubscriptionStatus.CREATED;
            }
            case ILLEGAL -> {
                return SubscriptionStatus.ILLEGAL;
            }
            case NO_OVERLAP -> {
                return SubscriptionStatus.NO_OVERLAP;
            }
            case GIVE_UP -> {
                return SubscriptionStatus.GIVE_UP;
            }
            case FAILED -> {
                return SubscriptionStatus.FAILED;
            }
            case REJECTED -> {
                return SubscriptionStatus.REJECTED;
            }
            case TEAR_DOWN -> {
                return SubscriptionStatus.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
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
        return new PrivateChannel(privateChannel.getPeerName(),
                //transformPrivateChannelStatusImportApiToPrivateChannelStatus(privateChannel.getStatus()),
                PrivateChannelStatus.REQUESTED,
                transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(privateChannel.getEndpoint()),
                privateChannel.getServiceProviderName()
        );
    }

    public PrivateChannelStatus transformPrivateChannelStatusImportApiToPrivateChannelStatus(PrivateChannelImportApi.PrivateChannelStatusImportApi status) {
        switch(status) {
            case CREATED -> {
                return PrivateChannelStatus.CREATED;
            }
            case TEAR_DOWN -> {
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
