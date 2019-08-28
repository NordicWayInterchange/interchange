package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.interchange.Sink;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.*;

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
	public void createQueue() {
		Neighbour findus = new Neighbour("findus", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		client.createQueue(findus);
	}

	@Test(expected = Exception.class)
	public void createQueueWithIllegalCharactersInIdFails() {
		Neighbour torsk = new Neighbour("torsk", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		client.createQueue(torsk);
		client.createQueue(torsk); //create some queue that already exists
	}

	@Test
	public void createdQueueCanBeQueriedFromQpid() {
		Neighbour leroy = new Neighbour("leroy", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);
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
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(flounder);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void interchangeWithTwoBindingsIsCreated() {
		Subscription s1 = new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", Subscription.SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(halibut);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void interchangeIsBothCreatedAndUpdated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED)));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		client.setupRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}

	@Test
	public void interchangeCanUnbindSubscription() {
		Subscription s1 = new Subscription("a = b", Subscription.SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", Subscription.SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = new HashSet<>(Collections.singletonList(s1));
		subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		client.setupRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(1);
	}

	@Test
	public void tearDownQueue() {
		Neighbour crab = new Neighbour("crab", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		//Set up a new queue
		client.createQueue(crab);
		assertThat(client.queueExists(crab.getName())).isTrue();

		//Delete the queue
		client.removeQueue(crab.getName());
		assertThat(client.queueExists(crab.getName())).isFalse();
	}

	@Test
	public void addAnInterchangeToGroups() {
		String newUser = "herring";
		client.addInterchangeUserToGroups(newUser, "federated-interchanges");

		List<String> userNames = client.getInterchangesUserNames("federated-interchanges");

		assertThat(userNames).contains(newUser);
	}

	@Test
	public void deleteAnInterchangeFromGroups() {
		String deleteUser = "carp";
		client.addInterchangeUserToGroups(deleteUser, "federated-interchanges");
		List<String> userNames = client.getInterchangesUserNames("federated-interchanges");
		assertThat(userNames).contains(deleteUser);

		client.removeInterchangeUserFromGroups("federated-interchanges", deleteUser);
		userNames = client.getInterchangesUserNames("federated-interchanges");
		assertThat(userNames).doesNotContain(deleteUser);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		client.createQueue(king_gustaf);
		client.addInterchangeUserToGroups(king_gustaf.getName(), "service-providers");
		client.addReadAccess(king_gustaf, "king_gustaf");
		SSLContext kingGustafSslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(getFilePathFromClasspathResource("jks/king_gustaf.p12"), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(getFilePathFromClasspathResource("jks/truststore.jks"), "password", KeystoreType.JKS));
		Sink readKingGustafQueue = new Sink("amqps://localhost:62671", "king_gustaf", kingGustafSslContext);
		readKingGustafQueue.start();
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}

	@Test
	public void addAccessBuildsUpRules() {
		List<String> initialACL = client.getACL();
		client.addReadAccess(new ServiceProvider("routing_configurer"), "onramp");
		List<String> newACL = client.getACL();
		assertThat(newACL).hasSize(initialACL.size() + 1);
	}


	@Test
	public void addOneConsumeRuleBeforeLastRule() {
		LinkedList<String> acl = new LinkedList<>();
		acl.add("ACL ALLOW-LOG interchange ALL ALL");
		acl.add("ACL ALLOW-LOG administrators ALL ALL");
		acl.add("ACL ALLOW-LOG service-providers PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"");
		acl.add("ACL ALLOW-LOG service-providers ACCESS VIRTUALHOST name = \"localhost\"");
		acl.add("ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"");
		acl.add("ACL DENY-LOG ALL ALL ALL");
		LinkedList<String> newAcl = new LinkedList<>(client.addOneConsumeRuleBeforeLastRule(new ServiceProvider("king_harald"), "king_harald", acl));
		assertThat(acl.getFirst()).isEqualTo(newAcl.getFirst());
		assertThat(acl.getLast()).isEqualTo(newAcl.getLast());
		assertThat(newAcl.get(newAcl.size()-2)).contains("king_harald");
	}
}