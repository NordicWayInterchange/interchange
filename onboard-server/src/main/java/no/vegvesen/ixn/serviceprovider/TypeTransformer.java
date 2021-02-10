package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TypeTransformer {
    public LocalSubscriptionStatusApi transformLocalSubscriptionStatusToLocalSubscriptionStatusApi(LocalSubscriptionStatus subscriptionStatus) {
        return LocalSubscriptionStatusApi.valueOf(subscriptionStatus.name());
    }

    public LocalSubscriptionApi transformLocalSubscriptionToLocalSubscriptionApi(LocalSubscription localSubscription) {
        return new LocalSubscriptionApi(localSubscription.getSub_id(),
                transformLocalSubscriptionStatusToLocalSubscriptionStatusApi(localSubscription.getStatus()),
                localSubscription.getSelector(),
                localSubscription.isCreateNewQueue());
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
