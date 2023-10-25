package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.napcore.model.*;
import no.vegvesen.ixn.federation.model.LocalEndpoint;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.serviceprovider.model.AddPrivateChannelsResponse;
import no.vegvesen.ixn.serviceprovider.model.PrivateChannelApi;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    public LocalSubscription transformNapSubscriptionToLocalSubscription(SubscriptionRequest subscription, String nodeName) {
        return new LocalSubscription(subscription.getSelector(), nodeName);
    }

    public Subscription transformLocalSubscriptionToNapSubscription(LocalSubscription localSubscription) {
        LocalDateTime lastUpdated = localSubscription.getLastUpdated();
        Long epochSecond = null;
        if (lastUpdated != null) {
            epochSecond = lastUpdated.atZone(ZoneId.systemDefault()).toEpochSecond();
        }
        Subscription subscription = new Subscription(
                localSubscription.getId(),
                transformLocalSubscriptionStatusToNapSubscriptionStatus(localSubscription.getStatus()),
                localSubscription.getSelector(),
                transformLocalEndpointsToNapSubscriptionEndpoints(localSubscription.getLocalEndpoints()),
                epochSecond

        );
        return subscription;
    }

    public List<Subscription> transformLocalSubscriptionsToNapSubscriptions(Set<LocalSubscription> localSubscriptions) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (LocalSubscription localSubscription : localSubscriptions) {
            subscriptions.add(transformLocalSubscriptionToNapSubscription(localSubscription));
        }
        return subscriptions;
    }

    public Set<SubscriptionEndpoint> transformLocalEndpointsToNapSubscriptionEndpoints(Set<LocalEndpoint> localEndpoints) {
        Set<SubscriptionEndpoint> endpoints = new HashSet<>();
        for (LocalEndpoint endpoint : localEndpoints) {
            endpoints.add( new SubscriptionEndpoint(
                    endpoint.getHost(),
                    endpoint.getPort(),
                    endpoint.getSource(),
                    endpoint.getMaxBandwidth(),
                    endpoint.getMaxMessageRate()
            ));
        }
        return endpoints;
    }

    public SubscriptionStatus transformLocalSubscriptionStatusToNapSubscriptionStatus(LocalSubscriptionStatus status) {
        switch(status) {
            case REQUESTED:
                return SubscriptionStatus.REQUESTED;
            case CREATED:
                return SubscriptionStatus.CREATED;
            case TEAR_DOWN:
                return SubscriptionStatus.NOT_VALID;
            default:
                return SubscriptionStatus.ILLEGAL;
        }
    }

    public List<Capability> transformCapabilitiesToGetMatchingCapabilitiesResponse(Set<CapabilitySplit> capabilities) {
        List<Capability> matchingCapabilities = new ArrayList<>();
        for (CapabilitySplit capability : capabilities) {
            matchingCapabilities.add(new Capability(
                    capability.getApplication().toApi(),
                    capability.getMetadata().toApi()
            ));
        }
        return matchingCapabilities;
    }

}
