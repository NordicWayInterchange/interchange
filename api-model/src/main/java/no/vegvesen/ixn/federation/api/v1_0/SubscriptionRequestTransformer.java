package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.Interchange;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionRequestTransformer {

	public SubscriptionRequestApi interchangeToSubscriptionRequestApi(Interchange interchange){
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName(interchange.getName());
		subscriptionRequestApi.setSubscriptions(interchange.getSubscriptionRequest().getSubscriptions());

		return subscriptionRequestApi;
	}

	public Interchange subscriptionRequestApiToInterchange(SubscriptionRequestApi subscriptionRequestApi){
		Interchange interchange = new Interchange();
		interchange.setName(subscriptionRequestApi.getName());
		interchange.getSubscriptionRequest().setSubscriptions(subscriptionRequestApi.getSubscriptions());

		return interchange;
	}
}
