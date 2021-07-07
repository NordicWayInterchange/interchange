package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.LocalBroker;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.serviceprovider.model.*;

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


	public LocalSubscription transformSelectorApiToLocalSubscription(String serviceProviderName, SelectorApi selectorApi) {
        return new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
                selectorApi.getSelector(),
                selectorApi.isCreateNewQueue(),
                serviceProviderName);
    }
}
