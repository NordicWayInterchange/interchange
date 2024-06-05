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
		String path = "myName/subscriptions/1";
		String consumerCommonName = "myName";
		NeighbourSubscription subscription = new NeighbourSubscription(1,NeighbourSubscriptionStatus.REQUESTED,selector,path,consumerCommonName);
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
				UUID.randomUUID(),
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
		NeighbourSubscription subscription = new NeighbourSubscription(1,NeighbourSubscriptionStatus.CREATED,selector,path, "myNeighbour");
		subscription.setEndpoints(new HashSet<>(Collections.singleton(new NeighbourEndpoint("my-queue", hostName, Integer.parseInt(port)))));
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.neighbourSubscriptionToSubscriptionPollResponseApi(subscription);
		assertThat(responseApi.getEndpoints().size()).isEqualTo(1);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getHost()).isEqualTo(hostName);
		assertThat(new ArrayList<>(responseApi.getEndpoints()).get(0).getPort().toString()).isEqualTo(port);
	}

	@Test
	public void canTransformSubscriptionsToSubscriptionResponseApi() {
		NeighbourSubscription subscription = new NeighbourSubscription();
		UUID uuid = UUID.randomUUID();
		subscription.setUuid(uuid);
		String path = "bouvet/subscriptions/1";
		subscription.setPath(path);
		String selector = "originatingCountry = 'NO'";
		subscription.setSelector(selector);
		subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.REQUESTED);

		SubscriptionResponseApi response = subscriptionRequestTransformer.subscriptionsToSubscriptionResponseApi("bouvet", Collections.singleton(subscription));
		assertThat(response.getVersion()).isEqualTo("2.0");
		assertThat(response.getName()).isEqualTo("bouvet");
		assertThat(response.getSubscriptions()).hasSize(1);

		RequestedSubscriptionResponseApi subsResponse = response.getSubscriptions().iterator().next();
		assertThat(subsResponse.getId()).isEqualTo(uuid);
		assertThat(subsResponse.getPath()).isEqualTo(path);
		assertThat(subsResponse.getSelector()).isEqualTo(selector);
		assertThat(subsResponse.getStatus()).isEqualTo(SubscriptionStatusApi.REQUESTED);
	}
}
