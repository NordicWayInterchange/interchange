package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class SubscriptionRequestTransformerTest {

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	@Test
	public void emptySubscriptionsToRequestedSubscriptionResponseApi() {
		String name = "myNode";
		SubscriptionRequestApi requestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(name, Collections.emptySet());
		assertThat(requestApi.getName()).isEqualTo(name);
		assertThat(requestApi.getSubscriptions()).isEmpty();
		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(requestApi);
		assertThat(result.getSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptonsToRequestedSubscriptionResponseApi() {
		String name = "myNode";
		String selector = "originatingCountry = 'NO'";
		Subscription one = new Subscription(selector, SubscriptionStatus.REQUESTED, "");

		SubscriptionRequestApi requestApi = subscriptionRequestTransformer.subscriptionRequestToSubscriptionRequestApi(name, Collections.singleton(one));
		assertThat(requestApi.getName()).isEqualTo(name);
		assertThat(requestApi.getSubscriptions()).hasSize(1);

		RequestedSubscriptionApi onlySubscription = requestApi.getSubscriptions().iterator().next();
		assertThat(onlySubscription.getSelector()).isEqualTo(selector);

		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(requestApi);
		assertThat(result.getSubscriptions()).hasSize(1);
		Subscription subscription = result.getSubscriptions().iterator().next();
		assertThat(subscription.getSelector()).isEqualTo(selector);
	}

	@Test
	public void emptySubscriptionsToSubscriptionRequestApi() {
		String name = "myNode";
		SubscriptionResponseApi responseApi = subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(name,Collections.emptySet());
		assertThat(responseApi.getName()).isEqualTo(name);
		assertThat(responseApi.getSubscriptions()).isEmpty();
		SubscriptionRequest result = subscriptionRequestTransformer.subscriptionResponseApiToSubscriptionRequest(responseApi,SubscriptionRequestStatus.REQUESTED);
		assertThat(result.getSubscriptions()).isEmpty();
	}

	@Test
	public void subscriptionSubscriptionPollResponse() {
		String neighbourName = "myNeighbour";
		String thisNodeName = "myName";
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		String consumerCommonName = "myName";
		Subscription subscription = new Subscription(1,SubscriptionStatus.REQUESTED,selector,path,consumerCommonName);
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription, neighbourName, thisNodeName);
		assertThat(responseApi.getPath()).isEqualTo(path);
		assertThat(responseApi.getSelector()).isEqualTo(selector);
		assertThat(responseApi.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
		Subscription result = subscriptionRequestTransformer.subscriptionPollApiToSubscription(responseApi);
		assertThat(result.getSelector()).isEqualTo(selector);
		assertThat(result.getPath()).isEqualTo(path);
		assertThat(responseApi.getPath()).isEqualTo(path);
	}

	@Test
	public void subscriptionPollResponseApiWithStatusCreated() {
		String neighbourName = "myNeighbour";
		String brokerUrl = "amqps://myName";
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		Subscription subscription = new Subscription(1,SubscriptionStatus.CREATED,selector,path, "myNeighbour");
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription, neighbourName, brokerUrl);
		assertThat(responseApi.getEndpoints().size()).isEqualTo(1);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getMessageBrokerUrl()).isEqualTo(brokerUrl);
	}

	@Test
	public void canTransformSubscriptionsToSubscriptionResponseApi() {
		Subscription subscription = new Subscription();
		subscription.setId(1);
		String path = "bouvet/subscriptions/1";
		subscription.setPath(path);
		String selector = "originatingCountry = 'NO'";
		subscription.setSelector(selector);
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		SubscriptionResponseApi response = subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi("bouvet", Collections.singleton(subscription));
		assertThat(response.getVersion()).isEqualTo("1.1NW3");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		RequestedSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo("1");
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}
