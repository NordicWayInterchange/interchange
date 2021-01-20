package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.transformer.DataTypeTransformer;
import no.vegvesen.ixn.serviceprovider.model.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TypeTransformer {

    private DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

	public LocalDataTypeList transformToDataTypeIdList(Set<DataType> dataTypes) {
		List<LocalDataType> idDataTypes = new LinkedList<>();
		for (DataType dataType : dataTypes) {
			idDataTypes.add(transformToDataTypeApiId(dataType));
		}
		return new LocalDataTypeList(idDataTypes);
	}

    public LocalDataType transformToDataTypeApiId(DataType dataType) {
        return new LocalDataType(dataType.getData_id(), dataTypeTransformer.dataTypeToApi(dataType));
    }

    public LocalSubscriptionStatusApi transformLocalSubscriptionStatusToLoccalSubscriptionStatusApi(LocalSubscriptionStatus subscriptionStatus) {
        return LocalSubscriptionStatusApi.valueOf(subscriptionStatus.name());
    }

    public LocalSubscriptionApi transformLocalSubecriptionToLocalSubscriptionApi(LocalSubscription localSubscription) {
        return new LocalSubscriptionApi(localSubscription.getSub_id(),
                transformLocalSubscriptionStatusToLoccalSubscriptionStatusApi(localSubscription.getStatus()),
                localSubscription.getSelector());
    }

    public LocalSubscriptionListApi transformLocalSubscriptionListToLocalSubscriptionListApi(Set<LocalSubscription> subscriptions) {
	    List<LocalSubscriptionApi> subscriptionApis = new ArrayList<>();
	    for (LocalSubscription subscription : subscriptions) {
	        subscriptionApis.add(transformLocalSubecriptionToLocalSubscriptionApi(subscription));
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
}
