package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.repository.*;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.mockito.Mock;
import org.mockito.Mockito;
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

	@MockBean
	PrivateChannelRepository privateChannelRepository;


	@Autowired
	QpidClient client;

 	@Autowired
	ServiceProviderRouter router;

	@MockBean
	MatchRepository matchRepository;

	@MockBean
	ListenerEndpointRepository listenerEndpointRepository;

	@MockBean
	OutgoingMatchRepository outgoingMatchRepository;


	@Test
	public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
		ServiceProvider nordea = new ServiceProvider("nordea");
		LocalSubscription localSubscription1 = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2' and originatingCountry = 'NO'",
				nodeName
		);
		nordea.addLocalSubscription(localSubscription1);

		when(serviceProviderRepository.save(any())).thenReturn(nordea);
		nordea = router.syncSubscriptions(nordea, client.getQpidDelta());
		Set<LocalEndpoint> endpoints = nordea.getSubscriptions().stream().flatMap(s -> s.getLocalEndpoints().stream()).collect(Collectors.toSet());
		assertThat(endpoints).hasSize(1);

		LocalSubscription localSubscription2 = new LocalSubscription(
				LocalSubscriptionStatus.REQUESTED,
				"messageType = 'DATEX2' and originatingCountry = 'FI'",
				nodeName
		);

		nordea.addLocalSubscription(localSubscription2);
		nordea = router.syncSubscriptions(nordea, client.getQpidDelta());
		Set<LocalEndpoint> endpoints2 = nordea.getSubscriptions().stream()
				.filter(s -> s.getSelector().contains("'FI'"))
				.flatMap(s -> s.getLocalEndpoints().stream())
				.collect(Collectors.toSet());
		assertThat(endpoints2).hasSize(1);
	}
	@Test
	public void setUpQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannel privateChannel = new PrivateChannel("private-channel",PrivateChannelStatus.REQUESTED,"service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.save(any())).thenReturn(privateChannel);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(privateChannel.getStatus()).isEqualTo(PrivateChannelStatus.CREATED);
		assertThat(client.queueExists(privateChannel.getQueueName())).isTrue();
		assertThat(client.getGroupMember(privateChannel.getPeerName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
	}

	@Test
	public void tearDownQueueForPrivateChannels(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannel privateChannel = new PrivateChannel("private-channel",PrivateChannelStatus.REQUESTED,"service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.save(any())).thenReturn(privateChannel);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);
		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of());

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(client.queueExists(privateChannel.getQueueName())).isFalse();
		assertThat(client.getGroupMember(privateChannel.getPeerName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();
	}
	@Test public void createEndpointForPrivateChannel(){
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannel privateChannel = new PrivateChannel("private-channel",PrivateChannelStatus.REQUESTED,"service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.save(any())).thenReturn(privateChannel);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel));
		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of(privateChannel));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());

		assertThat(privateChannel.getEndpoint()).isNotNull();

	}
	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyHaveMultiplePrivateChannels() {
		ServiceProvider serviceProvider = new ServiceProvider("service-provider");
		PrivateChannel privateChannel_1 = new PrivateChannel("private-channel-1",PrivateChannelStatus.REQUESTED,"service-provider");
		PrivateChannel privateChannel_2 = new PrivateChannel("private-channel-2", PrivateChannelStatus.CREATED, "service-provider");

		when(serviceProviderRepository.save(serviceProvider)).thenReturn(serviceProvider);
		when(privateChannelRepository.findAllByServiceProviderName(any())).thenReturn(List.of(privateChannel_1, privateChannel_2));
		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of(privateChannel_1, privateChannel_2));

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		privateChannel_1.setStatus(PrivateChannelStatus.TEAR_DOWN);

		when(privateChannelRepository.findAllByStatus(PrivateChannelStatus.CREATED)).thenReturn(List.of(privateChannel_2));
		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
	}


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

		CapabilitySplit capability = new CapabilitySplit(
				new DatexApplication(
						"NO-123",
						"pub-1",
						"NO",
						"1.0",
						Collections.emptySet(),
						"publicationType"
				),
				new Metadata(RedirectStatus.OPTIONAL)
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
				king_gustaf.getName()
		);
		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(outgoingMatch));
		when(serviceProviderRepository.save(any())).thenReturn(king_gustaf);
		router.syncServiceProviders(Arrays.asList(king_gustaf), client.getQpidDelta());

		SSLContext kingGustafSslContext = setUpTestSslContext("king_gustaf.p12");
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
			fail("Should not allow king_gustaf to read from queue not granted access on local endpoint");
		} catch (Exception ignore) {
		}
	}

	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribeFederatedInterchangesGroup() {
		String serviceProviderName = "tore-down-service-provider";

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-node");

		ServiceProvider toreDownServiceProvider = new ServiceProvider(
				serviceProviderName,
				Collections.singleton(localSubscription)
		);

		toreDownServiceProvider.addLocalSubscription(localSubscription);

		when(serviceProviderRepository.save(any())).thenReturn(toreDownServiceProvider);
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(toreDownServiceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
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
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider), client.getQpidDelta());
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getGroupMember(toreDownServiceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
		assertThat(client.queueExists(endpoint.getSource())).isFalse();
	}

	/*
	 Note that this test runs syncServiceProviders twice on the same ServiceProvider.
	 This is due to a bug we had, that checked the members of the groups based on a stale list of
	 group members. This only showed up after running the method twice.
	 */
	@Test
	public void serviceProviderWithCapabilitiesShouldNotHaveQueueButExistInServiceProvidersGroup() {
		ServiceProvider onlyCaps = new ServiceProvider("onlyCaps");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new CapabilitySplit(new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata(RedirectStatus.OPTIONAL))));

		when(serviceProviderRepository.save(any())).thenReturn(onlyCaps);
		onlyCaps.setCapabilities(capabilities);
		router.syncServiceProviders(Arrays.asList(onlyCaps), client.getQpidDelta());
		assertThat(client.getGroupMember(onlyCaps.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		router.syncServiceProviders(Arrays.asList(onlyCaps), client.getQpidDelta());
		assertThat(client.getGroupMember(onlyCaps.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();
		assertThat(client.queueExists(onlyCaps.getName())).isFalse();
	}

	@Test
	public void serviceProviderShouldBeRemovedWhenCapabilitiesAreRemoved() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceProvider");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new CapabilitySplit(new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata(RedirectStatus.OPTIONAL))));
		serviceProvider.setCapabilities(capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());

		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>()));
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
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

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		assertThat(sub1.getLocalEndpoints()).hasSize(1);
		LocalEndpoint endpoint1 = sub1.getLocalEndpoints().stream().findFirst().get();
		assertThat(client.queueExists(endpoint1.getSource())).isTrue();

		assertThat(sub2.getLocalEndpoints()).hasSize(0);
	}



	/*



	@Test
	public void doNotRemoveServiceProviderFromGroupWhenTheyHaveMultiplePrivateChannels() {
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName("my-private-service-provider-2");

		serviceProvider.addPrivateChannel("my-client-11");
		serviceProvider.addPrivateChannel("my-client-12");

		//when(serviceProviderRepository.findByName(any())).thenReturn(serviceProvider);
		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		verify(serviceProviderRepository, times(1)).save(any());

		assertThat(serviceProvider.getPrivateChannels().size()).isEqualTo(2);

		PrivateChannel privateChannel = serviceProvider.getPrivateChannels().stream().findFirst().get();

		privateChannel.setStatus(PrivateChannelStatus.TEAR_DOWN);

		router.syncPrivateChannels(serviceProvider, client.getQpidDelta());
		verify(serviceProviderRepository, times(2)).save(any());

		assertThat(client.queueExists(privateChannel.getQueueName())).isFalse();
		assertThat(client.getGroupMember(privateChannel.getPeerName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNull();
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).isNotNull();
		assertThat(serviceProvider.getPrivateChannels()).hasSize(1);
	}
*/
	@Test
	public void serviceProviderShouldBeRemovedFromGroupWhenTheyHaveNoCapabilitiesOrSubscriptions() {
		ServiceProvider serviceProvider = new ServiceProvider("serviceprovider-should-be-removed");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN,
				Collections.singleton(new CapabilitySplit(new DatexApplication("NO-123", "NO-pub","NO", "1.0", Collections.emptySet(), "SituationPublication"), new Metadata(RedirectStatus.OPTIONAL))));
		serviceProvider.setCapabilities(capabilities);

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNotNull();

		serviceProvider.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>()));

		router.syncServiceProviders(Arrays.asList(serviceProvider), client.getQpidDelta());
		assertThat(client.getGroupMember(serviceProvider.getName(),QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).isNull();
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
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.processSubscription(serviceProvider, localSubscription, nodeProperties.getName(), nodeProperties.getMessageChannelPort(), client.getQpidDelta());

		assertThat(client.queueExists(queueName)).isFalse();
	}

	@Test
	public void createTargetAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex1", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata
		);

		client.createHeadersExchange("cap-ex1");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		serviceProvider.addDeliveries(Collections.singleton(delivery));
		delivery.setExchangeName("my-exchange5");

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		verify(serviceProviderRepository, times(1)).save(any());

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getExchangeName()).isNotNull();
	}

	@Test
	public void createMultipleTargetsAndConnectForServiceProvider() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Metadata metadata1 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard1 = new Shard(1, "cap-ex2", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex2");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex3", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit denmCapability2 = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(5))
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex3");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setExchangeName("my-exchange6");
		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match, match2));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getExchangeName()).isNotNull();
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedDelivery() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String exchangeName = "my-exchange8";

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex4", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata
		);
		client.createHeadersExchange("cap-ex4");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		serviceProvider.addDeliveries(new HashSet<>(Arrays.asList(delivery)));
		delivery.setExchangeName(exchangeName);

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		delivery.setStatus(LocalDeliveryStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(delivery.exchangeExists()).isFalse();
	}

	@Test
	public void tearDownTargetForDeliveryByDeletedCapabilityWhenThereIsNoOtherMatches() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex5", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata
		);
		client.createHeadersExchange("cap-ex5");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO'", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		delivery.setExchangeName("my-exchange9");

		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match = new OutgoingMatch(delivery, denmCapability, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();

		denmCapability.setStatus(CapabilityStatus.TEAR_DOWN);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Collections.emptyList());
		router.tearDownDeliveryQueues(serviceProvider, client.getQpidDelta());

		assertThat(delivery.exchangeExists()).isFalse();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.NO_OVERLAP);
	}

	@Test
	public void removeOneEndpointsWhenOneOfTwoMatchesIsRemoved() {
		String serviceProviderName = "my-service-provider";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);

		Metadata metadata1 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard1 = new Shard(1, "cap-ex6", "publicationId = 'pub-1'");
		metadata1.setShards(Collections.singletonList(shard1));

		CapabilitySplit denmCapability1 = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata1
		);
		client.createHeadersExchange("cap-ex6");

		Metadata metadata2 = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard2 = new Shard(1, "cap-ex7", "publicationId = 'pub-1'");
		metadata2.setShards(Collections.singletonList(shard2));

		CapabilitySplit denmCapability2 = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1233")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata2
		);
		client.createHeadersExchange("cap-ex7");

		LocalDelivery delivery = new LocalDelivery("originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", LocalDeliveryStatus.CREATED);
		delivery.setId(1);
		delivery.setExchangeName("my-exchange10");

		serviceProvider.addDeliveries(Collections.singleton(delivery));

		OutgoingMatch match1 = new OutgoingMatch(delivery, denmCapability1, serviceProviderName);

		OutgoingMatch match2 = new OutgoingMatch(delivery, denmCapability2, serviceProviderName);

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match1, match2));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.setUpDeliveryQueue(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();

		when(outgoingMatchRepository.findAllByLocalDelivery_Id(any())).thenReturn(Arrays.asList(match2));
		router.tearDownDeliveryQueues(serviceProvider, client.getQpidDelta());

		assertThat(client.exchangeExists(delivery.getExchangeName())).isTrue();
		assertThat(delivery.getStatus()).isEqualTo(LocalDeliveryStatus.CREATED);
	}

	@Test
	public void tearDownDeliveryQueueShouldNotChangeRequestedDeliveries() {
		LocalDelivery localDelivery = new LocalDelivery(
				1,
				"/this/is/my/path",
				"a = b",
				LocalDeliveryStatus.REQUESTED
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				"no-change-for-requested-delivery-sp",
				new Capabilities(),
				Collections.emptySet(),
				Collections.singleton(
						localDelivery
				),
				null
		);
		QpidDelta delta = client.getQpidDelta();
		when(outgoingMatchRepository.findAllByLocalDelivery_Id(1)).thenReturn(new ArrayList<>());
		router.tearDownDeliveryQueues(serviceProvider,delta);
		assertThat(localDelivery.getStatus()).isEqualTo(LocalDeliveryStatus.REQUESTED);
	}


	@Test
	public void localSubscriptionConnectsToCapabilityExchange() {
		ServiceProvider mySP = new ServiceProvider("my-sp");
		ServiceProvider otherSP = new ServiceProvider("other-sp");

		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, "originatingCountry = 'NO' and (quadTree like '%,1234%' or quadTree like '%,1233%')", "my-node");
		LocalEndpoint endpoint = new LocalEndpoint();
		endpoint.setSource("my-queue12");
		subscription.setLocalEndpoints(Collections.singleton(endpoint));

		Metadata metadata = new Metadata(RedirectStatus.OPTIONAL);
		Shard shard = new Shard(1, "cap-ex8", "publicationId = 'pub-1'");
		metadata.setShards(Collections.singletonList(shard));

		CapabilitySplit denmCapability = new CapabilitySplit(
				new DenmApplication(
						"NPRA",
						"pub-1",
						"NO",
						"1.0",
						new HashSet<>(Arrays.asList("1234")),
						new HashSet<>(Arrays.asList(6))
				),
				metadata
		);
		client.createHeadersExchange("cap-ex8");
		client.createQueue("my-queue12");

		mySP.addLocalSubscription(subscription);
		otherSP.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Collections.singleton(denmCapability)));

		when(serviceProviderRepository.save(any())).thenReturn(mySP);
		router.syncLocalSubscriptionsToServiceProviderCapabilities(mySP, client.getQpidDelta(), Collections.singleton(otherSP));

		verify(serviceProviderRepository, times(1)).save(any());

		assertThat(client.getQueuePublishingLinks("my-queue12")).hasSize(1);
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

		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
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
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
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
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(new ArrayList<>());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
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
				"sp-1"
		);

		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Arrays.asList(match));
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).hasSize(1);
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
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue";
		String exchangeName = "sub-exchange";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

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

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionsCreated() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-1";
		String exchangeName = "sub-exchange-1";
		String exchangeName2 = "sub-exchange-2";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);
		client.createHeadersExchange(exchangeName2);

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

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(2);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName2));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedBindKeyAlreadyExists() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-4";
		String exchangeName = "sub-exchange-4";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, consumerCommonName);
		localSubscription.setLocalEndpoints(Collections.singleton(new LocalEndpoint(queueName, "my-node", 5671)));
		serviceProvider.addLocalSubscription(localSubscription);

		Subscription subscription = new Subscription(selector, SubscriptionStatus.CREATED, consumerCommonName);
		subscription.setExchangeName(exchangeName);

		Match match = new Match(localSubscription, subscription, "my-service-provider");

		//Mocking that binding already exists and isn't created again
		client.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));

		when(serviceProviderRepository.findAll()).thenReturn(Collections.singletonList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.singletonList(match));
		router.createBindingsWithMatches();

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionTearDown() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-3";
		String exchangeName = "sub-exchange-3";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

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

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(0);
		assertThat(client.getQueuePublishingLinks(queueName)).noneMatch(b -> b.getBindingKey().equals(exchangeName));
	}

	@Test
	public void createBindingsWithMatchesWithLocalSubscriptionCreatedAndSubscriptionCreatedButNoMatchYet() {
		String selector = "originatingCountry = 'NO' and messageType = 'DENM'";
		String consumerCommonName = "my-node";

		String queueName = "loc-sub-queue-5";
		String exchangeName = "sub-exchange-5";

		client.createQueue(queueName);
		client.createHeadersExchange(exchangeName);

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

		assertThat(client.getQueuePublishingLinks(queueName)).hasSize(1);
		assertThat(client.getQueuePublishingLinks(queueName)).anyMatch(b -> b.getBindingKey().equals(exchangeName));
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
		when(matchRepository.findAllByLocalSubscriptionId(any())).thenReturn(Collections.emptyList());
		when(serviceProviderRepository.save(any())).thenReturn(serviceProvider);
		router.syncServiceProviders(Collections.singleton(serviceProvider), client.getQpidDelta());
		assertThat(serviceProvider.getSubscriptions()).isEmpty();
	}

	@Test
	public void createBindingsWithMatchesWhereSubscriptionExchangeIsNotAlreadyCreated() {
		String name = "service-provider-no-subs-exchange-setup";
		String source = "no-subs-exchange-setup-local-sub";
		client.createQueue(source);
		LocalSubscription localSubscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"a = b",
				qpidContainer.getvHostName(),
				Collections.emptySet(),
				Collections.singleton(
						new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				name,
				Collections.singleton(
						localSubscription
				)
		);
		Subscription subscription = new Subscription(
			SubscriptionStatus.REQUESTED,
				"a = b",
				"",
				"a=b"
		);
		String exchangeName = "this-is-my-non-existing-local-exchange";
		subscription.setExchangeName(exchangeName);
		Match match = new Match(
				localSubscription,
				subscription
		);
		when(serviceProviderRepository.findAll()).thenReturn(Arrays.asList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(localSubscription.getId())).thenReturn(Arrays.asList(match));
		router.createBindingsWithMatches();
		//TODO asserts, verify that the methods are called
		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(client.getQueuePublishingLinks(source)).doesNotContain(new Binding(source, name, new Filter("a = b")));

	}

	@Test
	public void createBindingsWithMatchesWhereLocalSubscriptionQueueIsNotAlreadyCreated() {
		String name = "service-provider-no-local-subs-queue-setup";
		String source = "no-local-subs-queue-setup-local-sub";
		String exchangeName = "this-is-my-existing-local-exchange";
		client.createHeadersExchange(exchangeName);
		LocalSubscription localSubscription = new LocalSubscription(
				1,
				LocalSubscriptionStatus.REQUESTED,
				"a = b",
				qpidContainer.getvHostName(),
				Collections.emptySet(),
				Collections.singleton(
						new LocalEndpoint(
								source,
								qpidContainer.getHost(),
								qpidContainer.getAmqpsPort()
						)
				)
		);
		ServiceProvider serviceProvider = new ServiceProvider(
				name,
				Collections.singleton(
						localSubscription
				)
		);
		Subscription subscription = new Subscription(
				SubscriptionStatus.REQUESTED,
				"a = b",
				"",
				"a=b"
		);
		subscription.setExchangeName(exchangeName);
		Match match = new Match(
				localSubscription,
				subscription
		);
		when(serviceProviderRepository.findAll()).thenReturn(Arrays.asList(serviceProvider));
		when(matchRepository.findAllByLocalSubscriptionId(localSubscription.getId())).thenReturn(Arrays.asList(match));
		router.createBindingsWithMatches();
		//TODO asserts, verify that the methods are called
		assertThat(client.queueExists(source)).isFalse();
	}


	//TODO the queue might not be created, either

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}
}
