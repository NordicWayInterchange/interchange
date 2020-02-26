package no.vegvesen.ixn.serviceprovider.model;

import java.util.LinkedList;
import java.util.List;

public class LocalSubscriptionsApi {
	List<DataTypeApiId> subscriptions = new LinkedList<>();

	public LocalSubscriptionsApi() {
	}

	public LocalSubscriptionsApi(List<DataTypeApiId> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public List<DataTypeApiId> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(List<DataTypeApiId> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
