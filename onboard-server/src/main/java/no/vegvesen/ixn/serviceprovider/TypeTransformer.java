package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.LocalBroker;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
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

    public LocalSubscriptionListApi transformLocalSubscriptionListToLocalSubscriptionListApi(Set<LocalSubscription> subscriptions) {
	    List<LocalSubscriptionApi> subscriptionApis = new ArrayList<>();
	    for (LocalSubscription subscription : subscriptions) {
	        subscriptionApis.add(transformLocalSubscriptionToLocalSubscriptionApi(subscription));
        }
        return new LocalSubscriptionListApi(subscriptionApis);
    }

	public LocalCapabilityList transformToLocalCapabilityList(Set<Capability> capabilities) {
		List<LocalCapability> idList = new LinkedList<>();
		for (Capability capability : capabilities) {
			idList.add(new LocalCapability(capability.getId(), capability.toApi()));
		}
		return new LocalCapabilityList(idList);
	}


	public LocalSubscription transformSelectorApiToLocalSubscription(SelectorApi selectorApi) {
        return new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                selectorApi.getSelector());
    }

    public SubscriptionsPostResponseApi transformLocalSubscriptionsToSubscriptionPostResponseApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        return new SubscriptionsPostResponseApi(serviceProviderName,tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(serviceProviderName,localSubscriptions));
    }

    private Set<SubscriptionsPostResponseSubscriptionApi> tranformLocalSubscriptionsToSubscriptionsPostResponseSubscriptionApi(String serviceProviderName, Set<LocalSubscription> localSubscriptions) {
        Set<SubscriptionsPostResponseSubscriptionApi> result = new HashSet<>();
        for (LocalSubscription subscription : localSubscriptions) {
            result.add(new SubscriptionsPostResponseSubscriptionApi(
                    subscription.getSub_id().toString(),
                    String.format("/%s/subscription/%s",serviceProviderName,subscription.getSub_id().toString()),
                    subscription.getSelector(),
                    ZonedDateTime.of(subscription.getLastUpdated(),ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    transformLocalSubscriptionStaturToLocalActorSubscriptionStatusApi(subscription.getStatus())
                    )
            );
        }
        return null;
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
