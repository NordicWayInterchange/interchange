package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.model.capability.DatexApplication;
import no.vegvesen.ixn.federation.model.capability.DenmApplication;
import no.vegvesen.ixn.federation.model.capability.Metadata;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
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
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


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

	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(emptySet());
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

	@MockBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockBean
	InterchangeNodeProperties interchangeNodeProperties;



	@Test
	public void neighbourWithOneBindingIsCreated() {
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap.setCapabilityExchangeName("cap-ex10");
		client.createDirectExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "flounder");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(subscription);

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(flounder);
		assertThat(client.queueExists(subscription.getQueueName())).isTrue();
		assertThat(subscription.getLastUpdatedTimestamp()).isGreaterThan(0);
	}

	@Test
	public void neighbourWithTwoBindingsIsCreated() {
		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap1.setCapabilityExchangeName("cap-ex11");
		client.createDirectExchange("cap-ex11");

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap2.setCapabilityExchangeName("cap-ex11");
		client.createDirectExchange("cap-ex11");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription s1 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		NeighbourSubscription s2 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'RoadBlock' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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
		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap1.setCapabilityExchangeName("cap-ex12");
		client.createDirectExchange("cap-ex12");

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap2.setCapabilityExchangeName("cap-ex13");
		client.createDirectExchange("cap-ex13");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription s1 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'RoadBlock' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "salmon");

		NeighbourSubscription s2 = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
						"AND publicationType = 'RoadBlock' " +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE' " +
						"AND protocolVersion = '1.0' " +
						"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.REJECTED, "salmon");

		//Just to ensure the timestamp is updated by RoutingConfigurer
		assertThat(s1.getLastUpdatedTimestamp()).isEqualTo(0);
		assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour salmon = new Neighbour("salmon", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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
	public void newNeighbourCanNeitherWriteToIncomingExchangeNorOnramp() throws JMSException, NamingException {
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap.setCapabilityExchangeName("cap-ex14");
		client.createDirectExchange("cap-ex14");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		HashSet<NeighbourSubscription> subscriptions = new HashSet<>();
		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "nordea");
		subscriptions.add(subscription);

		Neighbour nordea = new Neighbour(
				"nordea",
				new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()),
				new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions),
				null);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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
					.publicationId("pub-1")
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
					.publicationId("pub-1")
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
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap.setCapabilityExchangeName("cap-ex1");
		client.createDirectExchange("cap-ex1");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "hammershark");
		subs.add(sub);

		Neighbour hammershark = new Neighbour("hammershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));

		routingConfigurer.setupNeighbourRouting(hammershark);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub.getQueueName()).size()).isEqualTo(1);
	}

	@Test
	public void addingTwoSubscriptionsResultsInTwoBindKeys() {
		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap1.setCapabilityExchangeName("cap-ex2");
		client.createDirectExchange("cap-ex2");

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap2.setCapabilityExchangeName("cap-ex3");
		client.createDirectExchange("cap-ex3");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub1);

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.queueExists(sub1.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub1.getQueueName()).size()).isEqualTo(1);

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub2);

		tigershark.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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
		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap1.setCapabilityExchangeName("cap-ex4");
		client.createDirectExchange("cap-ex4");

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap2.setCapabilityExchangeName("cap-ex5");
		client.createDirectExchange("cap-ex5");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub);

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(tigershark);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
		assertThat(client.getQueueBindKeys(sub.getQueueName()).size()).isEqualTo(2);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void setUpQueueForServiceProvider() {
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.MANDATORY)
		);
		cap.setCapabilityExchangeName("cap-ex6");
		client.createDirectExchange("cap-ex6");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		subs.add(sub);

		Neighbour neigh = new Neighbour("negih-true", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub.getQueueName())).isTrue();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbour() {
		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.MANDATORY)
		);
		cap1.setCapabilityExchangeName("cap-ex7");
		client.createDirectExchange("cap-ex7");

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		cap2.setCapabilityExchangeName("cap-ex8");
		client.createDirectExchange("cap-ex8");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-true-and-false");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub1);
		subs.add(sub2);

		Neighbour neigh = new Neighbour("neigh-true-and-false", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(neigh);
		assertThat(client.queueExists(sub1.getQueueName())).isTrue();
		assertThat(client.queueExists(sub2.getQueueName())).isTrue();
	}

	@Test
	public void routingIsNotSetUpWhenTryingToRedirect() {
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider"));

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour cod = new Neighbour("cod", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(serviceProvider));
		routingConfigurer.setupNeighbourRouting(cod);
		assertThat(client.queueExists(cod.getName())).isFalse();
		assertThat(cod.getNeighbourRequestedSubscriptions().getSubscriptions().stream().findFirst().get().getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
	}

	@Test
	public void routingIsNotSetUpWhenRedirectIsNotAvailable() {
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
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
		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap.setCapabilityExchangeName("cap-ex9");
		client.createDirectExchange("cap-ex9");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-sp");

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-both");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub1);
		subs.add(sub2);

		Neighbour neigh = new Neighbour("neigh-both", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.REQUESTED, subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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

		CapabilitySplit cap = new CapabilitySplit(
				new DenmApplication(
						"NO-123",
						"pub-1",
						"NO",
						"DENM:1.2.2",
						new HashSet<>(Arrays.asList("12004")),
						new HashSet<>(Arrays.asList(6))
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		cap.setCapabilityExchangeName("cap-ex10");
		client.createTopicExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

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

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
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
						.publicationId("pub-1")
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

	@Test
	public void listenerEndpointsAreSavedFromEndpointsList() {
		Neighbour neighbour = new Neighbour();
		neighbour.setName("my-neighbour");

		Endpoint endpoint1 = new Endpoint("my-source-1", "host-1", 5671);
		Endpoint endpoint2 = new Endpoint("my-source-2", "host-2", 5671);

		Set<Endpoint> endpoints = new HashSet<>(org.mockito.internal.util.collections.Sets.newSet(endpoint1, endpoint2));

		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "host-1", 5671, "my-source-1")).thenReturn(null);
		when(listenerEndpointRepository.findByNeighbourNameAndHostAndPortAndSource("my-neighbour", "host-2", 5671, "my-source-2")).thenReturn(null);

		ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("my-neighbour", "my-source-1", "host-1", 5671, new Connection());
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("my-neighbour", "my-source-2", "host-2", 5671, new Connection());

		when(listenerEndpointRepository.save(listenerEndpoint1)).thenReturn(listenerEndpoint1);
		when(listenerEndpointRepository.save(listenerEndpoint2)).thenReturn(listenerEndpoint2);

		routingConfigurer.createListenerEndpointFromEndpointsList(neighbour, endpoints, "");

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void setUpSubscriptionExchange() {
		String selector = "a=b";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
		subscription.setConsumerCommonName("my-node");
		//subscription.setExchangeName("subscription-exchange");


		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		routingConfigurer.setUpSubscriptionExchanges();

		assertThat(client.exchangeExists(subscription.getExchangeName())).isTrue();
	}

	@Test
	public void tearDownSubscriptionExchange() {
		String selector = "a=b";
		String exchangeName = "subscription-exchange";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);
		subscription.setExchangeName(exchangeName);
		subscription.setConsumerCommonName("my-node");

		client.createTopicExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");

		routingConfigurer.tearDownSubscriptionExchanges();

		assertThat(subscription.exchangeIsRemoved()).isTrue();
		assertThat(client.exchangeExists(exchangeName)).isFalse();
	}

	public void theNodeItselfCanReadFromAnyNeighbourQueue(String neighbourQueue) throws NamingException, JMSException {
		SSLContext localhostSslContext = setUpTestSslContext("localhost.p12");
		Sink neighbourSink = new Sink(AMQPS_URL, neighbourQueue, localhostSslContext);
		neighbourSink.start();
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}