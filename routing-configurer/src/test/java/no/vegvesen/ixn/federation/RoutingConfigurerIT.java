package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.TestSSLContextConfig;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.Before;
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
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;


@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {RoutingConfigurerIT.Initializer.class})
public class RoutingConfigurerIT extends DockerBaseIT {

	@ClassRule
	public static GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurerIT.class);
	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, emptySet());
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

	RoutingConfigurer routingConfigurer;

	@Autowired
	QpidClient client;

	@Autowired
	RestTemplate restTemplate;

	@Before
	public void setUp() {
		routingConfigurer = new RoutingConfigurer(mock(NeighbourRepository.class), mock(ServiceProviderRepository.class), client);
	}

	@Test
	public void interchangeWithOneBindingIsCreated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", SubscriptionStatus.REQUESTED)));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupSubscriberRouting(flounder);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void interchangeWithTwoBindingsIsCreated() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupSubscriberRouting(halibut);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void interchangeIsBothCreatedAndUpdated() {
		Set<Subscription> subscriptions = new HashSet<>(Collections.singletonList(new Subscription("a = b", SubscriptionStatus.REQUESTED)));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupSubscriberRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		routingConfigurer.setupSubscriberRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}

	@Test
	public void interchangeCanUnbindSubscription() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.REQUESTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.REQUESTED);
		Set<Subscription> subscriptions = new HashSet<>(Arrays.asList(s1, s2));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupSubscriberRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = new HashSet<>(Collections.singletonList(s1));
		subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupSubscriberRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(1);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		routingConfigurer.setupSubscriberRouting(king_gustaf);
		SSLContext kingGustafSslContext = setUpTestSslContext("jks/king_gustaf.p12");
		Sink readKingGustafQueue = new Sink(AMQPS_URL, "king_gustaf", kingGustafSslContext);
		readKingGustafQueue.start();
		Source writeOnrampQueue = new Source(AMQPS_URL, "onramp", kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(AMQPS_URL, "onramp", kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on (onramp)");
		} catch (Exception ignore) {
		}
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}

	@Test
	public void newNeighbourCanNeitherWriteToFedExNorOnramp() throws JMSException, NamingException {
		HashSet<Subscription> subscriptions = new HashSet<>();
		subscriptions.add(new Subscription("originatingCountry = 'SE'", SubscriptionStatus.REQUESTED));
		Neighbour nordea = new Neighbour("nordea", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions), null);
		routingConfigurer.setupSubscriberRouting(nordea);
		SSLContext nordeaSslContext = setUpTestSslContext("jks/nordea.p12");
		try {
			Source writeFedExExchange = new Source(AMQPS_URL, "fedEx", nordeaSslContext);
			writeFedExExchange.start();
			writeFedExExchange.send("Ordinary business at the Nordea office.");
			fail("Should not allow neighbour nordea to write on (fedEx)");
		} catch (JMSException ignore) {
		}
		try {
			Source writeOnramp = new Source(AMQPS_URL, "onramp", nordeaSslContext);
			writeOnramp.start();
			writeOnramp.send("Make Nordea great again!");

			fail("Should not allow nordea to write on (onramp)");
		} catch (JMSException ignore) {
		}
		theNodeItselfCanReadFromAnyNeighbourQueue("nordea");
	}

	/**
	 * Called from newNeighbourCanWriteToFedExButNotOnramp to avoid setting up more neighbour nodes in the test
	 *
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