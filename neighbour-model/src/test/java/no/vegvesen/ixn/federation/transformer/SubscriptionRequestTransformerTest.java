package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeSubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionExchangeSubscriptionResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatusPollResponseApi;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionRequestTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	@Test
	public void emptySubscriptionsToSubscriptionExchangeRequest() {
		String name = "myNode";
		SubscriptionExchangeRequestApi requestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionExchangeRequestApi(name, Collections.emptySet());
		assertThat(requestApi.getName()).isEqualTo(name);
		assertThat(requestApi.getSubscriptions()).isEmpty();
		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionExchangeRequestApiToSubscriptionRequest(requestApi);
		assertThat(result.getSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptonsToSubscriptionExchangeRequest() {
		String name = "myNode";
		String selector = "originatingCountry = 'NO'";
		Subscription one = new Subscription(selector, SubscriptionStatus.REQUESTED);

		SubscriptionExchangeRequestApi requestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionExchangeRequestApi(name, Collections.singleton(one));
		assertThat(requestApi.getName()).isEqualTo(name);
		assertThat(requestApi.getSubscriptions()).hasSize(1);

		SubscriptionExchangeSubscriptionRequestApi onlySubscription = requestApi.getSubscriptions().iterator().next();
		assertThat(onlySubscription.getSelector()).isEqualTo(selector);

		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionExchangeRequestApiToSubscriptionRequest(requestApi);
		assertThat(result.getSubscriptions()).hasSize(1);
		Subscription subscription = result.getSubscriptions().iterator().next();
		assertThat(subscription.getSelector()).isEqualTo(selector);
	}

	@Test
	public void emptySubscriptionsToSubscriptionExchangeResponseApi() {
		String name = "myNode";
		SubscriptionExchangeResponseApi responseApi = subscriptionRequestTransformer.subscriptionsToSubscriptionExchangeResponseApi(name,Collections.emptySet());
		assertThat(responseApi.getName()).isEqualTo(name);
		assertThat(responseApi.getSubscriptions()).isEmpty();
		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionExchangeResponseApiToSubscriptionRequest(responseApi,SubscriptionRequestStatus.REQUESTED);
		assertThat(result.getSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptionSubscriptionPollResponse() {
		String neighbourName = "myNeighbour";
		String thisNodeName = "myName";
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		Subscription subscription = new Subscription(1,SubscriptionStatus.REQUESTED,selector,path);
		SubscriptionStatusPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionStatusPollResponseApi(subscription, neighbourName, thisNodeName);
		assertThat(responseApi.getPath()).isEqualTo(path);
		assertThat(responseApi.getSelector()).isEqualTo(selector);
		assertThat(responseApi.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
		Subscription result = subscriptionRequestTransformer.subscriptionStatusPollApiToSubscription(responseApi);
		assertThat(result.getSelector()).isEqualTo(selector);
		assertThat(result.getPath()).isEqualTo(path);
		assertThat(responseApi.getPath()).isEqualTo(path);
	}

	@Test
	public void subscriptionPollResponseApiWithStatusCreated() {
		String neighbourName = "myNeighbour";
		String thisNodeName = "myName";
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		Subscription subscription = new Subscription(1,SubscriptionStatus.CREATED,selector,path);
		SubscriptionStatusPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionStatusPollResponseApi(subscription, neighbourName, thisNodeName);
		assertThat(responseApi.getMessageBrokerUrl()).isEqualTo("amqps://" + thisNodeName);
	}

	@Test
	public void canTransformSubscriptionsToSubscriptionExchangeResponseApi() {
		Subscription subscription = new Subscription();
		subscription.setId(1);
		String path = "bouvet/subscriptions/1";
		subscription.setPath(path);
		String selector = "originatingCountry = 'NO'";
		subscription.setSelector(selector);
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionExchangeResponseApi response = subscriptionRequestTransformer.subscriptionsToSubscriptionExchangeResponseApi("bouvet", Collections.singleton(subscription));
		assertThat(response.getVersion()).isEqualTo("1.0");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		SubscriptionExchangeSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo("1");
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}
