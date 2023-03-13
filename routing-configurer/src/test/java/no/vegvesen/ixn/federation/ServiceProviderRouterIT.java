package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.assertj.core.api.Assertions;
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
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/*
TODO this test should really only be used for testing things in Qpid, not in the model.
We need a separate test where Qpid is mocked, and the database is in a container!!!!
 */

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {ServiceProviderRouter.class, QpidClient.class, QpidClientConfig.class, InterchangeNodeProperties.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {ServiceProviderRouterIT.Initializer.class})
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {


	private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

	@Container
	private static KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf");

	@Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
			.dependsOn(keyContainer);
	private static final String nodeName = "localhost";


	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			qpidContainer.followOutput(new Slf4jLogConsumer(logger));
			TestPropertyValues.of(
					"routing-configurer.baseUrl=" + qpidContainer.getHttpsUrl(),
					"routing-configurer.vhost=localhost",
					"test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
					"test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12"),
					"interchange.node-provider.name=" + nodeName
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@MockBean
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	QpidClient client;

 	@Autowired
	ServiceProviderRouter router;

	@MockBean
	MatchRepository matchRepository;

	@MockBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockBean
	OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

	@MockBean
	OutgoingMatchRepository outgoingMatchRepository;

	@Test
	public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
		ServiceProvider nordea = new ServiceProvider("nordea");
		nordea.addLocalSubscription(createSubscription("DATEX2", "NO"));



		router.syncServiceProviders(Arrays.asList(nordea));
		Set<LocalEndpoint> endpoints = nordea.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(endpoints).hasSize(1);

		nordea.addLocalSubscription(createSubscription("DATEX2", "FI"));
		router.syncServiceProviders(Arrays.asList(nordea));
		Set<LocalEndpoint> endpoints2 = nordea.getSubscriptions().stream()
				.filter(s -> s.getSelector().contains("'FI'"))
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(endpoints2).hasSize(1);
	}

	private LocalSubscription createSubscription(String messageType, String originatingCountry) {
		String selector = "messageType = '" + messageType + "' and originatingCountry = '" + originatingCountry +"'";
		return new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector, nodeName);
	}

	//TODO: Need to fix this after match is removed with status
	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		String source = "king_gustaf_source";
		king_gustaf.addLocalSubscription(new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2'",
				nodeName,
				Collections.emptySet(),
				Collections.singleton(new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		));
		DatexCapability capability = new DatexCapability(
				"NO-123",
				"NO",
				"1.0",
				Collections.emptySet(),
				Collections.emptySet()
		);
		Capabilities capabilities = new Capabilities(
				Capabilities.CapabilitiesStatus.KNOWN,
				Collections.singleton(capability
				)
		);
		king_gustaf.setCapabilities(capabilities);
		String deliverySelector = "messageType = 'DATEX2'";
		LocalDelivery localDelivery = new LocalDelivery(
				1,
				"/deliveries/1",
				deliverySelector,
				LocalDeliveryStatus.CREATED
		);
		String exchangeName = "myexchange";
		localDelivery.setExchangeName(exchangeName);
		localDelivery.addEndpoint(new LocalDeliveryEndpoint(
				qpidContainer.getHost(),
				qpidContainer.getAmqpsPort(),
				exchangeName
		));
		king_gustaf.addDeliveries(Collections.singleton(localDelivery));

		OutgoingMatch outgoingMatch = new OutgoingMatch(
				localDelivery,
				capability,
				king_gustaf.getName(),
				OutgoingMatchStatus.SETUP_ENDPOINT
		);
		when(outgoingMatchDiscoveryService.findMatchesFromDeliveryId(1)).thenReturn(Arrays.asList(outgoingMatch));
		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(king_gustaf.getName())).thenReturn(Arrays.asList(outgoingMatch));

		router.syncServiceProviders(Arrays.asList(king_gustaf));

		SSLContext kingGustafSslContext = setUpTestSslContext("king_gustaf.p12");
		//TODO the actual name of the container is the name of the cluster as well....
		String amqpsUrl = qpidContainer.getAmqpsUrl();
		Set<LocalEndpoint> sinkEndpoints = king_gustaf.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(sinkEndpoints).hasSize(1);
		LocalEndpoint endpoint = sinkEndpoints.stream().findFirst().get();
		Sink readKingGustafQueue = new Sink(amqpsUrl, endpoint.getSource(), kingGustafSslContext);
		readKingGustafQueue.start();

		Set<LocalDeliveryEndpoint> deliveryEndpoints = king_gustaf.getDeliveries().stream().flatMap(d -> d.getEndpoints().stream()).collect(Collectors.toSet());
		assertThat(deliveryEndpoints).hasSize(1);
		LocalDeliveryEndpoint deliveryEndpoint = deliveryEndpoints.stream().findFirst().get();

		Source writeOnrampQueue = new Source(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(amqpsUrl, deliveryEndpoint.getTarget(), kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on (onramp)");
		} catch (Exception ignore) {
		}
	}

	//TODO: Need to fix this after match is removed with status
	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribFederatedInterchangesGroup() {
		String serviceProviderName = "tore-down-service-provider";

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");

		ServiceProvider toreDownServiceProvider = new ServiceProvider(
				serviceProviderName,
				Collections.singleton(localSubscription)
		);
		String subscriptionExchangeName = "subscription-exchange";

		Subscription subscription = new Subscription(
				"a = b",
				SubscriptionStatus.REQUESTED
		);
		subscription.setExchangeName(subscriptionExchangeName);

		Match match = new Match(localSubscription, subscription, serviceProviderName);

		when(matchRepository.findBySubscription_ExchangeName(serviceProviderName)).thenReturn(Arrays.asList(match));

		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(toreDownServiceProvider.getName());
		assertThat(localSubscription.getStatus().equals(LocalSubscriptionStatus.CREATED));

		Set<LocalEndpoint> localEndpoints = toreDownServiceProvider.getSubscriptions().stream()
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(localEndpoints).hasSize(1);
		LocalEndpoint endpoint = localEndpoints.stream().findFirst().get();

		assertThat(client.queueExists(endpoint.getSource())).isTrue();


		toreDownServiceProvider.setSubscriptions(
				toreDownServiceProvider.getSubscriptions().stream()
						.map(localSubscription1 -> localSubscription1.withStatus(LocalSubscriptionStatus.TEAR_DOWN))
						.collect(Collectors.toSet()));

		when(matchRepository.findAllByLocalSubscriptionId(any(Integer.class))).thenReturn(Collections.emptyList());
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(toreDownServiceProvider.getName());
		assertThat(client.queueExists(endpoint.getSource())).isFalse();
	}

	/*
	 Note that this test runs syncServiceProviders twice on the same ServiceProvider.
	 This is due to a bug we had, that checked the members of the groups based on a stale list of
	 group members. This only showed up after running the method twice.
	 */
	@Test
	public void serviceProviderWithCapabiltiesShouldNotHaveQueuButExistInServiceProvidersGroup() {
		ServiceProvider onlyCaps = new ServiceProvider("onlyCaps");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new DatexCapability(null,"NO", null, null, null)));
		onlyCaps.setCapabilities(capabilities);
		router.syncServiceProviders(Arrays.asList(onlyCaps));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(onlyCaps.getName());

		router.syncServiceProviders(Arrays.asList(onlyCaps));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(onlyCaps.getName());
		assertThat(client.queueExists(onlyCaps.getName())).isFalse();
	}

	@Test
	public void serviceProviderShouldBeRemovedWhenCapabilitiesAreRemoved() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new DatexCapability(null,"NO", null, null, null)));
		serviceProvider.setCapabilities(capabilities);

		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(serviceProvider.getName());

		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>()));
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(serviceProvider.getName());
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasConsumerCommonNameSameAsIxnNameAndServiceProviderName() {
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'",
				"my-node");
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'",
				"my-service-provider");

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		serviceProvider.addLocalSubscription(sub1);
		serviceProvider.addLocalSubscription(sub2);
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(serviceProvider.getName());

		assertThat(sub1.getLocalEndpoints()).hasSize(1);
		LocalEndpoint endpoint1 = sub1.getLocalEndpoints().stream().findFirst().get();
		assertThat(client.queueExists(endpoint1.getSource())).isTrue();

		assertThat(sub2.getLocalEndpoints()).hasSize(0);
	}

	@Test
	public void setUpQueueForPrivateChannels() {
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName("my-private-service-provider");

		serviceProvider.addPrivateChannel("my-client");

		router.syncPrivateChannels(serviceProvider);

		assertThat(serviceProvider.getPrivateChannels().size()).isEqualTo(1);

		PrivateChannel privateChannel = serviceProvider.getPrivateChannels().stream().findFirst().get();

		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(client.queueExists(privateChannel.getQueueName())).isTrue();
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).contains(privateChannel.getPeerName());
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).contains(serviceProvider.getName());
	}

	@Test
	public void tearDownQueueForPrivateChannels() {
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName("my-private-service-provider-1");

		serviceProvider.addPrivateChannel("my-client-1");

		router.syncPrivateChannels(serviceProvider);

		assertThat(serviceProvider.getPrivateChannels().size()).isEqualTo(1);

		PrivateChannel privateChannel = serviceProvider.getPrivateChannels().stream().findFirst().get();

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);

		router.syncPrivateChannels(serviceProvider);

		assertThat(client.queueExists(privateChannel.getQueueName())).isFalse();
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).doesNotContain(privateChannel.getPeerName());
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).doesNotContain(serviceProvider.getName());
		assertThat(serviceProvider.getPrivateChannels()).hasSize(0);
	}

	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyHaveMultiplePrivateChannels() {
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName("my-private-service-provider-2");

		serviceProvider.addPrivateChannel("my-client-11");
		serviceProvider.addPrivateChannel("my-client-12");

		router.syncPrivateChannels(serviceProvider);

		assertThat(serviceProvider.getPrivateChannels().size()).isEqualTo(2);

		PrivateChannel privateChannel = serviceProvider.getPrivateChannels().stream().findFirst().get();

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);

		router.syncPrivateChannels(serviceProvider);

		assertThat(client.queueExists(privateChannel.getQueueName())).isFalse();
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).doesNotContain(privateChannel.getPeerName());
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).contains(serviceProvider.getName());
		assertThat(serviceProvider.getPrivateChannels()).hasSize(1);
	}

	@Test
	public void serviceProviderShouldBeRemovedFromGroupWhenTheyHaveNoCapabilitiesOrSubscriptions() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider-should-be-removed");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new DatexCapability(null, "NO", null, null, null)));
		serviceProvider.setCapabilities(capabilities);

		router.syncServiceProviders(Arrays.asList(serviceProvider));
		Assertions.assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(serviceProvider.getName());

		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>()));
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		Assertions.assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(serviceProvider.getName());
	}

	@Test
	public void tearDownQueueWhenLocalSubscriptionIsDeletedAfterMatch() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		String queueName = "my-queue";
		InterchangeNodeProperties nodeProperties = new InterchangeNodeProperties("my-host","1234");
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN, selector, "my-node");
		localSubscription.setLocalEndpoints(Collections.singleton(
				new LocalEndpoint(queueName,
						nodeProperties.getName(),
						Integer.parseInt(nodeProperties.getMessageChannelPort())
				)
		));
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		serviceProvider.addLocalSubscription(localSubscription);

		client.createQueue(queueName);

		when(matchRepository.findAllByLocalSubscriptionId(any(Integer.class))).thenReturn(Collections.emptyList());

		router.processSubscription(serviceProvider, localSubscription, nodeProperties.getName(), nodeProperties.getMessageChannelPort());

		assertThat(client.queueExists(queueName)).isFalse();

	}

	@Test
	public void createTargetAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);

		denmCapability.setStatus(CapabilityStatus.CREATED);
		denmCapability.setCapabilityExchangeName("cap-ex1");
		client.createTopicExchange("cap-ex1");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		serviceProvider.addDeliveries(Collections.singleton(delivery));
		delivery.setExchangeName("my-exchange5");

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Arrays.asList(match));
		when(outgoingMatchDiscoveryService.findByOutgoingMatchId(any(Integer.class))).thenReturn(match);

		router.setUpDeliveryQueue(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(1)).updateOutgoingMatchToUp(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getExchangeName()).isNotNull();
	}

	@Test
	public void createMultipleTargetsAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);
		denmCapability.setCapabilityExchangeName("cap-ex2");
		client.createTopicExchange("cap-ex2");

		Capability denmCapability2 = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("5")
		);
		denmCapability2.setCapabilityExchangeName("cap-ex3");
		client.createTopicExchange("cap-ex3");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setExchangeName("my-exchange6");

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Arrays.asList(match, match2));
		router.setUpDeliveryQueue(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(2)).updateOutgoingMatchToUp(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getExchangeName()).isNotNull();
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedDelivery() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String exchangeName = "my-exchange8";

		Capability denmCapability = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);
		denmCapability.setStatus(CapabilityStatus.CREATED);
		denmCapability.setCapabilityExchangeName("cap-ex4");
		client.createTopicExchange("cap-ex4");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
		delivery.setExchangeName(exchangeName);

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Arrays.asList(match));
		router.setUpDeliveryQueue(serviceProvider);

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);
		match.setStatus(OutgoingMatchStatus.TEARDOWN_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToTearDownEndpointsFor(any(String.class))).thenReturn(Arrays.asList(match));
		when(outgoingMatchDiscoveryService.findMatchFromDeliveryId(any(Integer.class))).thenReturn(match);
		router.tearDownDeliveryQueues(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(1)).updateOutgoingMatchToDeleted(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.exchangeExists()).isFalse();
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedCapabilityWhenThereIsNoOtherMatches() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);
		denmCapability.setStatus(CapabilityStatus.CREATED);
		denmCapability.setCapabilityExchangeName("cap-ex5");
		client.createDirectExchange("cap-ex5");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		delivery.setExchangeName("my-exchange9");

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Arrays.asList(match));
		router.setUpDeliveryQueue(serviceProvider);

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);
		match.setStatus(OutgoingMatchStatus.TEARDOWN_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToTearDownEndpointsFor(any(String.class))).thenReturn(Arrays.asList(match));
		router.tearDownDeliveryQueues(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(1)).updateOutgoingMatchToDeleted(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
	}

	@Test
	public void removeOneEndpointsWhenOneOfTwoMatchesIsRemoved() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Capability denmCapability1 = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);
		denmCapability1.setStatus(CapabilityStatus.CREATED);
		denmCapability1.setCapabilityExchangeName("cap-ex6");
		client.createDirectExchange("cap-ex6");

		Capability denmCapability2 = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1233"),
				Collections.singleton("6")
		);
		denmCapability2.setStatus(CapabilityStatus.CREATED);
		denmCapability2.setCapabilityExchangeName("cap-ex7");
		client.createDirectExchange("cap-ex7");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		delivery.setExchangeName("my-exchange10");

		OutgoingMatch match1 = new OutgoingMatch(delivery, denmCapability1, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName, OutgoingMatchStatus.SETUP_ENDPOINT);

		when(outgoingMatchDiscoveryService.findMatchesToSetupEndpointFor(any(String.class))).thenReturn(Arrays.asList(match1, match2));
		router.setUpDeliveryQueue(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(2)).updateOutgoingMatchToUp(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();

		match1.setStatus(OutgoingMatchStatus.UP);
		match2.setStatus(OutgoingMatchStatus.TEARDOWN_ENDPOINT);
		when(outgoingMatchDiscoveryService.findMatchesToTearDownEndpointsFor(any(String.class))).thenReturn(Arrays.asList(match2));

		router.tearDownDeliveryQueues(serviceProvider);

		verify(outgoingMatchDiscoveryService, times(1)).updateOutgoingMatchToDeleted(any(OutgoingMatch.class));

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
	}

	@Test
	public void localSubscriptionConnectsToCapabilityExchange() {
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint();
		endpoint.setSource("my-queue12");
		subscription.setLocalEndpoints(Collections.singleton(endpoint));

		Capability denmCapability = new DenmCapability(
				"NPRA",
				"NO",
				"1.0",
				Collections.singleton("1234"),
				Collections.singleton("6")
		);
		denmCapability.setStatus(CapabilityStatus.CREATED);
		denmCapability.setCapabilityExchangeName("cap-ex8");
		client.createDirectExchange("cap-ex8");
		client.createQueue("my-queue12");

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(denmCapability)));

		router.syncLocalSubscriptionsToServiceProviderCapabilities(mySP, Collections.singleton(otherSP));

		assertThat(client.getQueueBindKeys("my-queue12")).hasSize(1);
		assertThat(subscription.getConnections()).hasSize(1);
	}


	@Test
	public void routerPicksUpRequestedLocalSubscription() {
		LocalSubscription localSubscription = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"originatingCountry = 'NO' and messageType = 'DENM' and quadTree like '%,12004%' and causeCode = '6'",
				"a.bouvetinterchange.eu"

		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"serviceProvider",
				new Capabilities(),
				Collections.singleton(
						localSubscription
				),
				Collections.emptySet(),
				LocalDateTime.now()
		);
		//TODO this will make no difference whatsoever!
		serviceProviderRepository.save(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider));
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		assertThat(serviceProvider.getSubscriptions().stream().findFirst().get().getStatus()).isEqualTo(LocalSubscriptionStatus.CREATED);
	}

	@Test
	public void testIllegalLocalSubscriptionGetsRemovedFromServiceProvider() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		router.processSubscription(serviceProvider,subscription,"myNode","5432");
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void tearDownLocalSubscriptionWithEmptyMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(1)).thenReturn(new ArrayList<>());
		router.processSubscription(serviceProvider,subscription,"myNode","5432");
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
		verify(matchRepository).findAllByLocalSubscriptionId(1);
	}

	@Test
	public void teardownLocalSubscriptionWithRemainingMatch() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		Match match = new Match(
				subscription,
				new Subscription("",SubscriptionStatus.TEAR_DOWN),
                "sp1"
		);
		when(matchRepository.findAllByLocalSubscriptionId(1)).thenReturn(Arrays.asList(match));
		router.processSubscription(serviceProvider,subscription,"myNode","5432");
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
		verify(matchRepository).findAllByLocalSubscriptionId(1);
	}

	@Test
	public void redirectSubscriptionStatusTearDownEmptyMatchList() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.TEAR_DOWN,
				"",
				"myNode"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		when(matchRepository.findAllByLocalSubscriptionId(1)).thenReturn(Collections.emptyList());
		router.processRedirectSubscription(serviceProvider,subscription);
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue";
		String exchangeName = "sub-exchange";

		client.createQueue(queueName);
		client.createTopicExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueueBindKeys(queueName)).hasSize(1);
		assertThat(client.getQueueBindKeys(queueName)).contains(client.createBindKey(exchangeName, queueName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionsCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-1";
		String exchangeName = "sub-exchange-1";
		String exchangeName2 = "sub-exchange-2";

		client.createQueue(queueName);
		client.createTopicExchange(exchangeName);
		client.createTopicExchange(exchangeName2);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Subscription subscription2 = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription2.setExchangeName(exchangeName2);

		Match match = new Match(localSubscription, subscription, "my-service-provider");
		Match match2 = new Match(localSubscription, subscription2, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match, match2));
		router.createBindingsWithMatches();

		assertThat(client.getQueueBindKeys(queueName)).hasSize(2);
		assertThat(client.getQueueBindKeys(queueName)).contains(client.createBindKey(exchangeName, queueName));
		assertThat(client.getQueueBindKeys(queueName)).contains(client.createBindKey(exchangeName2, queueName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedBindKeyAlreadyExists() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-4";
		String exchangeName = "sub-exchange-4";

		client.createQueue(queueName);
		client.createTopicExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		//Mocking that binding already exists and isn't created again
		client.bindSubscriptionExchange(selector, exchangeName, queueName);

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueueBindKeys(queueName)).hasSize(1);
		assertThat(client.getQueueBindKeys(queueName)).contains(client.createBindKey(exchangeName, queueName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionTearDown() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-3";
		String exchangeName = "sub-exchange-3";

		client.createQueue(queueName);
		client.createTopicExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueueBindKeys(queueName)).hasSize(0);
		assertThat(client.getQueueBindKeys(queueName)).doesNotContain(client.createBindKey(exchangeName, queueName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedButNoMatchYet() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-5";
		String exchangeName = "sub-exchange-5";

		client.createQueue(queueName);
		client.createTopicExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueueBindKeys(queueName)).hasSize(1);
		assertThat(client.getQueueBindKeys(queueName)).contains(client.createBindKey(exchangeName, queueName));
	}

	@Test
	public void redirectSubscriptionStatusIllegal() {
		LocalSubscription subscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.ILLEGAL,
				"",
				"sp1"
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"sp1",
				Collections.singleton(subscription)
		);
		router.processRedirectSubscription(serviceProvider,subscription);
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}
}
