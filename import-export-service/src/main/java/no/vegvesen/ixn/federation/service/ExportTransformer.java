package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.exportmodel.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.Metadata;

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
                serviceProvider.getSubscriptions().stream().map(this::transformLocalSubscriptionToLocalSubscriptionExportApi).collect(Collectors.toSet()),
                serviceProvider.getCapabilities().getCapabilities().stream().map(this::transformCapabilityToCapabilityExportApi).collect(Collectors.toSet()),
                serviceProvider.getDeliveries().stream().map(this::transformDeliveryToDeliveryExportApi).collect(Collectors.toSet())
        );
    }

    public LocalSubscriptionExportApi transformLocalSubscriptionToLocalSubscriptionExportApi(LocalSubscription localSubscription) {
        return new LocalSubscriptionExportApi(localSubscription.getSelector(),
                localSubscription.getConsumerCommonName(),
                transformLocalSubscriptionStatusToLocalSubscriptionStatusExportApi(localSubscription.getStatus()),
                localSubscription.getLocalEndpoints().stream().map(this::transformLocalEndpointToLocalEndpointExportApi).collect(Collectors.toSet()),
                localSubscription.getConnections().stream().map(this::transformLocalConnectionToLocalConnectionExportApi).collect(Collectors.toSet())
                );
    }

    public LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi transformLocalSubscriptionStatusToLocalSubscriptionStatusExportApi(LocalSubscriptionStatus status) {
        switch (status) {
            case CREATED -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.CREATED;
            }
            case ILLEGAL -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.ILLEGAL;
            }
            case TEAR_DOWN -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
                return LocalSubscriptionExportApi.LocalSubscriptionStatusExportApi.RESUBSCRIBE;
            }
            case ERROR -> {
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

    public CapabilityExportApi transformCapabilityToCapabilityExportApi(CapabilitySplit capability) {
        return new CapabilityExportApi(capability.getApplication().toApi(),
                transformMetadataToMetadataExportApi(capability.getMetadata()
                ));
    }

    public MetadataExportApi transformMetadataToMetadataExportApi(Metadata metadata) {
        return new MetadataExportApi(metadata.getShardCount(),
                metadata.getInfoUrl(),
                transformRedirectStatusToRedirectStatusExportApi(metadata.getRedirectPolicy()),
                metadata.getMaxBandwidth(),
                metadata.getMaxMessageRate(),
                metadata.getRepetitionInterval(),
                metadata.getShards().stream().map(this::transformCapabilityShardToCapabilityShardExportApi).collect(Collectors.toSet())
        );
    }

    public MetadataExportApi.RedirectStatusExportApi transformRedirectStatusToRedirectStatusExportApi(RedirectStatus status) {
        switch (status) {
            case MANDATORY -> {
                return MetadataExportApi.RedirectStatusExportApi.MANDATORY;
            }
            case NOT_AVAILABLE -> {
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
        return new DeliveryExportApi(delivery.getEndpoints().stream().map(this::transformDeliveryEndpointToDeliveryEndpointExportApi).collect(Collectors.toSet()),
                delivery.getSelector(),
                transformLocalDateTimeToEpochMili(delivery.getLastUpdatedTimestamp()),
                transformDeliveryStatusToDeliveryStatusExportApi(delivery.getStatus())
                );
    }

    public DeliveryEndpointExportApi transformDeliveryEndpointToDeliveryEndpointExportApi(LocalDeliveryEndpoint endpoint) {
        return new DeliveryEndpointExportApi(endpoint.getHost(),
                endpoint.getPort(),
                endpoint.getTarget(),
                endpoint.getMaxBandwidth(),
                endpoint.getMaxMessageRate()
        );
    }

    public DeliveryExportApi.DeliveryStatusExportApi transformDeliveryStatusToDeliveryStatusExportApi(LocalDeliveryStatus status) {
        switch(status) {
            case CREATED -> {
                return DeliveryExportApi.DeliveryStatusExportApi.CREATED;
            }
            case ILLEGAL -> {
                return DeliveryExportApi.DeliveryStatusExportApi.ILLEGAL;
            }
            case NOT_VALID -> {
                return DeliveryExportApi.DeliveryStatusExportApi.NOT_VALID;
            }
            case NO_OVERLAP -> {
                return DeliveryExportApi.DeliveryStatusExportApi.NO_OVERLAP;
            }
            case ERROR -> {
                return DeliveryExportApi.DeliveryStatusExportApi.ERROR;
            }
            default -> {
                return DeliveryExportApi.DeliveryStatusExportApi.REQUESTED;
            }
        }
    }

    public LocalConnectionExportApi transformLocalConnectionToLocalConnectionExportApi(LocalConnection localConnection) {
        return new LocalConnectionExportApi(localConnection.getSource(),
                localConnection.getDestination());
    }

    public NeighbourExportApi transformNeighbourToNeighbourExportApi(Neighbour neighbour) {
        return new NeighbourExportApi(neighbour.getName(),
                transformCapabilitiesToCapabilitiesExportApi(neighbour.getCapabilities()),
                neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().stream().map(this::transformNeighbourSubscriptionToNeighbourSubscriptionExportApi).collect(Collectors.toSet()),
                neighbour.getOurRequestedSubscriptions().getSubscriptions().stream().map(this::transformSubscriptionToSubscriptionExportApi).collect(Collectors.toSet()),
                transformConnectionToConnectionExportApi(neighbour.getControlConnection()),
                neighbour.getControlChannelPort()
        );
    }

    public CapabilitiesExportApi transformCapabilitiesToCapabilitiesExportApi(Capabilities capabilities) {
        return new CapabilitiesExportApi(transformLocalDateTimeToEpochMili(capabilities.getLastCapabilityExchange()),
                transformCapabilitiesStatusToCapabilitiesStatusExportApi(capabilities.getStatus()),
                capabilities.getCapabilities().stream().map(this::transformCapabilityToCapabilityExportApi).collect(Collectors.toSet()),
                transformLocalDateTimeToEpochMili(capabilities.getLastUpdated().get())
                );
    }

    public CapabilitiesExportApi.CapabilitiesStatusExportApi transformCapabilitiesStatusToCapabilitiesStatusExportApi(Capabilities.CapabilitiesStatus status) {
        switch (status) {
            case KNOWN -> {
                return CapabilitiesExportApi.CapabilitiesStatusExportApi.KNOWN;
            }
            case FAILED -> {
                return CapabilitiesExportApi.CapabilitiesStatusExportApi.FAILED;
            }
            default -> {
                return CapabilitiesExportApi.CapabilitiesStatusExportApi.UNKNOWN;
            }
        }
    }

    public NeighbourSubscriptionExportApi transformNeighbourSubscriptionToNeighbourSubscriptionExportApi(NeighbourSubscription neighbourSubscription) {
        return new NeighbourSubscriptionExportApi(transformNeighbourSubscriptionStatusToNeighbourSubscriptionStatusExportApi(neighbourSubscription.getSubscriptionStatus()),
                neighbourSubscription.getSelector(),
                neighbourSubscription.getPath(),
                neighbourSubscription.getConsumerCommonName(),
                neighbourSubscription.getEndpoints().stream().map(this::transformNeighbourEndpointToNeighbourEndpointExportApi).collect(Collectors.toSet())
        );
    }

    public NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi transformNeighbourSubscriptionStatusToNeighbourSubscriptionStatusExportApi(NeighbourSubscriptionStatus status) {
        switch (status) {
            case ACCEPTED -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.ACCEPTED;
            }
            case CREATED -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.CREATED;
            }
            case ILLEGAL -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.ILLEGAL;
            }
            case NOT_VALID -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.NOT_VALID;
            }
            case NO_OVERLAP -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.NO_OVERLAP;
            }
            case GIVE_UP -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.GIVE_UP;
            }
            case FAILED -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.FAILED;
            }
            case UNREACHABLE -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.UNREACHABLE;
            }
            case TEAR_DOWN -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
                return NeighbourSubscriptionExportApi.NeighbourSubscriptionStatusExportApi.RESUBSCRIBE;
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
                subscription.getLastUpdatedTimestamp(),
                subscription.getEndpoints().stream().map(this::transformEndpointToEndpointExportApi).collect(Collectors.toSet())
                );
    }

    public SubscriptionExportApi.SubscriptionStatusExportApi transformSubscriptionStatusToSubscriptionStatusExportApi(SubscriptionStatus status) {
        switch (status) {
            case ACCEPTED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.ACCEPTED;
            }
            case CREATED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.CREATED;
            }
            case ILLEGAL -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.ILLEGAL;
            }
            case NO_OVERLAP -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.NO_OVERLAP;
            }
            case  GIVE_UP -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.GIVE_UP;
            }
            case FAILED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.FAILED;
            }
            case REJECTED -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.REJECTED;
            }
            case TEAR_DOWN -> {
                return SubscriptionExportApi.SubscriptionStatusExportApi.TEAR_DOWN;
            }
            case RESUBSCRIBE -> {
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

    public ConnectionExportApi transformConnectionToConnectionExportApi(Connection connection) {
        return new ConnectionExportApi(transformLocalDateTimeToEpochMili(connection.getBackoffStartTime()),
                connection.getBackoffAttempts(),
                transformConnectionStatusToConnectionStatusExportApi(connection.getConnectionStatus()),
                transformLocalDateTimeToEpochMili(connection.getLastFailedConnectionAttempt()));
    }

    public ConnectionExportApi.ConnectionStatusExportApi transformConnectionStatusToConnectionStatusExportApi(ConnectionStatus status) {
        switch (status) {
            case FAILED -> {
                return ConnectionExportApi.ConnectionStatusExportApi.FAILED;
            }
            case UNREACHABLE -> {
                return ConnectionExportApi.ConnectionStatusExportApi.UNREACHABLE;
            }
            default -> {
                return ConnectionExportApi.ConnectionStatusExportApi.CONNECTED;
            }
        }
    }

    public PrivateChannelExportApi transformPrivateChannelToPrivateChannelExportApi(PrivateChannel privateChannel) {
        return new PrivateChannelExportApi(privateChannel.getServiceProviderName(),
                privateChannel.getPeerName(),
                transformPrivateChannelStatusToPrivateChannelStatusExportApi(privateChannel.getStatus()),
                transformPrivateChannelEndpointToPrivateChannelEndpointExportApi(privateChannel.getEndpoint())
        );
    }

    public PrivateChannelExportApi.PrivateChannelStatusExportApi transformPrivateChannelStatusToPrivateChannelStatusExportApi(PrivateChannelStatus status) {
        switch (status) {
            case CREATED -> {
                return PrivateChannelExportApi.PrivateChannelStatusExportApi.CREATED;
            }
            case TEAR_DOWN -> {
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
