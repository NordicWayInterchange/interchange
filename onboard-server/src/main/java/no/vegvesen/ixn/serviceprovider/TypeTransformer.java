package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.LocalBroker;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class TypeTransformer {
    public LocalSubscriptionStatusApi transformLocalSubscriptionStatusToLocalSubscriptionStatusApi(LocalSubscriptionStatus subscriptionStatus) {
        return LocalSubscriptionStatusApi.valueOf(subscriptionStatus.name());
    }

    public Set<LocalBrokerApi> transformListOfLocalBrokersToListOfLocalBrokersApi(Set<LocalBroker> localBrokers) {
        Set<LocalBrokerApi> localBrokerApis = new HashSet<>();
        for(LocalBroker localBroker : localBrokers){
            LocalBrokerApi localBrokerApi = new LocalBrokerApi(
                    localBroker.getQueueName(),
                    localBroker.getMessageBrokerUrl()
            );
            localBrokerApis.add(localBrokerApi);
        }
        return localBrokerApis;
    }

    public LocalSubscriptionApi transformLocalSubscriptionToLocalSubscriptionApi(LocalSubscription localSubscription) {
        return new LocalSubscriptionApi(localSubscription.getSub_id(),
                transformLocalSubscriptionStatusToLocalSubscriptionStatusApi(localSubscription.getStatus()),
                localSubscription.getSelector(),
                localSubscription.isCreateNewQueue(),
                transformListOfLocalBrokersToListOfLocalBrokersApi(localSubscription.getLocalBrokers()));
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

    public LocalCapabilityList transformToLocalCapabilityList(Set<Capability> capabilities) {
		List<LocalCapability> idList = new LinkedList<>();
		for (Capability capability : capabilities) {
			idList.add(new LocalCapability(capability.getId(), capability.toApi()));
		}
		return new LocalCapabilityList(idList);
	}


	public LocalSubscription transformSelectorApiToLocalSubscription(String serviceProviderName, SelectorApi selectorApi) {
        return new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                selectorApi.getSelector(),
                serviceProviderName);
    }

    public AddSubscriptionsResponse transformLocalSubscriptionsToSubscriptionPostResponseApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        return new AddSubscriptionsResponse(serviceProviderName,tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(serviceProviderName,localSubscriptions));
    }

    private Set<LocalActorSubscription> tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        Set<LocalActorSubscription> result = new HashSet<>();
        for (LocalSubscription subscription : localSubscriptions) {
            String subscriptionId = subscription.getSub_id().toString();
            result.add(new LocalActorSubscription(
                    subscriptionId,
                    createSubscriptionPath(serviceProviderName, subscriptionId),
                    subscription.getSelector(),
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

    private long transformLocalDateTimeToEpochMili(LocalDateTime lastUpdated) {
        return lastUpdated == null ? 0 : ZonedDateTime.of(lastUpdated, ZoneId.systemDefault()).toInstant().toEpochMilli();
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
}
