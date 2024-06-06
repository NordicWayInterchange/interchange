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
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
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

	@Test
	public void neighbourWithOneSubscriptionIsCreated() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex1");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex1");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription subscription = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "flounder");
		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(subscription);

		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour flounder = new Neighbour("flounder", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(flounder, client.getQpidDelta());
		assertThat(client.queueExists(subscription.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(subscription.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(subscription.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(subscription.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void neighbourWithTwoSubscriptionsIsCreated() {
		Capability cap1 = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex2");
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex2");

		Capability cap2 = getDatexCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex3");
		cap2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex3");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription s1 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		NeighbourSubscription s2 = new NeighbourSubscription("publicationId = 'pub-2'", NeighbourSubscriptionStatus.ACCEPTED, "halibut");

		Set<NeighbourSubscription> subscriptions = Sets.newLinkedHashSet(s1, s2);
		NeighbourSubscriptionRequest subscriptionRequest = new NeighbourSubscriptionRequest(subscriptions);
		Neighbour halibut = new Neighbour("halibut", emptyNeighbourCapabilities, subscriptionRequest, emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn(qpidContainer.getvHostName());
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(halibut, client.getQpidDelta());
		assertThat(client.queueExists(s1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(s1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(client.queueExists(s2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(s2.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(s2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void neighbourWithTwoSubscriptionsAndOnlyOneAcceptedIsCreated() {
		Capability cap1 = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex4");
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex4");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Collections.singleton(cap1)));

		NeighbourSubscription s1 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230123%'", NeighbourSubscriptionStatus.ACCEPTED, "salmon");

		NeighbourSubscription s2 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230122%'", NeighbourSubscriptionStatus.ILLEGAL, "salmon");

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
		assertThat(client.getQueuePublishingLinks(s1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(s2.getEndpoints()).isEmpty();
		assertThat(s1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(s2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.ILLEGAL);

		//Showing that the timestamp have been changed for the ACCEPTED subscription, but not for the REJECTED one
		assertThat(s1.getLastUpdatedTimestamp()).isGreaterThan(0);
		assertThat(s2.getLastUpdatedTimestamp()).isEqualTo(0);
	}

	@Test
	public void neighbourToreDownWillBeRemovedFromFederatedInterchangesGroup() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex5");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex5");

		ServiceProvider serviceProvider = new ServiceProvider("my-sp");
		serviceProvider.setCapabilities(new Capabilities(Collections.singleton(cap)));

		NeighbourSubscription neighbourSub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "tore-down-neighbour");

		Neighbour toreDownNeighbour = new Neighbour("tore-down-neighbour", emptyNeighbourCapabilities, new NeighbourSubscriptionRequest(Collections.singleton(neighbourSub)), emptySubscriptionRequest);
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
	public void addingOneSubscriptionAndTwoCapabilitiesResultsInTwoEndpoints() {
		Capability cap1 = getDatexCapability("pub-1", RedirectStatus.NOT_AVAILABLE, "cap-ex6");
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex6");

		Capability cap2 = getDatexCapability("pub-2", RedirectStatus.NOT_AVAILABLE, "cap-ex7");
		cap2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex7");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(Set.of(cap1, cap2)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' OR publicationId = 'pub-2'", NeighbourSubscriptionStatus.ACCEPTED, "tigershark");

		Neighbour tigershark = new Neighbour("tigershark", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(tigershark, client.getQpidDelta());
		for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub.getEndpoints()).hasSize(2);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(tigershark.getNeighbourRequestedSubscriptions().getSubscriptions().size()).isEqualTo(1);
	}

	@Test
	public void routingIsNotSetUpWhenRedirectIsNotAvailable() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.NOT_AVAILABLE, "cap-ex8");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex8");

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "remote-service-provider");

		Neighbour cod = new Neighbour("cod", emptyNeighbourCapabilities, new NeighbourSubscriptionRequest(Collections.singleton(sub)), emptySubscriptionRequest);

		ServiceProvider serviceProvider = new ServiceProvider("sp");
		serviceProvider.setCapabilities(new Capabilities(singleton(cap)));

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(serviceProvider));
		routingConfigurer.setupNeighbourRouting(cod, client.getQpidDelta());
		assertThat(client.queueExists(cod.getName())).isFalse();
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
		assertThat(sub.getEndpoints()).isEmpty();
	}

	@Test
	public void setUpQueueForServiceProviderAndNeighbourForOneCapability() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex9");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex9");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230123%'", NeighbourSubscriptionStatus.ACCEPTED, "remote-sp");
		NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND quadTree like '%,01230122%'", NeighbourSubscriptionStatus.ACCEPTED, "neigh-both");

		Neighbour neigh = new Neighbour("neigh-both", new NeighbourCapabilities(CapabilitiesStatus.UNKNOWN, emptySet()), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), emptySubscriptionRequest);

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neigh, client.getQpidDelta());
		assertThat(sub1.getEndpoints()).hasSize(1);
		assertThat(sub2.getEndpoints()).hasSize(1);
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.queueExists(sub2.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void setupRoutingWithCapabilityExchanges() throws Exception {
		LocalDelivery delivery = new LocalDelivery(
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = 6",
				LocalDeliveryStatus.CREATED
		);
		String deliveryExchangeName = "del-ex10";

		Capability cap = new Capability(
				new DenmApplication(
						"NO-123",
						"pub-1",
						"NO",
						"DENM:1.2.2",
						List.of("12004"),
						List.of(6)
				),
				new Metadata(RedirectStatus.OPTIONAL)
		);
		CapabilityShard shard = new CapabilityShard(1, "cap-ex10", "publicationId = 'pub-1'");
		cap.setShards(Collections.singletonList(shard));
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex10");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		MessageValidatingSelectorCreator creator = new MessageValidatingSelectorCreator();
		String capabilitySelector = creator.makeSelector(cap, null);

		String joinedSelector = String.format("(%s) AND (%s)", delivery.getSelector(), capabilitySelector);
		System.out.println(joinedSelector);

		client.createDirectExchange(deliveryExchangeName);
		client.addWriteAccess(sp.getName(), deliveryExchangeName);
		client.addBinding(deliveryExchangeName, new Binding(deliveryExchangeName, cap.getShards().get(0).getExchangeName(), new Filter(joinedSelector)));

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
	public void oneShardedCapabilityAndOneShardedSubscription() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex11", "cap-ex12", "cap-ex13");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex11");
		client.createHeadersExchange("cap-ex12");
		client.createHeadersExchange("cap-ex13");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(1);
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void oneShardedCapabilityAndTwoShardedSubscriptions() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex14", "cap-ex15", "cap-ex16");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex14");
		client.createHeadersExchange("cap-ex15");
		client.createHeadersExchange("cap-ex16");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");
		NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub1.getEndpoints()).hasSize(1);
		assertThat(sub2.getEndpoints()).hasSize(2);
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		for (NeighbourEndpoint endpoint : sub2.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void oneShardedCapabilityAndNotShardedSubscription() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex17", "cap-ex18", "cap-ex19");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex17");
		client.createHeadersExchange("cap-ex18");
		client.createHeadersExchange("cap-ex19");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(3);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
	}

	@Test
	public void oneCapabilityNotShardedAndOneSubscriptionSharded() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex20");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex20");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(0);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
	}

	@Test
	public void oneShardedCapabilityAndOneSubscriptionShardedAndOneSubscriptionNotSharded() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex21", "cap-ex22", "cap-ex23");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex21");
		client.createHeadersExchange("cap-ex22");
		client.createHeadersExchange("cap-ex23");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");
		NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub1.getEndpoints()).hasSize(2);
		assertThat(sub2.getEndpoints()).hasSize(3);
		for (NeighbourEndpoint endpoint : sub1.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		for (NeighbourEndpoint endpoint : sub2.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
	}

	@Test
	public void twoCapabilitiesShardedAndOneSubscriptionSharded() {
		Capability cap1 = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex24", "cap-ex25", "cap-ex26");
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex24");
		client.createHeadersExchange("cap-ex25");
		client.createHeadersExchange("cap-ex26");

		Capability cap2 = getShardedCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex27", "cap-ex28", "cap-ex29");
		cap2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex27");
		client.createHeadersExchange("cap-ex28");
		client.createHeadersExchange("cap-ex29");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription sub = new NeighbourSubscription("(publicationId = 'pub-1' OR publicationId = 'pub-2') AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "neighbour");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(4);
		for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
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
	@Disabled
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
		String neighbourName = "neighbour-tear-down";
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

	@Test
	public void oneShardedCapabilityAndOneShardedRedirectSubscription() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex30", "cap-ex31", "cap-ex32");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex30");
		client.createHeadersExchange("cap-ex31");
		client.createHeadersExchange("cap-ex32");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-1");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(1);
		assertThat(client.queueExists(sub.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(client.getGroupMember("redirect-sp-1", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
	}

	@Test
	public void oneShardedCapabilityAndTwoShardedRedirectSubscriptions() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex33", "cap-ex34", "cap-ex35");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex33");
		client.createHeadersExchange("cap-ex34");
		client.createHeadersExchange("cap-ex35");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-2");
		NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-3");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub1.getEndpoints()).hasSize(1);
		assertThat(sub2.getEndpoints()).hasSize(2);
		assertThat(client.queueExists(sub1.getEndpoints().stream().findFirst().get().getSource())).isTrue();
		assertThat(client.getQueuePublishingLinks(sub1.getEndpoints().stream().findFirst().get().getSource())).hasSize(1);
		for (NeighbourEndpoint endpoint : sub2.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(client.getGroupMember("redirect-sp-2", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember("redirect-sp-3", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
	}

	@Test
	public void oneShardedCapabilityAndNotShardedRedirectSubscription() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex36", "cap-ex37", "cap-ex38");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex36");
		client.createHeadersExchange("cap-ex37");
		client.createHeadersExchange("cap-ex38");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-4");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(3);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(client.getGroupMember("redirect-sp-4", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
	}

	@Test
	public void oneCapabilityNotShardedAndOneRedirectSubscriptionSharded() {
		Capability cap = getDatexCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex39");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex39");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub = new NeighbourSubscription("publicationId = 'pub-1' AND shardId = 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-5");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(0);
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.NO_OVERLAP);
		assertThat(client.getGroupMember("redirect-sp-4", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNull();
	}

	@Test
	public void oneShardedCapabilityAndOneRedirectSubscriptionShardedAndOneRedirectSubscriptionNotSharded() {
		Capability cap = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex40", "cap-ex41", "cap-ex42");
		cap.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex40");
		client.createHeadersExchange("cap-ex41");
		client.createHeadersExchange("cap-ex42");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(singleton(cap)));

		NeighbourSubscription sub1 = new NeighbourSubscription("publicationId = 'pub-1' AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-6");
		NeighbourSubscription sub2 = new NeighbourSubscription("publicationId = 'pub-1'", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-7");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(new HashSet<>(Arrays.asList(sub1, sub2))), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub1.getEndpoints()).hasSize(2);
		assertThat(sub2.getEndpoints()).hasSize(3);
		for (NeighbourEndpoint endpoint : sub1.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		for (NeighbourEndpoint endpoint : sub2.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub1.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(sub2.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(client.getGroupMember("redirect-sp-6", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember("redirect-sp-7", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
	}

	@Test
	public void twoCapabilitiesShardedAndOneRedirectSubscriptionSharded() {
		Capability cap1 = getShardedCapability("pub-1", RedirectStatus.OPTIONAL, "cap-ex43", "cap-ex44", "cap-ex45");
		cap1.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex43");
		client.createHeadersExchange("cap-ex44");
		client.createHeadersExchange("cap-ex45");

		Capability cap2 = getShardedCapability("pub-2", RedirectStatus.OPTIONAL, "cap-ex46", "cap-ex47", "cap-ex48");
		cap2.setStatus(CapabilityStatus.CREATED);
		client.createHeadersExchange("cap-ex46");
		client.createHeadersExchange("cap-ex47");
		client.createHeadersExchange("cap-ex48");

		ServiceProvider sp = new ServiceProvider("sp");
		sp.setCapabilities(new Capabilities(new HashSet<>(Arrays.asList(cap1, cap2))));

		NeighbourSubscription sub = new NeighbourSubscription("(publicationId = 'pub-1' OR publicationId = 'pub-2') AND shardId >= 2", NeighbourSubscriptionStatus.ACCEPTED, "redirect-sp-8");

		Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(Collections.singleton(sub)), new SubscriptionRequest());

		when(serviceProviderRouter.findServiceProviders()).thenReturn(singleton(sp));
		when(neighbourService.getNodeName()).thenReturn("my-name");
		when(neighbourService.getMessagePort()).thenReturn(qpidContainer.getAmqpsPort().toString());
		routingConfigurer.setupNeighbourRouting(neighbour, client.getQpidDelta());

		assertThat(sub.getEndpoints()).hasSize(4);
		for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
			assertThat(client.queueExists(endpoint.getSource())).isTrue();
			assertThat(client.getQueuePublishingLinks(endpoint.getSource())).hasSize(1);
		}
		assertThat(sub.getSubscriptionStatus()).isEqualTo(NeighbourSubscriptionStatus.CREATED);
		assertThat(client.getGroupMember("redirect-sp-8", QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
	}

	public Capability getDatexCapability(String publicationId, RedirectStatus redirect, String exchangeName) {
		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						publicationId,
						"NO",
						"1.0",
						Arrays.asList("01230122", "01230123"),
						"RoadBlock"
				),
				new Metadata(redirect)
		);
		CapabilityShard shard = new CapabilityShard(1, exchangeName, "publicationId = '" + publicationId + "'");
		cap.setShards(Collections.singletonList(shard));
		return cap;
	}

	public Capability getShardedCapability(String publicationId, RedirectStatus redirect, String exchangeName1, String exchangeName2, String exchangeName3) {
		Metadata metadata = new Metadata(redirect);
		metadata.setShardCount(3);
		CapabilityShard shard1 = new CapabilityShard(1, exchangeName1, "publicationId = '" + publicationId + "'");
		CapabilityShard shard2 = new CapabilityShard(2, exchangeName2, "publicationId = '" + publicationId + "'");
		CapabilityShard shard3 = new CapabilityShard(3, exchangeName3, "publicationId = '" + publicationId + "'");
		Capability cap = new Capability(
				new DatexApplication(
						"NO-1234",
						publicationId,
						"NO",
						"1.0",
						Arrays.asList("01230122", "01230123"),
						"RoadBlock"
				),
				metadata
		);
		cap.setShards(Arrays.asList(shard1, shard2, shard3));
		return cap;
	}
}
