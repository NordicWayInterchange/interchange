package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
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
import no.vegvesen.ixn.federation.service.SubscriptionCapabilityMatchDiscoveryService;
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
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
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



	public static KeysStructure keysStructure = generateKeys(RoutingConfigurerIT.class,"my_ca", "localhost", "routing_configurer", "king_gustaf", "nordea");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", keysStructure,"localhost");

	@Autowired
	SSLContext sslContext;

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurerIT.class);

	private final SubscriptionRequest emptySubscriptionRequest = new SubscriptionRequest(emptySet());
	private final NeighbourCapabilities emptyNeighbourCapabilities = new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet());

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
					"test.ssl.trust-store=" + keysStructure.getKeysOutputPath().resolve("truststore.jks"),
					"test.ssl.key-store=" +  keysStructure.getKeysOutputPath().resolve("routing_configurer.p12")
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

	@MockBean
    SubscriptionCapabilityMatchDiscoveryService subscriptionCapabilityMatchDiscoveryService;

	@Test
	public void subscriptionIsSetToResubscribeWhenNewMatchExists(){
		Neighbour nb = new Neighbour();
		nb.setNeighbourRequestedSubscriptions(new NeighbourSubscriptionRequest(Set.of(new NeighbourSubscription("originatingCountry='NO'",NeighbourSubscriptionStatus.CREATED))));
		when(neighbourService.findAllNeighbours()).thenReturn(List.of(nb));
		when(subscriptionCapabilityMatchDiscoveryService.newMatchExists(any())).thenReturn(true);
		neighbourService.saveNeighbour(nb);

		routingConfigurer.handleNewMatches();
		assertThat(nb.getNeighbourRequestedSubscriptions().getSubscriptions().stream().filter(a->a.getSubscriptionStatus().equals(NeighbourSubscriptionStatus.RESUBSCRIBE))).hasSize(1);
	}

	@Test
	public void neighbourWithOneBindingIsCreated() {
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex20", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex20");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription subscription = new NeighbourSubscription(
				"(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "flounder");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(subscription);

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

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

		Capability cap1 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex11");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex12", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		Capability cap2 = new Capability(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex12");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(cap1, cap2))));

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
		Neighbour halibut = new Neighbour("halibut", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

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

		Capability cap1 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex32");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex13", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		Capability cap2 = new Capability(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex13");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(cap1, cap2))));

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
		Neighbour salmon = new Neighbour("salmon", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

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

	@Test
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		ServiceProvider serviceProvider = new ServiceProvider("my-sp");
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex34", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		Capability cap = new Capability(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex34");

		serviceProvider.setCapabilities(new Capabilities(
				Collections.singleton(cap)
		));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription neighbourSub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'SE' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'SE-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tore-down-neighbour");
		subs.add(neighbourSub);

		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyNeighbourCapabilities, new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);
		when(serviceProviderRouter.findServiceProviders()).thenReturn(Collections.singleton(serviceProvider));
		when(neighbourService.getNodeName()).thenReturn("my-node");
		when(neighbourService.getMessagePort()).thenReturn("5671");
		routingConfigurer.setupNeighbourRouting(toreDownNeighbour, client.getQpidDelta());
		assertThat(client.getGroupMember(toreDownNeighbour.getName(),QpidClient.FEDERATED_GROUP_NAME)).isNotNull();

		neighbourSub.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN);

		routingConfigurer.tearDownNeighbourRouting(toreDownNeighbour);
		assertThat(client.getGroupMember(toreDownNeighbour.getName(),QpidClient.FEDERATED_GROUP_NAME)).isNull();
	}


	@Test
	public void addingOneSubscriptionResultsInOneBindKey() {
		Metadata metadata = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard = new Shard(1, "cap-ex1", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex1");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "hammershark");
		subs.add(sub);

		Neighbour hammershark = new Neighbour("hammershark", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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

		Capability cap1 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex2");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex3", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		Capability cap2 = new Capability(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex3");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Set.of(cap1, cap2)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub1 = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub1);

		Neighbour tigershark = new Neighbour("tigershark", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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

		Capability cap1 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex4");

		Metadata metadata2 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard2 = new Shard(1, "cap-ex5", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		Capability cap2 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122"),
						"RoadBlock",
						"publisherName"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex5");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Set.of(cap1, cap2)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");
		subs.add(sub);

		Neighbour tigershark = new Neighbour("tigershark", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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

		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex6");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		NeighbourSubscription sub = new NeighbourSubscription("(quadTree like '%,01230123%' OR quadTree like '%,01230122%') " +
				"AND publicationType = 'RoadBlock' " +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO' " +
				"AND protocolVersion = '1.0' " +
				"AND publisherId = 'NO-1234'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		subs.add(sub);

		Neighbour neigh = new Neighbour("negih-true", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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

		Capability cap1 = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex7");

		Metadata metadata2 = new Metadata(RedirectStatus.NOT_AVAILABLE);
		Shard shard2 = new Shard(1, "cap-ex8", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		Capability cap2 = new Capability(
				new DatexApplication(
						"SE-1234",
						"pub-1",
						"SE",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex8");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Set.of(cap1, cap2)));

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

		Neighbour neigh = new Neighbour("neigh-true-and-false", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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

		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
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
		Neighbour cod = new Neighbour("cod", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setCapabilities(new Capabilities(singleton(cap)));

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
		Neighbour clownfish = new Neighbour("clownfish", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

		routingConfigurer.setupNeighbourRouting(clownfish, client.getQpidDelta());
		assertThat(client.queueExists(clownfish.getName())).isFalse();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex9", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						"pub-1",
						"NO",
						"1.0",
						List.of("01230122", "01230123"),
						"RoadBlock",
						"publisherName"
				),
				metadata
		);
		client.createHeadersExchange("cap-ex9");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

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

		Neighbour neigh = new Neighbour("neigh-both", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
				LocalDeliveryStatus.CREATED
		);
		String deliveryExchangeName = "del-ex10";

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex10", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		Capability cap = new Capability(
				new DenmApplication(
						"NO-123",
						"pub-1",
						"NO",
						"DENM:1.2.2",
						List.of("12004"),
						List.of(6)
				),
				metadata
		);
		client.createHeadersExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
		String capabilitySelector = creator.makeSelector(cap);

		String joinedSelector = String.format("(%s) AND (%s)", delivery.getSelector(), capabilitySelector);
		System.out.println(joinedSelector);

		client.createDirectExchange(deliveryExchangeName);
		client.addWriteAccess(sp.getName(), deliveryExchangeName);
		client.addBinding(deliveryExchangeName, new Binding(deliveryExchangeName, cap.getMetadata().getShards().get(0).getExchangeName(), new Filter(joinedSelector)));

		NeighbourSubscription sub = new NeighbourSubscription("originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6", NeighbourSubscriptionStatus.ACCEPTED, "neigh10");

		HashSet<NeighbourSubscription> subs = new HashSet<>();
		subs.add(sub);

		Neighbour neigh = new Neighbour("neigh10", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(subs), emptySubscriptionRequest);

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
						.causeCode(6)
						.subCauseCode(61)
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

		Endpoint endpoint1 = new Endpoint("my-source-1", "host-1", 5671, new SubscriptionShard("target"));
		Endpoint endpoint2 = new Endpoint("my-source-2", "host-2", 5671, new SubscriptionShard("target"));

		Set<Endpoint> endpoints = new HashSet<>(org.mockito.internal.util.collections.Sets.newSet(endpoint1, endpoint2));

		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "my-source-1", "my-neighbour")).thenReturn(null);
		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName("target", "my-source-2", "my-neighbour")).thenReturn(null);

		ListenerEndpoint listenerEndpoint1 = new ListenerEndpoint("my-neighbour", "my-source-1", "host-1", 5671, new Connection(), "target");
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint("my-neighbour", "my-source-2", "host-2", 5671, new Connection(), "target");

		when(listenerEndpointRepository.save(listenerEndpoint1)).thenReturn(listenerEndpoint1);
		when(listenerEndpointRepository.save(listenerEndpoint2)).thenReturn(listenerEndpoint2);

		routingConfigurer.createListenerEndpoint("host-1", 5671, "my-source-1", "target", "my-neighbour");
		routingConfigurer.createListenerEndpoint("host-2", 5671, "my-source-2", "target", "my-neighbour");

		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void subscriptionShardIsNotSetUpWhenEndpointIsMissing() {
		String selector = "a=b";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
		subscription.setConsumerCommonName("my-node");

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(listenerEndpointRepository.save(any())).thenReturn(new ListenerEndpoint());
		routingConfigurer.setUpSubscriptionExchanges();

		assertThat(subscription.getEndpoints()).isEmpty();
		verify(listenerEndpointRepository, times(0)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void setUpSubscriptionShardExchange() {
		String selector = "a=b";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
		subscription.setConsumerCommonName("my-node");
		subscription.setEndpoints(Collections.singleton(new Endpoint("my-source", "my-host", 5671)));

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(listenerEndpointRepository.save(any())).thenReturn(new ListenerEndpoint());
		routingConfigurer.setUpSubscriptionExchanges();

		assertThat(subscription.getEndpoints().stream().findFirst().get().hasShard()).isTrue();
		assertThat(client.exchangeExists(subscription.getEndpoints().stream().findFirst().get().getShard().getExchangeName())).isTrue();
		verify(listenerEndpointRepository, times(1)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void multipleEndpointsGetTheirOwnShardAndListenerEndpointsAreCreated() {
		String selector = "a=b";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED);
		subscription.setConsumerCommonName("my-node");

		Endpoint end1 = new Endpoint("my-source1", "my-host", 5671);
		Endpoint end2 = new Endpoint("my-source2", "my-host", 5671);

		subscription.setEndpoints(new HashSet<>(Arrays.asList(end1, end2)));

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(listenerEndpointRepository.save(any())).thenReturn(new ListenerEndpoint());
		routingConfigurer.setUpSubscriptionExchanges();

		assertThat(end1.hasShard()).isTrue();
		assertThat(end2.hasShard()).isTrue();
		assertThat(client.exchangeExists(end1.getShard().getExchangeName())).isTrue();
		assertThat(client.exchangeExists(end2.getShard().getExchangeName())).isTrue();
		verify(listenerEndpointRepository, times(2)).save(any(ListenerEndpoint.class));
	}

	@Test
	public void tearDownSubscriptionShardExchange() {
		String selector = "a=b";
		String exchangeName = "subscription-exchange";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);
		subscription.setEndpoints(Collections.singleton(new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName))));
		subscription.setConsumerCommonName("my-node");

		client.createHeadersExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");

		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscription.getEndpoints().isEmpty()).isTrue();
		assertThat(client.exchangeExists(exchangeName)).isFalse();
	}

	@Test
	public void multipleEndpointsAndSubscriptionShardExchangesAreRemoved() {
		String selector = "originatingCountry = 'NO'";
		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);

		Endpoint end1 = new Endpoint(
				"my-source-1",
				"my-host",
				5671,
				new SubscriptionShard("exchange1")
		);

		Endpoint end2 = new Endpoint(
				"my-source-2",
				"my-host",
				5671,
				new SubscriptionShard("exchange2")
		);

		subscription.setEndpoints(new HashSet<>(List.of(end1, end2)));
		subscription.setConsumerCommonName("my-node");

		client.createHeadersExchange("exchange1");
		client.createHeadersExchange("exchange2");

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");

		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscription.getEndpoints().isEmpty()).isTrue();
		assertThat(client.exchangeExists("exchange1")).isFalse();
		assertThat(client.exchangeExists("exchange2")).isFalse();
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
	public void teardownSubscriptionWhereEndpointHasNoShard() {
		Subscription subscritption = new Subscription(
				SubscriptionStatus.TEAR_DOWN,
				"a = b",
				"/subscriptoins/foo",
				"my-node",
				Set.of(
						new Endpoint(
								"source",
								"host",
								1234
						)
				)
		);
		Neighbour neighbour = new Neighbour(
				"neighbour",
				new NeighbourCapabilities(),
				new NeighbourSubscriptionRequest(),
				new SubscriptionRequest(
						Set.of(
								subscritption
						)
				)
		);
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(neighbourService.findAllNeighbours()).thenReturn(List.of(neighbour));
		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscritption.getEndpoints()).isEmpty();
		verify(neighbourService,times(1)).saveNeighbour(neighbour);
	}

	@Test
	public void subscriptionExchangeAndSubscriptionShardIsRemovedWhenSubscriptionHasStatusFailed() {
		String exchangeName = "failed-exchange";
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		Endpoint endpoint = new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));
		subscription.setConsumerCommonName("my-node");

		client.createHeadersExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(anyString(), anyString(), anyString())).thenReturn(null);

		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscription.getEndpoints().isEmpty()).isFalse();
		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(endpoint.hasShard()).isFalse();
	}

	@Test
	public void subscriptionExchangeAndSubscriptionShardIsNotRemovedWhenSubscriptionHasStatusFailedAndListenerEndpointExists() {
		String exchangeName = "failed-exchange-with-listener-endpoint";
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.FAILED);
		Endpoint endpoint = new Endpoint("my-source", "my-host", 5671, new SubscriptionShard(exchangeName));
		subscription.setEndpoints(Collections.singleton(endpoint));
		subscription.setConsumerCommonName("my-node");

		client.createHeadersExchange(exchangeName);

		Neighbour myNeighbour = new Neighbour();
		myNeighbour.setName("my-neighbour");
		myNeighbour.setOurRequestedSubscriptions(new SubscriptionRequest(singleton(subscription)));

		when(neighbourService.findAllNeighbours()).thenReturn(Arrays.asList(myNeighbour));
		when(interchangeNodeProperties.getName()).thenReturn("my-node");
		when(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(exchangeName, "my-source", "my-neighbour"))
				.thenReturn(new ListenerEndpoint("my-neighbour", "my-source", "my-host", 5672, new Connection(), exchangeName));

		routingConfigurer.tearDownSubscriptionExchanges();
		assertThat(subscription.getEndpoints().isEmpty()).isFalse();
		assertThat(client.exchangeExists(exchangeName)).isTrue();
		assertThat(endpoint.hasShard()).isTrue();
	}

	@Test
	public void setupRegularRoutingWithNonExistingExchangeKeepsTheSubscriptionUnchanged() {
		Capability denmCapability = new Capability(
				new DenmApplication(
						"NO0000",
						"NO0000:001",
						"NO",
						"1.0",
						List.of("0122"),
						List.of(1)
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
				new NeighbourCapabilities(),
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
				new NeighbourCapabilities(),
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
				new NeighbourCapabilities(),
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
				new NeighbourCapabilities(),
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
				new NeighbourCapabilities(),
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
				new KeystoreDetails(keysStructure.getKeysOutputPath().resolve(s).toString(), "password", KeystoreType.PKCS12),
				new KeystoreDetails(keysStructure.getKeysOutputPath().resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}
