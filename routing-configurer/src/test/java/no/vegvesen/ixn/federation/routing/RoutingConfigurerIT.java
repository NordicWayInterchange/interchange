package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.MessageValidatingSelectorCreator;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
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
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
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
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex20", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex20");

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

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(flounder, client.getQpidDelta());
		assertThat(client.queueExists(subscription.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(subscription.getLastUpdatedTimestamp()).isGreaterThan(0);
	}

	@Test
	public void neighbourWithTwoBindingsIsCreated() {
		Metadata metadata1 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard1 = new Shard(1, "cap-ex11", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex11");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex12", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex12");

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
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(halibut, client.getQpidDelta());
		assertThat(client.queueExists(s1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.queueExists(s2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isGreaterThan(0);
	}

	@Test
	public void neighbourWithTwoBindingsAndOnlyOneIsAcceptedIsCreated() {
		Metadata metadata1 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard1 = new Shard(1, "cap-ex32", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex32");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex13", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex13");

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
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour salmon = new Neighbour("salmon", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(salmon, client.getQpidDelta());
		assertThat(client.queueExists(s1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		List<Binding> queueBindKeys = client.getQueuePublishingLinks(s1.getEndpoints().stream().findFirst().get().getSource());
		assertThat(queueBindKeys).hasSize(1);
		assertThat(s2.getEndpoints()).isEmpty();

		Set<NeighbourSubscription> createdSubscriptions = salmon.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.CREATED);
		assertThat(createdSubscriptions).hasSize(1);

		//Showing that the timestamp have been changed for the ACCEPTED subscription, but not for the REJECTED one
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);
	}

	//TODO this test does not really make any sense. We no longer have incoming exchange (we do this through
	//separate capability exchanges, and messageCollector. This test is old as ...
	@Test
	@Disabled("This test does no longer make any sense!")
	public void newNeighbourCanNeitherWriteToIncomingExchangeNorOnramp() {
		Metadata metadata = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard = new Shard(1, "cap-ex14", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex14");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		HashSet<NeighbourSubscription> subscriptions = new HashSet<>();
		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'",
				NeighbourSubscriptionStatus.ACCEPTED,
				"nordea");
		subscriptions.add(subscription);

		Neighbour nordea = new Neighbour(
				"nordea",
				new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()),
				new NeighbourSubscriptionRequest(subscriptions),
				null);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(nordea, client.getQpidDelta());
		SSLContext nordeaSslContext = setUpTestSslContext("nordea.p12");
		Source writeIncomingExchange = new Source(AMQPS_URL, "incomingExchange", nordeaSslContext);
		try {
			writeIncomingExchange.start();
		} catch (NamingException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
		JmsMessage message = null;
		try {
			message = writeIncomingExchange
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
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		try {
			writeIncomingExchange.send(message);
			fail("Should not allow neighbour nordea to write on (incomingExchange)");
		} catch (JMSException e) {
			//This, apparently, should fail...
			//throw new RuntimeException(e);
		}
			Source writeOnramp = new Source(AMQPS_URL, "onramp", nordeaSslContext);
		try {
			writeOnramp.start();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		try {
			message = writeOnramp.createMessageBuilder()
						.textMessage("Make Nordea great again!")
						.messageType(Constants.DATEX_2)
						.publisherId("NO-123")
						.publicationId("pub-1")
						.publicationType("Test")
						.quadTreeTiles(",123,")
						.originatingCountry("NO")
						.protocolVersion("1.0")
						.build();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		try {
			writeOnramp.send(message);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}

		fail("Should not allow nordea to write on (onramp)");
		try {
			theNodeItselfCanReadFromAnyNeighbourQueue(subscription.getEndpoints().stream().findFirst().get().getSource());
		} catch (NamingException e) {
			throw new RuntimeException(e);
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@Disabled("Have to write tear down all over again, does not do what it should atm")
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tore-down-neighbour"));

		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyCapabilities, new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);
		routingConfigurer.setupNeighbourRouting(toreDownNeighbour, client.getQpidDelta());
		assertThat(client.getGroupMember(toreDownNeighbour.getName(),QpidClient.FEDERATED_GROUP_NAME)).isNull();

		subs.clear();
		subs.add(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'Road Block' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.TEAR_DOWN, "tore-down-neighbour"));

		routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMember(toreDownNeighbour.getName(),QpidClient.FEDERATED_GROUP_NAME)).isNull();
	}


	@Test
	public void addingOneSubscriptionResultsInOneBindKey() {
		Metadata metadata = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard = new Shard(1, "cap-ex1", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex1");

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

		Neighbour hammershark = new Neighbour("hammershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(neighbourService.getMessagePort()).thenReturn("5671");
		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));

		routingConfigurer.setupNeighbourRouting(hammershark, client.getQpidDelta());
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
	}

	@Test
	public void addingTwoSubscriptionsResultsInTwoBindKeys() {
		Metadata metadata1 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard1 = new Shard(1, "cap-ex2", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex2");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex3", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex3");

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

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(neighbourService.getMessagePort()).thenReturn("5671");
		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		routingConfigurer.setupNeighbourRouting(tigershark, client.getQpidDelta());
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);

		NeighbourSubscription sub2 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub2);

		tigershark.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(subs));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(tigershark, client.getQpidDelta());
		assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub2.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(2);
	}

	@Test
	public void addingTwoSubscriptionsAndOneCapabilityResultsInTwoBindKeys() {
		Metadata metadata1 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard1 = new Shard(1, "cap-ex4", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230123")),
						"RoadBlock"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex4");

		Metadata metadata2 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard2 = new Shard(1, "cap-ex5", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122")),
						"RoadBlock"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex5");

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

		Neighbour tigershark = new Neighbour("tigershark", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(tigershark, client.getQpidDelta());
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(2);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void setUpQueueForServiceProvider() {
		Metadata metadata = new Metadata(RedirectStatus.MANDATORY);
		Shard shard = new Shard(1, "cap-ex6", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex6");

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

		Neighbour neigh = new Neighbour("negih-true", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(neighbourService.getMessagePort()).thenReturn("5671");
		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbour() {
		Metadata metadata1 = new Metadata(RedirectStatus.MANDATORY);
		Shard shard1 = new Shard(1, "cap-ex7", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit cap1 = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex7");

		Metadata metadata2 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard2 = new Shard(1, "cap-ex8", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit cap2 = new CapabilitySplit(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex8");

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

		Neighbour neigh = new Neighbour("neigh-true-and-false", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
	}

	@Test
	public void routingIsNotSetUpWhenTryingToRedirect() {
		Metadata metadata = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard = new Shard(1, "cap-ex8", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider"));

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour cod = new Neighbour("cod", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(serviceProvider));
		routingConfigurer.setupNeighbourRouting(cod, client.getQpidDelta());
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

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour clownfish = new Neighbour("clownfish", emptyCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(clownfish, client.getQpidDelta());
		assertThat(client.queueExists(clownfish.getName())).isFalse();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex9", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("01230122", "01230123")),
						"RoadBlock"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex9");

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

		Neighbour neigh = new Neighbour("neigh-both", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
	}

	@Test
	public void setupRoutingWithCapabilityExchanges() throws Exception {
		LocalDelivery delivery = new LocalDelivery(
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
				LocalDeliveryStatus.CREATED
		);
		String deliveryExchangeName = "del-ex10";

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex10", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit cap = new CapabilitySplit(
				new DenmApplication(
						"NO-123",
						"pub-1",
						"NO",
						"DENM:1.2.2",
						new HashSet<>(Arrays.asList("12004")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata
		);
		client.createHeadersExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, singleton(cap)));

		MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
		String capabilitySelector = creator.makeSelector(cap);

		String joinedSelector = String.format("(%s) AND (%s)", delivery.getSelector(), capabilitySelector);

		client.createDirectExchange(deliveryExchangeName);
		client.addWriteAccess(sp.getName(), deliveryExchangeName);
		client.addBinding(deliveryExchangeName, new Binding(deliveryExchangeName, cap.getMetadata().getShards().get(0).getExchangeName(), new Filter(joinedSelector)));

		NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'", NeighbourSubscriptionStatus.ACCEPTED, "neigh10");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub);

		Neighbour neigh = new Neighbour("neigh10", new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();

		AtomicInteger numMessages = new AtomicInteger();
		try (Sink sink = new Sink(qpidContainer.getAmqpsUrl(),
				sub.getEndpoints().stream().findFirst().get().getSource(),
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
						.shardId(1)
						.shardCount(1)
						.timestamp(System.currentTimeMillis())
						.build());
			}
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

		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "my-source-1", "my-neighbour")).thenReturn(null);
		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "my-source-2", "my-neighbour")).thenReturn(null);

		ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("my-neighbour", "my-source-1", "host-1", 5671, new Connection(), "target");
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("my-neighbour", "my-source-2", "host-2", 5671, new Connection(), "target");

		when(listenerEndpointRepository.save(listenerEndpoint1)).thenReturn(listenerEndpoint1);
		when(listenerEndpointRepository.save(listenerEndpoint2)).thenReturn(listenerEndpoint2);

		routingConfigurer.createListenerEndpointFromEndpointsList(neighbour, endpoints, "target");

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

		client.createHeadersExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");

		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscription.exchangeIsRemoved()).isTrue();
		assertThat(client.exchangeExists(exchangeName)).isFalse();
	}

	@Test
	public void subscriptionExchangeIsNotRemovedWhenSubscriptionIsDeleted() {
		String exchangeName = "subscription-exchange-one";

		client.createHeadersExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");

		routingConfigurer.tearDownSubscriptionExchanges();

		assertThat(client.exchangeExists(exchangeName)).isTrue();
	}

	@Test
	public void setupRegularRoutingWithNonExistingExchangeKeepsTheSubscriptionUnchanged() {
		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NO0000",
						"NO0000:001",
						"NO",
						"1.0",
						Collections.emptySet(),
						Collections.singleton(1)
				),
				new Metadata(RedirectStatus.NOT_AVAILABLE)
		);
		NeighbourSubscription neighbourSubscription = new NeighbourSubscription(
				"publisherId = 'NO0000'",
				NeighbourSubscriptionStatus.ACCEPTED
		);

		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getHost());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setUpRegularRouting(
				Collections.singleton(neighbourSubscription),
				Collections.singleton(denmCapability),
				"my_Neighbour",
				client.getQpidDelta()
		);
		assertThat(neighbourSubscription.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.ACCEPTED);
		assertThat(neighbourSubscription.getEndpoints()).isEmpty();
	}

	@Test
	public void tearDownSubscriptionShouldRemoveAclForQueue() {
		String neighbourName = "neighbour";
		String queueName = UUID.randomUUID().toString();
		NeighbourEndpoint endpoint = new NeighbourEndpoint(
				queueName,
				"hostName",
				5671
		);
		NeighbourSubscription subscription = new NeighbourSubscription(
				"a = b",
				NeighbourSubscriptionStatus.TEAR_DOWN,
				neighbourName
		);
		subscription.setEndpoints(Collections.singleton(endpoint));
		Neighbour neighbour = new Neighbour(
				neighbourName,
				new Capabilities(),
				new NeighbourSubscriptionRequest(
						Collections.singleton(subscription)
				),
				new SubscriptionRequest()
		);
		Queue queue = client.createQueue(queueName);
		client.addMemberToGroup(neighbourName,QpidClient.FEDERATED_GROUP_NAME);
		client.addReadAccess(neighbourName,queue.getName());
		routingConfigurer.tearDownNeighbourRouting(neighbour);
		assertThat(client.getGroupMember(neighbourName,QpidClient.FEDERATED_GROUP_NAME)).isNull();
		assertThat(client
				.getQpidAcl()
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourName,queue.getName())
				)
		).isFalse();
		assertThat(client.getQueue(queue.getName())).isNull();
	}

	@Test
	public void tearDownSubscriptionShouldNotRemoveNeighbourFromGroupIfOtherSubsExist() {
		String neighbourName = "non-redirect-neighbour";
		String queueName = UUID.randomUUID().toString();
		NeighbourEndpoint endpoint = new NeighbourEndpoint(
				queueName,
				"hostName",
				5671
		);
		NeighbourSubscription subscription = new NeighbourSubscription(
				"a = b",
				NeighbourSubscriptionStatus.TEAR_DOWN,
				neighbourName);
		subscription.setEndpoints(Collections.singleton(endpoint));
		String nonTeardownQueueName = "non-teardown-queue";
		NeighbourEndpoint nonTearDownEndpoint = new NeighbourEndpoint(
				nonTeardownQueueName,
				"hostName",
				5671
		);
		NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
				"c = d",
				NeighbourSubscriptionStatus.CREATED,
				neighbourName);
		nonTearDownSubscription.setEndpoints(Collections.singleton(nonTearDownEndpoint));
		Neighbour neighbour = new Neighbour(
				neighbourName,
				new Capabilities(),
				new NeighbourSubscriptionRequest(
						new HashSet<>(Arrays.asList(subscription,nonTearDownSubscription))
				),
				new SubscriptionRequest()
		);
		Queue queue = client.createQueue(queueName);
		Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
		client.addMemberToGroup(neighbourName,QpidClient.FEDERATED_GROUP_NAME);
		client.addReadAccess(neighbourName,queue.getName());
		client.addReadAccess(neighbourName,nonTeardownQueue.getName());

		routingConfigurer.tearDownNeighbourRouting(neighbour);


		assertThat(client.getGroupMember(neighbourName,QpidClient.FEDERATED_GROUP_NAME)).isNotNull();
		VirtualHostAccessController qpidAcl = client.getQpidAcl();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourName,queue.getName())
				)
		).isFalse();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourName,nonTeardownQueue.getName())
				)
		).isTrue();
		assertThat(client.getQueue(queue.getName())).isNull();
		assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
	}

	@Test
	public void tearDownRedirectedSubscriptionShouldRemoveAclForQueue() {
		String neighbourSPName = "neighbour-service-provider";
		String queueName = UUID.randomUUID().toString();
		NeighbourEndpoint endpoint = new NeighbourEndpoint(
				queueName,
				"hostName",
				5671
		);
		NeighbourSubscription subscription = new NeighbourSubscription(
				"a = b",
				NeighbourSubscriptionStatus.TEAR_DOWN,
				neighbourSPName);
		subscription.setEndpoints(Collections.singleton(endpoint));
		String neighbourName = "redirect-neighbour";
		Neighbour neighbour = new Neighbour(
				neighbourName,
				new Capabilities(),
				new NeighbourSubscriptionRequest(
						Collections.singleton(subscription)
				),
				new SubscriptionRequest()
		);
		Queue queue = client.createQueue(queueName);
		client.addMemberToGroup(neighbourName,QpidClient.FEDERATED_GROUP_NAME);
		client.addReadAccess(neighbourSPName,queue.getName());
		client.addMemberToGroup(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME);
		routingConfigurer.tearDownNeighbourRouting(neighbour);
		assertThat(client.getGroupMember(neighbourName,QpidClient.FEDERATED_GROUP_NAME)).isNull();
		assertThat(client
				.getQpidAcl()
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourSPName,queue.getName())
				)
		).isFalse();
		assertThat(client.getGroupMember(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNull();
		assertThat(client.getQueue(queue.getName())).isNull();
	}

	@Test
	public void tearDownRedirectedSubscriptionShouldNotRemoveSPFromGroupIfOtherRedirectSubsExist() {
		String neighbourSPName = "neighbour-non-teardown-service-provider-1";
		String queueName = UUID.randomUUID().toString();
		NeighbourEndpoint endpoint = new NeighbourEndpoint(
				queueName,
				"hostName",
				5671
		);
		NeighbourSubscription subscription = new NeighbourSubscription(
				"a = b",
				NeighbourSubscriptionStatus.TEAR_DOWN,
				neighbourSPName);
		subscription.setEndpoints(Collections.singleton(endpoint));
		String nonTeardownQueueName = "non-teardown-redirected-queue";
		NeighbourEndpoint endpointNonTearDown = new NeighbourEndpoint(
				nonTeardownQueueName,
				"hostName",
				5671
		);
		NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
				"c = d",
				NeighbourSubscriptionStatus.CREATED,
				neighbourSPName);
		nonTearDownSubscription.setEndpoints(Collections.singleton(endpointNonTearDown));
		String neighbourName = "redirect-neighbour";
		Neighbour neighbour = new Neighbour(
				neighbourName,
				new Capabilities(),
				new NeighbourSubscriptionRequest(
						new HashSet<>(Arrays.asList(subscription,nonTearDownSubscription))
				),
				new SubscriptionRequest()
		);
		Queue queue = client.createQueue(queueName);
		Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
		client.addMemberToGroup(neighbourName,QpidClient.FEDERATED_GROUP_NAME);
		client.addReadAccess(neighbourSPName,queue.getName());
		client.addReadAccess(neighbourSPName,nonTeardownQueue.getName());
		client.addMemberToGroup(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		routingConfigurer.tearDownNeighbourRouting(neighbour);


		assertThat(client.getGroupMember(neighbourName,QpidClient.FEDERATED_GROUP_NAME)).isNotNull();
		VirtualHostAccessController qpidAcl = client.getQpidAcl();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourSPName,queue.getName())
				)
		).isFalse();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourSPName,nonTeardownQueue.getName())
				)
		).isTrue();
		assertThat(client.getGroupMember(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(client.getQueue(queue.getName())).isNull();
		assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
	}

	@Test
	public void tearDownRedirectedSubscriptionShouldNotAffectOtherRedirectedSubscriptions() {
		String neighbourSPName = "neighbour-service-provider-teardown-x";
		String otherNeighbourSPName = "other-neighbour-service-provider";
		String queueName = UUID.randomUUID().toString();
		NeighbourEndpoint endpoint = new NeighbourEndpoint(
				queueName,
				"hostName",
				5671
		);
		NeighbourSubscription subscription = new NeighbourSubscription(
				"a = b",
				NeighbourSubscriptionStatus.TEAR_DOWN,
				neighbourSPName);
		subscription.setEndpoints(Collections.singleton(endpoint));
		String nonTeardownQueueName = "non-teardown-redirected-queue-x";
		NeighbourEndpoint endpointNonTearDown = new NeighbourEndpoint(
				nonTeardownQueueName,
				"hostName",
				5671
		);
		NeighbourSubscription nonTearDownSubscription = new NeighbourSubscription(
				"c = d",
				NeighbourSubscriptionStatus.CREATED,
				otherNeighbourSPName);
		nonTearDownSubscription.setEndpoints(Collections.singleton(endpointNonTearDown));
		String neighbourName = "redirect-neighbour-xx";
		Neighbour neighbour = new Neighbour(
				neighbourName,
				new Capabilities(),
				new NeighbourSubscriptionRequest(
						new HashSet<>(Arrays.asList(subscription,nonTearDownSubscription))
				),
				new SubscriptionRequest()
		);
		Queue queue = client.createQueue(queueName);
		Queue nonTeardownQueue = client.createQueue(nonTeardownQueueName);
		client.addMemberToGroup(neighbourName,QpidClient.FEDERATED_GROUP_NAME);
		client.addReadAccess(neighbourSPName,queue.getName());
		client.addReadAccess(otherNeighbourSPName,nonTeardownQueue.getName());
		client.addMemberToGroup(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME);
		client.addMemberToGroup(otherNeighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME);

		routingConfigurer.tearDownNeighbourRouting(neighbour);


		assertThat(client.getGroupMember(neighbourName,QpidClient.FEDERATED_GROUP_NAME)).isNotNull();
		VirtualHostAccessController qpidAcl = client.getQpidAcl();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(neighbourSPName,queue.getName())
				)
		).isFalse();
		assertThat(qpidAcl
				.containsRule(VirtualHostAccessController
						.createQueueReadAccessRule(otherNeighbourSPName,nonTeardownQueue.getName())
				)
		).isTrue();
		assertThat(client.getGroupMember(neighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNull();
		assertThat(client.getGroupMember(otherNeighbourSPName,QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(client.getQueue(queue.getName())).isNull();
		assertThat(client.getQueue(nonTeardownQueue.getName())).isNotNull();
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
