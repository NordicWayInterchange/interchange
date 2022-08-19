package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.model.Endpoint;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
	public void emptySubscriptionResponseApiToSubscriptions() {
		String name = "myNode";
		SubscriptionResponseApi responseApi = subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi(name,Collections.emptySet());
		assertThat(responseApi.getName()).isEqualTo(name);
		assertThat(responseApi.getSubscriptions()).isEmpty();
		Set<Subscription> subscriptions = subscriptionTransformer.requestedSubscriptionResponseApiToSubscriptions(responseApi.getSubscriptions());
		assertThat(subscriptions).isEmpty();
	}

	@Test
	public void subscriptionSubscriptionPollResponse() {
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		String consumerCommonName = "myName";
		Subscription subscription = new Subscription(1,SubscriptionStatus.REQUESTED,selector,path,consumerCommonName);
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription);
		assertThat(responseApi.getPath()).isEqualTo(path);
		assertThat(responseApi.getSelector()).isEqualTo(selector);
		assertThat(responseApi.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
		Subscription result = subscriptionRequestTransformer.subscriptionPollApiToSubscription(responseApi);
		assertThat(result.getSelector()).isEqualTo(selector);
		assertThat(result.getPath()).isEqualTo(path);
		assertThat(responseApi.getPath()).isEqualTo(path);
	}

	@Test
	public void subscriptionPollApiWithNullEndpoint() {
		SubscriptionPollResponseApi api = new SubscriptionPollResponseApi(
				"1",
				"t = b",
				"/mynode/1",
				SubscriptionStatusApi.REQUESTED,
				"mynode",
				null
		);
		Subscription subscription = subscriptionRequestTransformer.subscriptionPollApiToSubscription(api);
		assertThat(subscription.getEndpoints()).isEmpty();

	}

	@Test
	public void subscriptionPollResponseApiWithStatusCreated() {
		String neighbourName = "myNeighbour";
		String hostName = "myName";
		String port = "5671";
		String selector = "originatingCountry = 'NO'";
		String path = "myName/subscriptions/1";
		Subscription subscription = new Subscription(1,SubscriptionStatus.CREATED,selector,path, "myNeighbour");
		subscription.setEndpoints(new HashSet<>(Collections.singleton(new Endpoint("my-queue", hostName, Integer.parseInt(port)))));
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription);
		assertThat(responseApi.getEndpoints().size()).isEqualTo(1);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getHost()).isEqualTo(hostName);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getPort().toString()).isEqualTo(port);
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
		assertThat(response.getVersion()).isEqualTo("1.3NW3");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		RequestedSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo("1");
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}
