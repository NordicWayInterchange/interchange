package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.*;

import static java.util.Collections.emptySet;
import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.SERVICE_PROVIDERS_GROUP_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {QpidClientIT.Initializer.class})
public class QpidClientIT extends DockerBaseIT {


	@ClassRule
	public static GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private static Logger logger = LoggerFactory.getLogger(QpidClientIT.class);
	private static final String NW_EX = "nwEx";
	private static final String FED_EX = "fedEx";
	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, emptySet());
	private final Capabilities emptyCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet());
	private static String AMQPS_URL;

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			String httpsUrl = "https://localhost:" + qpidContainer.getMappedPort(HTTPS_PORT);
			String httpUrl = "http://localhost:" + qpidContainer.getMappedPort(8080);
			logger.info("server url: " + httpsUrl);
			logger.info("server url: " + httpUrl);
			AMQPS_URL = "amqps://localhost:" + qpidContainer.getMappedPort(AMQPS_PORT);
			TestPropertyValues.of(
					"qpid.rest.api.baseUrl=" + httpsUrl,
					"qpid.rest.api.vhost=localhost"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

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
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", SubscriptionStatus.REQUESTED)));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(flounder, NW_EX);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void interchangeWithTwoBindingsIsCreated() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(halibut, NW_EX);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void interchangeIsBothCreatedAndUpdated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", SubscriptionStatus.REQUESTED)));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(seabass, NW_EX);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		client.setupRouting(seabass, NW_EX);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}

	@Test
	public void interchangeCanUnbindSubscription() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		client.setupRouting(trout, NW_EX);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = new HashSet<>(Collections.singletonList(s1));
		subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions);
		trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		client.setupRouting(trout, NW_EX);
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
		client.addInterchangeUserToGroups(newUser, FEDERATED_GROUP_NAME);

		List<String> userNames = client.getInterchangesUserNames(FEDERATED_GROUP_NAME);

		assertThat(userNames).contains(newUser);
	}

	@Test
	public void deleteAnInterchangeFromGroups() {
		String deleteUser = "carp";
		client.addInterchangeUserToGroups(deleteUser, FEDERATED_GROUP_NAME);
		List<String> userNames = client.getInterchangesUserNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).contains(deleteUser);

		client.removeInterchangeUserFromGroups(FEDERATED_GROUP_NAME, deleteUser);
		userNames = client.getInterchangesUserNames(FEDERATED_GROUP_NAME);
		assertThat(userNames).doesNotContain(deleteUser);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		client.setupRouting(king_gustaf, NW_EX);
		client.setupRouting(king_gustaf, FED_EX);
		client.addInterchangeUserToGroups(king_gustaf.getName(), SERVICE_PROVIDERS_GROUP_NAME);
		SSLContext kingGustafSslContext = setUpTestSslContext("jks/king_gustaf.p12");
		Sink readKingGustafQueue = new Sink(AMQPS_URL, "king_gustaf", kingGustafSslContext);
		readKingGustafQueue.start();
		Source writeOnrampQueue = new Source(AMQPS_URL, "onramp", kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(AMQPS_URL, "onramp", kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on (onramp)");
		} catch (Exception ignore) { }
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
		acl.add("ACL ALLOW-LOG " + SERVICE_PROVIDERS_GROUP_NAME + " PUBLISH EXCHANGE routingkey = \"onramp\" name = \"\"");
		acl.add("ACL ALLOW-LOG " + SERVICE_PROVIDERS_GROUP_NAME + " ACCESS VIRTUALHOST name = \"localhost\"");
		acl.add("ACL ALLOW-LOG interchange CONSUME QUEUE name = \"onramp\"");
		acl.add("ACL DENY-LOG ALL ALL ALL");
		LinkedList<String> newAcl = new LinkedList<>(client.addOneConsumeRuleBeforeLastRule(new ServiceProvider("king_harald"), "king_harald", acl));
		assertThat(acl.getFirst()).isEqualTo(newAcl.getFirst());
		assertThat(acl.getLast()).isEqualTo(newAcl.getLast());
		assertThat(newAcl.get(newAcl.size()-2)).contains("king_harald");
	}

	@Test
	public void newNeighbourCanWriteToFedExButNotOnramp() throws JMSException, NamingException {
		HashSet<Subscription> subscriptions = new HashSet<>();
		subscriptions.add(new Subscription("where = 'SE'", SubscriptionStatus.REQUESTED));
		Neighbour nordea = new Neighbour("nordea", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, subscriptions), null);
		client.setupRouting(nordea, NW_EX);
		client.addInterchangeUserToGroups(nordea.getName(), FEDERATED_GROUP_NAME);
		SSLContext nordeaSslContext = setUpTestSslContext("jks/nordea.p12");
		Source writeFedExExchange = new Source(AMQPS_URL, "fedEx", nordeaSslContext);
		writeFedExExchange.start();
		writeFedExExchange.send("Ordinary business at the Nordea office.");
		try {
			Source writeOnramp = new Source(AMQPS_URL, "onramp", nordeaSslContext);
			writeOnramp.start();
			writeOnramp.send("Make Nordea great again!");

			fail("Should not allow nordea to write on (onramp)");
		} catch (JMSException ignore) { }
		theNodeItselfCanReadFromAnyNeighbourQueue("nordea");
	}


	/**
	 * Called from newNeighbourCanWriteToFedExButNotOnramp to avoid setting up more neighbour nodes in the test
	 * @param neighbourQueue name of the queue to be read by the node itself
	 */
	public void theNodeItselfCanReadFromAnyNeighbourQueue(String neighbourQueue) throws NamingException, JMSException {
		SSLContext localhostSslContext = setUpTestSslContext("jks/localhost.p12");
		Sink neighbourSink = new Sink(AMQPS_URL, neighbourQueue, localhostSslContext);
		neighbourSink.start();
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(getFilePathFromClasspathResource(s), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(getFilePathFromClasspathResource("jks/truststore.jks"), "password", KeystoreType.JKS));
	}

}