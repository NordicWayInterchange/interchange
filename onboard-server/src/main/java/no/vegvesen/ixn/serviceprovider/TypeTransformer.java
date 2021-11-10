package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {

    public static AddCapabilitiesResponse addCapabilitiesResponse(String serviceProviderName, Set<Capability> capabilities) {
        return new AddCapabilitiesResponse(
               serviceProviderName,
               capabilitySetToLocalActorCapability(serviceProviderName,capabilities)
        );
    }

    private static Set<LocalActorCapability> capabilitySetToLocalActorCapability(String serviceProviderName, Set<Capability> capabilities) {
        Set<LocalActorCapability> result = new HashSet<>();
        for (Capability capability : capabilities) {
            result.add(capabilityToLocalCapability(serviceProviderName,capability));
        }
        return result;
    }

    private static LocalActorCapability capabilityToLocalCapability(String serviceProviderName, Capability capability) {
        String id = capability.getId().toString();
        return new LocalActorCapability(
                id,
                createCapabilitiesPath(serviceProviderName, id),
                capability.toApi());
    }


    public ListSubscriptionsResponse transformLocalSubscriptionsToListSubscriptionResponse(String name, Set<LocalSubscription> subscriptions) {
        return new ListSubscriptionsResponse(name,transformLocalSubscriptionsToLocalActorSubscription(name,subscriptions));

    }

    private Set<LocalActorSubscription> transformLocalSubscriptionsToLocalActorSubscription(String name, Set<LocalSubscription> subscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : subscriptions) {
            String sub_id = subscription.getSub_id() == null ? null : subscription.getSub_id().toString();
            result.add(new LocalActorSubscription(
                    sub_id,
                    createSubscriptionPath(name,sub_id),
                    subscription.getSelector(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStaturToLocalActorSubscriptionStatusApi(subscription.getStatus())));
        }
        return result;
    }


    public LocalSubscription transformAddSubscriptionToLocalSubscription(String serviceProviderName, AddSubscription addSubscription) {
        return new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                addSubscription.getSelector().getSelector(),
                addSubscription.isCreateNewQueue(),
                serviceProviderName);
    }

    public AddSubscriptionsResponse transformLocalSubscriptionsToSubscriptionPostResponseApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        return new AddSubscriptionsResponse(serviceProviderName,
                tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(
                        serviceProviderName,
                        localSubscriptions)
        );
    }

    private Set<LocalActorSubscription> tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : localSubscriptions) {
            String subscriptionId = subscription.getSub_id().toString();
            result.add(new LocalActorSubscription(
                    subscriptionId,
                    createSubscriptionPath(serviceProviderName, subscriptionId),
                    subscription.getSelector(),
                    subscription.isCreateNewQueue(),
                    subscription.getQueueConsumerUser(),
                    transformLocalDateTimeToEpochMili(subscription.getLastUpdated()),
                    transformLocalSubscriptionStaturToLocalActorSubscriptionStatusApi(subscription.getStatus())
                    )
            );
        }
        return result;
    }

    private String createSubscriptionPath(String serviceProviderName, String subscriptionId) {
        return String.format("/%s/subscriptions/%s", serviceProviderName, subscriptionId);
    }

    private static String createCapabilitiesPath(String serviceProviderName, String capabilityId) {
        return String.format("/%s/capabilities/%s", serviceProviderName, capabilityId);
    }

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public GetSubscriptionResponse transformLocalSubscriptionToGetSubscriptionResponse(String serviceProviderName, LocalSubscription localSubscription) {
        return new GetSubscriptionResponse(
                localSubscription.getSub_id().toString(),
                createSubscriptionPath(serviceProviderName,localSubscription.getSub_id().toString()),
                localSubscription.getSelector(),
                transformLocalDateTimeToEpochMili(localSubscription.getLastUpdated()),
                transformLocalSubscriptionStaturToLocalActorSubscriptionStatusApi(localSubscription.getStatus()),
                transformLocalBrokersToEndpoints(localSubscription.getLocalBrokers())
        );
    }

    private Set<LocalEndpoint> transformLocalBrokersToEndpoints(Set<LocalBroker> localBrokers) {
        Set<LocalEndpoint> result = new HashSet<>();
        for (LocalBroker broker : localBrokers) {
            List<String> hostAndPort = makeHostAndPortOfUrl(broker.getMessageBrokerUrl());
            result.add(new LocalEndpoint(hostAndPort.get(0),
                    Integer.parseInt(hostAndPort.get(1)),
                    broker.getQueueName(),
                    broker.getMaxBandwidth(),
                    broker.getMaxMessageRate()));
        }
        return result;
    }

    private LocalActorSubscriptionStatusApi transformLocalSubscriptionStaturToLocalActorSubscriptionStatusApi(LocalSubscriptionStatus status) {
        switch (status) {
            case REQUESTED:
                return LocalActorSubscriptionStatusApi.REQUESTED;
            case CREATED:
                return LocalActorSubscriptionStatusApi.CREATED;
            case TEAR_DOWN:
                return LocalActorSubscriptionStatusApi.NOT_VALID;
            default:
                return LocalActorSubscriptionStatusApi.ILLEGAL;
        }
    }

    public Set<Capability> capabilitiesRequestToCapabilities(CapabilityToCapabilityApiTransformer capabilityApiTransformer, AddCapabilitiesRequest capabilitiesRequest) {
        Set<Capability> capabilities = new HashSet<>();
        for (CapabilityApi capabilityApi : capabilitiesRequest.getCapabilities()) {
            Capability capability = capabilityApiTransformer.capabilityApiToCapability(capabilityApi);
            capabilities.add(capability);

        }
        return capabilities;
    }

    public ListCapabilitiesResponse listCapabilitiesResponse(String serviceProviderName, Set<Capability> capabilities) {
        return new ListCapabilitiesResponse(
                serviceProviderName,
                capabilitySetToLocalActorCapability(serviceProviderName,capabilities)
        );
    }

    public GetCapabilityResponse getCapabilityResponse(String serviceProviderName, Capability capability) {
        String capabilityId = capability.getId().toString();
        return new GetCapabilityResponse(
                capabilityId,
                createCapabilitiesPath(serviceProviderName,capabilityId),
                capability.toApi()
        );
    }

    public static List<String> makeHostAndPortOfUrl(String url){
        URI uri = URI.create(url);
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1){
            port = 5671;
        }
        return new ArrayList<>(Arrays.asList(host, String.valueOf(port)));
    }
}
