package net.nordicway.interchange;

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

    public Neighbour transformNeighbourImportApiToNeighbour(NeighbourImportApi neighbourImportApi) {
        return new Neighbour(neighbourImportApi.getName(),
                transformCapabilitiesImportApiToCapabilities(neighbourImportApi.getCapabilities()),
                transformNeighbourSubscriptionsImportApiToNeighbourSubscriptionRequest(neighbourImportApi.getNeighbourSubscriptions()),
                transformSubscriptionImportApisToSubscriptionRequest(neighbourImportApi.getOurSubscriptions()),
                transformConnectionImportApiToConnection(neighbourImportApi.getConnection()),
                neighbourImportApi.getControlChannelPort()
        );
    }

    public SubscriptionRequest transformSubscriptionImportApisToSubscriptionRequest(Set<SubscriptionImportApi> subscriptionImportApis) {
        Set<Subscription> importSubscriptions = new HashSet<>();
        for (SubscriptionImportApi subscriptionImportApi : subscriptionImportApis) {
            importSubscriptions.add(new Subscription(
                    transformSubscriptionStatusImportApiToSubscriptionStatus(subscriptionImportApi.getStatus()),
                    subscriptionImportApi.getSelector(),
                    subscriptionImportApi.getPath(),
                    subscriptionImportApi.getConsumerCommonName(),
                    transformEndpointImportApisToEndpoints(subscriptionImportApi.getEndpoints())
            ));
        }
        return new SubscriptionRequest(importSubscriptions);
    }

    public SubscriptionStatus transformSubscriptionStatusImportApiToSubscriptionStatus(SubscriptionImportApi.SubscriptionStatusImportApi statusApi) {
        switch (statusApi) {
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

    public Set<Endpoint> transformEndpointImportApisToEndpoints(Set<EndpointImportApi> endpointApis) {
        Set<Endpoint> importEndpoints = new HashSet<>();
        for (EndpointImportApi endpointImportApi : endpointApis) {
            importEndpoints.add(new Endpoint(
                    endpointImportApi.getSource(),
                    endpointImportApi.getHost(),
                    endpointImportApi.getPort(),
                    endpointImportApi.getMaxMessageRate(),
                    endpointImportApi.getMaxBandwidth()
            ));
        }
        return importEndpoints;
    }

    public Capabilities transformCapabilitiesImportApiToCapabilities(CapabilitiesImportApi capabilitiesImportApi) {
        Capabilities capabilities =  new Capabilities(
                transformCapabilitiesStatusImportApiToCapabilitiesStatus(capabilitiesImportApi.getStatus()),
                transformCapabilityImportApisToCapabilitySplits(capabilitiesImportApi.getCapabilities()),
                convertLongToLocalDateTime(capabilitiesImportApi.getLastUpdated())
        );
        capabilities.setLastCapabilityExchange(convertLongToLocalDateTime(capabilitiesImportApi.getLastCapabilityExchange()));
        return capabilities;
    }

    public Capabilities.CapabilitiesStatus transformCapabilitiesStatusImportApiToCapabilitiesStatus(CapabilitiesImportApi.CapabilitiesStatusImportApi statusImportApi) {
        switch (statusImportApi) {
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

    public Set<CapabilitySplit> transformCapabilityImportApisToCapabilitySplits(Set<CapabilityImportApi> capabilitySplitApis) {
        Set<CapabilitySplit> importCapabilities = new HashSet<>();
        for (CapabilityImportApi capabilityImportApi : capabilitySplitApis) {
            importCapabilities.add(new CapabilitySplit(
                    capabilityTransformer.applicationApiToApplication(capabilityImportApi.getApplication()),
                    transformMetadataImportApiToMetadata(capabilityImportApi.getMetadata())
            ));
        }
        return importCapabilities;
    }

    public Metadata transformMetadataImportApiToMetadata(MetadataImportApi metadataImportApi) {
        return new Metadata(metadataImportApi.getInfoUrl(),
                metadataImportApi.getShardCount(),
                transformRedirectStatusImportApiToRedirectStatus(metadataImportApi.getRedirectPolicy()),
                metadataImportApi.getMaxBandwidth(),
                metadataImportApi.getMaxMessageRate(),
                metadataImportApi.getRepetitionInterval());
    }

    public RedirectStatus transformRedirectStatusImportApiToRedirectStatus(MetadataImportApi.RedirectStatusImportApi redirectStatusImportApi) {
        switch (redirectStatusImportApi) {
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

    public NeighbourSubscriptionRequest transformNeighbourSubscriptionsImportApiToNeighbourSubscriptionRequest(Set<NeighbourSubscriptionImportApi> neighbourSubscriptionImportApis) {
        return new NeighbourSubscriptionRequest(neighbourSubscriptionImportApis.stream().map(this::transformNeighbourSubscriptionImportApiToNeighbourSubscription).collect(Collectors.toSet()));
    }

    public NeighbourSubscription transformNeighbourSubscriptionImportApiToNeighbourSubscription(NeighbourSubscriptionImportApi neighbourSubscriptionImportApi) {
        return new NeighbourSubscription(
                transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(neighbourSubscriptionImportApi.getStatus()),
                neighbourSubscriptionImportApi.getSelector(),
                neighbourSubscriptionImportApi.getPath(),
                neighbourSubscriptionImportApi.getConsumerCommonName(),
                transformNeighbourEndpointImportApiToNeighbourEndpoint(neighbourSubscriptionImportApi.getEndpoints())
                );
    }

    public NeighbourSubscriptionStatus transformNeighbourSubscriptionStatusImportApiToNeighbourSubscriptionStatus(NeighbourSubscriptionImportApi.NeighbourSubscriptionStatusImportApi statusImportApi) {
        switch (statusImportApi) {
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

    public Set<NeighbourEndpoint> transformNeighbourEndpointImportApiToNeighbourEndpoint(Set<NeighbourEndpointImportApi> endpointImportApis) {
        Set<NeighbourEndpoint> neighbourEndpoints = new HashSet<>();
        for (NeighbourEndpointImportApi endpointImportApi : endpointImportApis) {
            neighbourEndpoints.add(new NeighbourEndpoint(
                    endpointImportApi.getSource(),
                    endpointImportApi.getHost(),
                    endpointImportApi.getPort()
            ));
        }
        return neighbourEndpoints;
    }

    public Connection transformConnectionImportApiToConnection(ConnectionImportApi connectionImportApi) {
        Connection connection = new Connection();
        connection.setBackoffStart(convertLongToLocalDateTime(connectionImportApi.getBackoffStart()));
        connection.setBackoffAttempts(connection.getBackoffAttempts());
        connection.setConnectionStatus(transformConnectionStatusImportApiToConnectionStatus(connectionImportApi.getStatus()));
        connection.setLastFailedConnectionAttempt(convertLongToLocalDateTime(connectionImportApi.getLastFailedConnectionAttempt()));
        return connection;
    }

    public ConnectionStatus transformConnectionStatusImportApiToConnectionStatus(ConnectionImportApi.ConnectionStatusImportApi statusImportApi) {
        switch (statusImportApi) {
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

    public Set<PrivateChannel> transformPrivateChannelImportApisToPrivateChannels(Set<PrivateChannelImportApi> privateChannelImportApis) {
        Set<PrivateChannel> importPrivateChannels = new HashSet<>();
        for (PrivateChannelImportApi privateChannelImportApi : privateChannelImportApis) {
            importPrivateChannels.add(new PrivateChannel(
                    privateChannelImportApi.getPeerName(),
                    transformPrivateChannelStatusImportApiToPrivateChannelStatus(privateChannelImportApi.getStatus()),
                    transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(privateChannelImportApi.getEndpoint()),
                    privateChannelImportApi.getServiceProviderName()
            ));
        }
        return importPrivateChannels;
    }

    public PrivateChannelStatus transformPrivateChannelStatusImportApiToPrivateChannelStatus(PrivateChannelImportApi.PrivateChannelStatusImportApi statusImportApi) {
        switch(statusImportApi) {
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

    public PrivateChannelEndpoint transformPrivateChannelEndpointImportApiToPrivateChannelEndpoint(PrivateChannelEndpointImportApi endpointImportApi) {
        return new PrivateChannelEndpoint(endpointImportApi.getHost(),
                endpointImportApi.getPort(),
                endpointImportApi.getQueueName());
    }
}
