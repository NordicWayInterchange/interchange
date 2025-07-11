package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionResponseApi;
import no.vegvesen.ixn.federation.model.*;
import org.junit.jupiter.api.Test;

import java.util.*;

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
		NeighbourSubscriptionRequest result = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(requestApi);
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

		NeighbourSubscriptionRequest result = subscriptionRequestTransformer.subscriptionRequestApiToSubscriptionRequest(requestApi);
		assertThat(result.getSubscriptions()).hasSize(1);
		NeighbourSubscription subscription = result.getSubscriptions().iterator().next();
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
		String neighbourName = "company";
		String consumerCommonName = "myName";
		NeighbourSubscription subscription = new NeighbourSubscription(
				1,
				NeighbourSubscriptionStatus.REQUESTED,
				selector,
				neighbourName,
				consumerCommonName
		);
		String path = neighbourName + "/subscriptions/" + subscription.getUuid();
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.neighbourSubscriptionToSubscriptionPollResponseApi(subscription);
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
				UUID.randomUUID().toString(),
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
		Set<NeighbourEndpoint> endpoints = Set.of(new NeighbourEndpoint("my-queue", hostName, Integer.parseInt(port)));
		NeighbourSubscription subscription = new NeighbourSubscription(
				NeighbourSubscriptionStatus.CREATED,
				selector,
				neighbourName,
				 "myNeighbour",
				endpoints

		);
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.neighbourSubscriptionToSubscriptionPollResponseApi(subscription);
		assertThat(responseApi.getEndpoints().size()).isEqualTo(1);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getHost()).isEqualTo(hostName);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getPort().toString()).isEqualTo(port);
	}

	@Test
	public void canTransformSubscriptionsToSubscriptionResponseApi() {
		String selector = "originatingCountry = 'NO'";
        NeighbourSubscription subscription = new NeighbourSubscription(selector, NeighbourSubscriptionStatus.REQUESTED);
		subscription.constructPath("bouvet");

		SubscriptionResponseApi response = subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi("bouvet", Collections.singleton(subscription));
		assertThat(response.getVersion()).isEqualTo("2.0");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		RequestedSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo(subscription.getUuid());
		String path = "bouvet/subscriptions/" + subscription.getUuid();
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}
