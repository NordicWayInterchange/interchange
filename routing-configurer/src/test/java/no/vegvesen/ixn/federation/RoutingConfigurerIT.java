package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsMessage;
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
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;


@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {RoutingConfigurer.class, QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, ServiceProviderRouter.class})
@ContextConfiguration(initializers = {RoutingConfigurerIT.Initializer.class})
@Testcontainers
public class RoutingConfigurerIT extends QpidDockerBaseIT {


	private static Path testKeysPath = getFolderPath("target/test-keys" + RoutingConfigurerIT.class.getSimpleName());

	@Container
	public static final GenericContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

	@Container
	public static final GenericContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurerIT.class);

	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, emptySet());
	private final Capabilities emptyCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet());

	private static String AMQPS_URL;

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			qpidContainer.followOutput(new Slf4jLogConsumer(logger));
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

	@Autowired
	RoutingConfigurer routingConfigurer;

	@Autowired
	QpidClient client;

	@MockBean
	ServiceProviderRouter serviceProviderRouter;


	@Test
	public void neighbourWithOneBindingIsCreated() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Arrays.asList("Road Block")));

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "flounder"));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("first");
		firstServiceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN,Collections.singleton(cap)));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(firstServiceProvider));

		routingConfigurer.setupNeighbourRouting(flounder);
		assertThat(client.queueExists(flounder.getName())).isTrue();
	}

	@Test
	public void neighbourWithTwoBindingsIsCreated() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		Subscription s1 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "halibut");
		Subscription s2 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'SE' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "halibut");

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		//ServiceProvider serviceProvider = new ServiceProvider()

		//when(serviceProviderRouter.findServiceProviders()).thenReturn();
		fail("Anna, here you go!");
		routingConfigurer.setupNeighbourRouting(halibut);
		assertThat(client.queueExists(halibut.getName())).isTrue();
	}

	@Test
	public void neighbourWithTwoBindingsAndOnlyOneIsAcceptedIsCreated() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		Subscription s1 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "salmon");
		Subscription s2 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'SE' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.REJECTED, "salmon");

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour salmon = new Neighbour("salmon", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1, cap2)));
		routingConfigurer.setupNeighbourRouting(salmon);
		assertThat(client.queueExists(salmon.getName())).isTrue();
		Set<String> queueBindKeys = client.getQueueBindKeys(salmon.getName());
		assertThat(queueBindKeys).hasSize(1);
		Set<Subscription> createdSubscriptions = salmon.getNeighbourRequestedSubscriptions().getSubscriptions().stream().filter(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.CREATED)).collect(Collectors.toSet());
		assertThat(createdSubscriptions).hasSize(1);
	}

	@Test
	public void neighbourIsBothCreatedAndUpdated() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Arrays.asList("Road Block")));

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "seabass"));
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));

		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
	}

	@Test
	public void neighbourCanUnbindSubscription() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		Subscription s1 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "trout");
		Subscription s2 = new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'SE' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "trout");
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1, cap2)));
		routingConfigurer.setupNeighbourRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = Sets.newLinkedHashSet(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "trout"));
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
	public void newNeighbourCanNeitherWriteToIncomingExchangeNorOnramp() throws JMSException, NamingException {
		Capability cap = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subscriptions = new HashSet<>();
		subscriptions.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "nordea"));

		Neighbour nordea = new Neighbour(
				"nordea",
				new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()),
				new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions),
				null);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(nordea);
		SSLContext nordeaSslContext = setUpTestSslContext("nordea.p12");
		try {
			Source writeIncomingExchange = new Source(AMQPS_URL, "incomingExchange", nordeaSslContext);
			writeIncomingExchange.start();
			JmsMessage message = writeIncomingExchange
					.createMessageBuilder()
					.textMessage("Ordinary business at the Nordea office.")
					.originatingCountry("SE")
					.build();
			writeIncomingExchange.send(message);
			fail("Should not allow neighbour nordea to write on (incomingExchange)");
		} catch (JMSException ignore) {
		}
		try {
			Source writeOnramp = new Source(AMQPS_URL, "onramp", nordeaSslContext);
			writeOnramp.start();
			JmsMessage message = writeOnramp.createMessageBuilder().textMessage("Make Nordea great again!").build();
			writeOnramp.send(message);

			fail("Should not allow nordea to write on (onramp)");
		} catch (JMSException ignore) {
		}
		theNodeItselfCanReadFromAnyNeighbourQueue("nordea");
	}

	@Test
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "tore-down-neighbour"));

		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyCapabilities, new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).contains(toreDownNeighbour.getName());

		subs.clear();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.TEAR_DOWN, "tore-down-neighbour"));

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(emptySet());
		routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).doesNotContain(toreDownNeighbour.getName());
	}


	@Test
	public void addingOneSubscriptionResultsInOneBindKey() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "hammershark"));

		Neighbour hammershark = new Neighbour("hammershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(hammershark);
		assertThat(client.queueExists("hammershark")).isTrue();
		assertThat(client.getQueueBindKeys("hammershark").size()).isEqualTo(1);
	}

	@Test
	public void addingTwoSubscriptionsResultsInTwoBindKeys() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "tigershark"));

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1)));
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.queueExists("tigershark")).isTrue();
		assertThat(client.getQueueBindKeys("tigershark").size()).isEqualTo(1);

		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "tigershark"));

		tigershark.setNeighbourRequestedSubscriptions(new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs));

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1, cap2)));
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.getQueueBindKeys("tigershark").size()).isEqualTo(2);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(2);
	}

	@Test
	public void setUpQueueForServiceProvider() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.MANDATORY, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "remote-service-provider"));

		Neighbour neigh = new Neighbour("negih-true", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));

		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists("remote-service-provider")).isTrue();
	}
	@Test
	public void setUpQueueForNeighbour() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "neigh-false"));

		Neighbour neigh = new Neighbour("neigh-false", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(neigh);

		assertThat(client.queueExists(neigh.getName())).isTrue();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbour() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.MANDATORY, new HashSet<>(Arrays.asList("Road Block")));
		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "remote-service-provider"));

		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "neigh-true-and-false"));

		Neighbour neigh = new Neighbour("neigh-true-and-false", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1, cap2)));

		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists("remote-service-provider")).isTrue();
		assertThat(client.queueExists(neigh.getName())).isTrue();
	}

	@Test
	public void routingIsNotSetUpWhenTryingToRedirect() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.MANDATORY, new HashSet<>(Arrays.asList("Road Block")));

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "cod"));

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour cod = new Neighbour("cod", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(cod);
		assertThat(client.queueExists(cod.getName())).isFalse();
		assertThat(cod.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
	}

	@Test
	public void routingIsNotSetUpWhenRedirectIsNotAvailable() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		Set<Subscription> subscriptions = Sets.newLinkedHashSet(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "no-clownfish"));

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour clownfish = new Neighbour("clownfish", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap)));
		routingConfigurer.setupNeighbourRouting(clownfish);
		assertThat(client.queueExists(clownfish.getName())).isFalse();
		assertThat(clownfish.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get().getSubscriptionStatus()).isEqualTo(SubscriptionStatus.ILLEGAL);
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Arrays.asList("Road Block")));

		HashSet<Subscription> subs = new HashSet<>();
		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "remote-sp"));

		subs.add(new Subscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", SubscriptionStatus.ACCEPTED, "neigh-both"));

		Neighbour neigh = new Neighbour("neigh-both", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(CapabilityCalculator.calculateSelfCapabilities(serviceProviderRouter.findServiceProviders())).thenReturn(new HashSet<>(Arrays.asList(cap1)));

		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists("remote-sp")).isTrue();
		assertThat(client.queueExists(neigh.getName())).isTrue();
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