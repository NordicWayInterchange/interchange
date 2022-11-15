package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsMessage;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Disabled;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {RoutingConfigurer.class, QpidClient.class, RoutingConfigurerProperties.class, QpidClientConfig.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class, ServiceProviderRouter.class})
@ContextConfiguration(initializers = {RoutingConfigurerIT.Initializer.class})
@Testcontainers
public class RoutingConfigurerIT extends QpidDockerBaseIT {


	private static Path testKeysPath = getFolderPath("target/test-keys" + RoutingConfigurerIT.class.getSimpleName());

	@Container
	public static final KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
            .dependsOn(keyContainer);

	@Autowired
	SSLContext sslContext;

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurerIT.class);

	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, emptySet());
	private final Capabilities emptyCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet());

	private static String AMQPS_URL;

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			qpidContainer.followOutput(new Slf4jLogConsumer(logger));
			String httpsUrl = qpidContainer.getHttpsUrl();
			String httpUrl = qpidContainer.getHttpUrl();
			logger.info("server url: " + httpsUrl);
			logger.info("server url: " + httpUrl);
			AMQPS_URL = qpidContainer.getAmqpsUrl();
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
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap.setCapabilityExchangeName("cap-ex10");
		client.createDirectExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "flounder");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(subscription);

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(flounder);
		assertThat(client.queueExists(subscription.getQueueName())).isTrue();
		assertThat(subscription.getLastUpdatedTimestamp()).isGreaterThan(0);
	}

	@Test
	public void neighbourWithTwoBindingsIsCreated() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap1.setCapabilityExchangeName("cap-ex11");
		client.createDirectExchange("cap-ex11");

		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap2.setCapabilityExchangeName("cap-ex11");
		client.createDirectExchange("cap-ex11");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription s1 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		NeighbourSubscription s2 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'Road Block' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(halibut);
		assertThat(client.queueExists(s1.getQueueName())).isTrue();
		assertThat(client.queueExists(s2.getQueueName())).isTrue();
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isGreaterThan(0);
	}

	@Test
	public void neighbourWithTwoBindingsAndOnlyOneIsAcceptedIsCreated() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap1.setCapabilityExchangeName("cap-ex12");
		client.createDirectExchange("cap-ex12");

		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap2.setCapabilityExchangeName("cap-ex13");
		client.createDirectExchange("cap-ex13");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription s1 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'Road Block' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "salmon");

		NeighbourSubscription s2 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'Road Block' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.REJECTED, "salmon");

		//Just to ensure the timestamp is updated by RoutingConfigurer
		assertThat(s1.getLastUpdatedTimestamp()).isEqualTo(0);
		assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour salmon = new Neighbour("salmon", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(salmon);
		assertThat(client.queueExists(s1.getQueueName())).isTrue();
		Set<String> queueBindKeys = client.getQueueBindKeys(s1.getQueueName());
		assertThat(queueBindKeys).hasSize(1);
		assertThat(s2.getQueueName()).isNull();

		Set<NeighbourSubscription> createdSubscriptions = salmon.getNeighbourRequestedSubscriptions().getCreatedSubscriptions();
		assertThat(createdSubscriptions).hasSize(1);

		//Showing that the timestamp have been changed for the ACCEPTED subscription, but not for the REJECTED one
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);
	}

	@Test
	@Disabled
	public void neighbourIsBothCreatedAndUpdated() {
		NeighbourSubscription subscription = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "seabass");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(subscription);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour seabass = new Neighbour("seabass", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();
		long timestamp = subscription.getLastUpdatedTimestamp();
		assertThat(timestamp).isGreaterThan(0);

		routingConfigurer.setupNeighbourRouting(seabass);
		assertThat(client.queueExists(seabass.getName())).isTrue();

		//no change, no change in timestamp
		assertThat(subscription.getLastUpdatedTimestamp()).isEqualTo(timestamp);
	}

	@Test
	@Disabled
	public void neighbourCanUnbindSubscription() {
		NeighbourSubscription s1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "trout");
		NeighbourSubscription s2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'SE' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "trout");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(2);

		subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') AND publicationType = 'Road Block' AND messageType = 'DATEX2' AND originatingCountry = 'NO' AND protocolVersion = '1.0' AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "trout"));
		subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		trout = new Neighbour("trout", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(trout);
		assertThat(client.getQueueBindKeys(trout.getName())).hasSize(1);
	}

	@Test
	public void newNeighbourCanNeitherWriteToIncomingExchangeNorOnramp() throws JMSException, NamingException {
		Capability cap = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Collections.singletonList("Road Block")));
		cap.setCapabilityExchangeName("cap-ex14");
		client.createDirectExchange("cap-ex14");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		HashSet<NeighbourSubscription> subscriptions = new HashSet<>();
		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "nordea");
		subscriptions.add(subscription);

		Neighbour nordea = new Neighbour(
				"nordea",
				new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()),
				new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions),
				null);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		routingConfigurer.setupNeighbourRouting(nordea);
		SSLContext nordeaSslContext = setUpTestSslContext("nordea.p12");
		try {
			Source writeIncomingExchange = new Source(AMQPS_URL, "incomingExchange", nordeaSslContext);
			writeIncomingExchange.start();
			JmsMessage message = writeIncomingExchange
					.createMessageBuilder()
					.textMessage("Ordinary business at the Nordea office.")
					.messageType(Constants.DATEX_2)
					.quadTreeTiles(",123,")
					.publicationType("Test")
					.publisherId("NO-123")
					.originatingCountry("SE")
					.protocolVersion("1.0")
					.build();
			writeIncomingExchange.send(message);
			fail("Should not allow neighbour nordea to write on (incomingExchange)");
		} catch (JMSException ignore) {
		}
		try {
			Source writeOnramp = new Source(AMQPS_URL, "onramp", nordeaSslContext);
			writeOnramp.start();
			JmsMessage message = writeOnramp.createMessageBuilder()
					.textMessage("Make Nordea great again!")
					.messageType(Constants.DATEX_2)
					.publisherId("NO-123")
					.publicationType("Test")
					.quadTreeTiles(",123,")
					.originatingCountry("NO")
					.protocolVersion("1.0")
					.build();
			writeOnramp.send(message);

			fail("Should not allow nordea to write on (onramp)");
		} catch (JMSException ignore) {
		}
		theNodeItselfCanReadFromAnyNeighbourQueue(subscription.getQueueName());
	}

	@Test
	@Disabled
	//TODO: Have to write tear down all over again, does not do what it should atm
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tore-down-neighbour"));

		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyCapabilities, new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).contains(toreDownNeighbour.getName());

		subs.clear();
		subs.add(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.TEAR_DOWN, "tore-down-neighbour"));

		routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMemberNames(QpidClient.FEDERATED_GROUP_NAME)).doesNotContain(toreDownNeighbour.getName());
	}


	@Test
	public void addingOneSubscriptionResultsInOneBindKey() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		cap.setCapabilityExchangeName("cap-ex1");
		client.createDirectExchange("cap-ex1");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "hammershark");
		subs.add(sub);

		Neighbour hammershark = new Neighbour("hammershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));

		routingConfigurer.setupNeighbourRouting(hammershark);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub.getQueueName()).size()).isEqualTo(1);
	}

	@Test
	public void addingTwoSubscriptionsResultsInTwoBindKeys() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		cap1.setCapabilityExchangeName("cap-ex2");
		client.createDirectExchange("cap-ex2");

		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));
		cap2.setCapabilityExchangeName("cap-ex3");
		client.createDirectExchange("cap-ex3");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub1);

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.queueExists(sub1.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub1.getQueueName()).size()).isEqualTo(1);

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub2);

		tigershark.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.getQueueBindKeys(sub1.getQueueName()).size()).isEqualTo(1);
		assertThat(client.queueExists(sub2.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub2.getQueueName()).size()).isEqualTo(1);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(2);
	}

	@Test
	public void addingTwoSubscriptionsAndOneCapabilityResultsInTwoBindKeys() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Collections.singletonList("01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Collections.singletonList("Road Block")));
		cap1.setCapabilityExchangeName("cap-ex4");
		client.createDirectExchange("cap-ex4");

		Capability cap2 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Collections.singletonList("01230122")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Collections.singletonList("Road Block")));
		cap2.setCapabilityExchangeName("cap-ex5");
		client.createDirectExchange("cap-ex5");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub);

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub.getQueueName()).size()).isEqualTo(2);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void setUpQueueForServiceProvider() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.MANDATORY, new HashSet<>(Collections.singletonList("Road Block")));
		cap.setCapabilityExchangeName("cap-ex6");
		client.createDirectExchange("cap-ex6");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		subs.add(sub);

		Neighbour neigh = new Neighbour("negih-true", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbour() {
		Capability cap1 = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.MANDATORY, new HashSet<>(Collections.singletonList("Road Block")));
		cap1.setCapabilityExchangeName("cap-ex7");
		client.createDirectExchange("cap-ex7");

		Capability cap2 = new DatexCapability("NO-1234", "SE", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Collections.singletonList("Road Block")));
		cap2.setCapabilityExchangeName("cap-ex8");
		client.createDirectExchange("cap-ex8");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-true-and-false");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub1);
		subs.add(sub2);

		Neighbour neigh = new Neighbour("neigh-true-and-false", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub1.getQueueName())).isTrue();
		assertThat(client.queueExists(sub2.getQueueName())).isTrue();
	}

	@Test
	public void routingIsNotSetUpWhenTryingToRedirect() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.NOT_AVAILABLE, new HashSet<>(Arrays.asList("Road Block")));

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider"));

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour cod = new Neighbour("cod", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(serviceProvider));
		routingConfigurer.setupNeighbourRouting(cod);
		assertThat(client.queueExists(cod.getName())).isFalse();
		assertThat(cod.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get().getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
	}

	@Test
	public void routingIsNotSetUpWhenRedirectIsNotAvailable() {
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "no-clownfish"));

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour clownfish = new Neighbour("clownfish", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(clownfish);
		assertThat(client.queueExists(clownfish.getName())).isFalse();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
		Capability cap = new DatexCapability("NO-1234", "NO", "1.0", new HashSet<>(Arrays.asList("01230122", "01230123")), RedirectStatus.OPTIONAL, new HashSet<>(Collections.singletonList("Road Block")));
		cap.setCapabilityExchangeName("cap-ex9");
		client.createDirectExchange("cap-ex9");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-sp");

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-both");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub1);
		subs.add(sub2);

		Neighbour neigh = new Neighbour("neigh-both", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub1.getQueueName())).isTrue();
		assertThat(client.queueExists(sub2.getQueueName())).isTrue();
	}

	@Test
	public void setupRoutingWithCapabilityExchanges() throws Exception {
		LocalDelivery delivery = new LocalDelivery(
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
				LocalDeliveryStatus.CREATED
		);
		String deliveryExchangeName = "del-ex10";

		DenmCapability cap = new DenmCapability(
				"NO-123",
				"NO",
				"DENM:1.2.2",
				new HashSet<>(Arrays.asList("12004")),
				new HashSet<>(Arrays.asList("6"))
		);
		cap.setRedirect(RedirectStatus.OPTIONAL);
		cap.setCapabilityExchangeName("cap-ex10");
		client.createTopicExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(cap)));

		MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
		String capabilitySelector = creator.makeSelector(cap);

		String joinedSelector = String.format("(%s) AND (%s)", delivery.getSelector(), capabilitySelector);

		client.createDirectExchange(deliveryExchangeName);
		client.addWriteAccess(sp.getName(), deliveryExchangeName);
		client.bindDirectExchange(joinedSelector, deliveryExchangeName, cap.getCapabilityExchangeName());

		NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'", NeighbourSubscriptionStatus.ACCEPTED, "neigh10");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub);

		Neighbour neigh = new Neighbour("neigh10", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();

		AtomicInteger numMessages = new AtomicInteger();
		try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
				sub.getQueueName(),
				sslContext,
				message -> numMessages.incrementAndGet())) {
			sink.start();
			try (Source source = new Source(qpidContainer.getAmqpsUrl(),deliveryExchangeName,sslContext)) {
				source.start();
				String messageText = "This is my DENM message :) ";
				byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
				source.sendNonPersistentMessage(source.createMessageBuilder()
						.bytesMessage(bytemessage)
						.userId("kong_olav")
						.publisherId("NO-123")
						.messageType(Constants.DENM)
						.causeCode("6")
						.subCauseCode("61")
						.originatingCountry("NO")
						.protocolVersion("DENM:1.2.2")
						.quadTreeTiles(",12004,")
						.timestamp(System.currentTimeMillis())
						.build());
				System.out.println();
			}
			System.out.println();
			Thread.sleep(200);
		}
		assertThat(numMessages.get()).isEqualTo(1);
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