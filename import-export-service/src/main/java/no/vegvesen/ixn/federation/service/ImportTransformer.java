package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.api.v1_0.importmodel.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportTransformer {

    public CapabilityToCapabilityApiTransformer capabilityTransformer;

    public ServiceProvider transformServiceProviderImportApiToServiceProvider(ServiceProviderImportApi serviceProvider) {
        return new ServiceProvider(serviceProvider.getName(),
                transformCapabilitiesImportApiToCapabilities(serviceProvider.getCapabilities()),
                serviceProvider.getSubscriptions().stream().map(this::transformLocalSubscriptionImportApiToLocalSubscription).collect(Collectors.toSet()),
                serviceProvider.getDeliveries().stream().map(this::transformDeliveryImportApiToLocalDelivery).collect(Collectors.toSet()),
                convertLongToLocalDateTime(serviceProvider.getSubscriptionsUpdated())
        );
    }

    public LocalSubscription transformLocalSubscriptionImportApiToLocalSubscription(LocalSubscriptionImportApi localSubscription) {
        return new LocalSubscription();
    }

    public LocalDelivery transformDeliveryImportApiToLocalDelivery(DeliveryImportApi delivery) {
        return new LocalDelivery();
    }

    public Neighbour transformNeighbourImportApiToNeighbour(NeighbourImportApi neighbour) {
        return new Neighbour(neighbour.getName(),
                transformCapabilitiesImportApiToCapabilities(neighbour.getCapabilities()),
                new NeighbourSubscriptionRequest(neighbour.getNeighbourSubscriptions().stream().map(this::transformNeighbourSubscriptionImportApiToNeighbourSubscription).collect(Collectors.toSet())),
                new SubscriptionRequest(neighbour.getOurSubscriptions().stream().map(this::transformSubscriptionImportApiToSubscription).collect(Collectors.toSet())),
                transformConnectionImportApiToConnection(neighbour.getConnection()),
                neighbour.getControlChannelPort()
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

    public Capabilities transformCapabilitiesImportApiToCapabilities(CapabilitiesImportApi capabilities) {
        Capabilities newCapabilities =  new Capabilities(
                transformCapabilitiesStatusImportApiToCapabilitiesStatus(capabilities.getStatus()),
                capabilities.getCapabilities().stream().map(this::transformCapabilityImportApiToCapability).collect(Collectors.toSet()),
                convertLongToLocalDateTime(capabilities.getLastUpdated())
        );
        newCapabilities.setLastCapabilityExchange(convertLongToLocalDateTime(capabilities.getLastCapabilityExchange()));
        return newCapabilities;
    }

    public Capabilities.CapabilitiesStatus transformCapabilitiesStatusImportApiToCapabilitiesStatus(CapabilitiesImportApi.CapabilitiesStatusImportApi status) {
        switch (status) {
            case KNOWN -> {
                return Capabilities.CapabilitiesStatus.KNOWN;
            }
            case FAILED -> {
                return Capabilities.CapabilitiesStatus.FAILED;
            }
            default -> {
                return Capabilities.CapabilitiesStatus.UNKNOWN;
            }
        }
    }

    public CapabilitySplit transformCapabilityImportApiToCapability(CapabilityImportApi capability) {
        return new CapabilitySplit(
                capabilityTransformer.applicationApiToApplication(capability.getApplication()),
                transformMetadataImportApiToMetadata(capability.getMetadata())
        );
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

    public LocalDateTime convertLongToLocalDateTime(long importTimestamp) {
        Instant instant = Instant.ofEpochMilli(importTimestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.of("UTC"));
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

    public Connection transformConnectionImportApiToConnection(ConnectionImportApi connection) {
        Connection newConnection = new Connection();
        newConnection.setBackoffStart(convertLongToLocalDateTime(connection.getBackoffStart()));
        newConnection.setBackoffAttempts(connection.getBackoffAttempts());
        newConnection.setConnectionStatus(transformConnectionStatusImportApiToConnectionStatus(connection.getStatus()));
        newConnection.setLastFailedConnectionAttempt(convertLongToLocalDateTime(connection.getLastFailedConnectionAttempt()));
        return newConnection;
    }

    public ConnectionStatus transformConnectionStatusImportApiToConnectionStatus(ConnectionImportApi.ConnectionStatusImportApi status) {
        switch (status) {
            case FAILED -> {
                return ConnectionStatus.FAILED;
            }
            case UNREACHABLE -> {
                return ConnectionStatus.UNREACHABLE;
            }
            default -> {
                return ConnectionStatus.CONNECTED;
            }
        }
    }

    public Set<PrivateChannel> transformPrivateChannelImportApisToPrivateChannels(Set<PrivateChannelImportApi> privateChannels) {
        Set<PrivateChannel> importPrivateChannels = new HashSet<>();
        for (PrivateChannelImportApi privateChannel : privateChannels) {
            importPrivateChannels.add(new PrivateChannel(
                    privateChannel.getPeerName(),
                    transformPrivateChannelStatusImportApiToPrivateChannelStatus(privateChannel.getStatus()),
                    transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(privateChannel.getEndpoint()),
                    privateChannel.getServiceProviderName()
            ));
        }
        return importPrivateChannels;
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
