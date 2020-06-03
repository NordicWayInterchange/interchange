package no.vegvesen.ixn.serviceprovider;

/*-
 * #%L
 * onboard-server
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
                dataTypeTransformer.dataTypeToApi(localSubscription.getDataType()));
    }

    public LocalSubscriptionListApi transformLocalSubscriptionListToLocalSubscriptionListApi(Set<LocalSubscription> subscriptions) {
	    List<LocalSubscriptionApi> subscriptionApis = new ArrayList<>();
	    for (LocalSubscription subscription : subscriptions) {
	        subscriptionApis.add(transformLocalSubecriptionToLocalSubscriptionApi(subscription));
        }
        return new LocalSubscriptionListApi(subscriptionApis);
    }
}
