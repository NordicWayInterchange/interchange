package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;


@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {RoutingConfigurer.class, QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {RoutingConfigurerIT.Initializer.class})
@Testcontainers
public class RoutingConfigurerIT extends QpidDockerBaseIT {

	private static Path testKeysPath = generateKeys(RoutingConfigurerIT.class, "my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

	@Container
	public static final GenericContainer qpidContainer = getQpidContainerGeneratedKeys("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password");

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
					"routing-configurer.baseUrl=" + httpsUrl,
					"routing-configurer.vhost=localhost",
					"test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
					"test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12")
			).applyTo(configurableApplicationContext.getEnvironment());
		}

	}

	@MockBean
	NeighbourService neighbourService;

	@MockBean
	ServiceProviderRouter serviceProviderRouter;

	@Autowired
	RoutingConfigurer routingConfigurer;

	@Autowired
	QpidClient client;

	@Test
	public void neighbourWithOneBindingIsCreated() {
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("a = b", SubscriptionStatus.ACCEPTED));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(flounder);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void neighbourWithTwoBindingsIsCreated() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.ACCEPTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.ACCEPTED);
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(halibut);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void neighbourWithTwoBindingsAndOnlyOneIsAcceptedIsCreated() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.ACCEPTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.REJECTED);
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour salmon = new Neighbour("salmon", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(salmon);
		assertThat(client.queueExists(salmon.getName())).isTrue();
		Set<String> queueBindKeys = client.getQueueBindKeys(salmon.getName());
		assertThat(queueBindKeys).hasSize(1);
	}

	@Test
	public void neighbourIsBothCreatedAndUpdated() {
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("a = b", SubscriptionStatus.ACCEPTED));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}

	@Test
	public void neighbourCanUnbindSubscription() {
		Subscription s1 = new Subscription("a = b", SubscriptionStatus.ACCEPTED);
		Subscription s2 = new Subscription("b = c", SubscriptionStatus.ACCEPTED);
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = Sets.newLinkedHashSet(new Subscription("a = b", SubscriptionStatus.ACCEPTED));
		subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(1);
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
		subscriptions.add(new Subscription("originatingCountry = 'SE'", SubscriptionStatus.ACCEPTED));
		Neighbour nordea = new Neighbour("nordea", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions), null);
		routingConfigurer.setupNeighbourRouting(nordea);
		SSLContext nordeaSslContext = setUpTestSslContext("nordea.p12");
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

	@Test
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyCapabilities, emptySubscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).contains(toreDownNeighbour.getName());

		routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).doesNotContain(toreDownNeighbour.getName());
	}


	public void theNodeItselfCanReadFromAnyNeighbourQueue(String neighbourQueue) throws NamingException, JMSException {
		SSLContext localhostSslContext = setUpTestSslContext("localhost.p12");
		Sink neighbourSink = new Sink(AMQPS_URL, neighbourQueue, localhostSslContext);
		neighbourSink.start();
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}