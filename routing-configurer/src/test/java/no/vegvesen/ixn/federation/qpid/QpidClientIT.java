package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@RunWith(SpringRunner.class)
public class QpidClientIT {

	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet());
	private final Capabilities emptyCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());

	@Autowired
	QpidClient client;

	@Test
	public void pingQpid() {
		assertThat(client.ping()).isEqualTo(200);
	}

	@Test
	public void createQueue(){
		Interchange findus = new Interchange("findus", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		client.createQueue(findus);
	}

	@Test(expected = Exception.class)
	public void createQueueWithIllegalCharactersInIdFails() {
		Interchange torsk = new Interchange("torsk", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		client.createQueue(torsk);
		client.createQueue(torsk); //create some queue that already exists
	}

	@Test
	public void createdQueueCanBeQueriedFromQpid() {
		Interchange leroy = new Interchange("leroy", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);
		client.createQueue(leroy);
		assertThat(client.queueExists(leroy.getName())).isTrue();
	}

	@Test
	public void queueNotCreatedQueueDoesNotExist() {
		assertThat(client.queueExists("mackrel")).isFalse();
	}

	@Test
	public void interchangeWithOneBindingIsCreated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED)));
		Interchange flounder = new Interchange("flounder", Collections.emptySet(), subscriptions, Collections.emptySet());
		client.setupRouting(flounder);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void interchangeWithTwoBindingsIsCreated() {
		Subscription s1 = new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", Subscription.SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		Interchange halibut = new Interchange("halibut", Collections.emptySet(), subscriptions, Collections.emptySet());
		client.setupRouting(halibut);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void interchangeIsBothCreatedAndUpdated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED)));
		Interchange seabass = new Interchange("seabass", Collections.emptySet(), subscriptions, Collections.emptySet());
		client.setupRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		client.setupRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}
}