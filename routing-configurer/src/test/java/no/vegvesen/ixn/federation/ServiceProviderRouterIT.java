package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {ServiceProviderRouter.class, QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {ServiceProviderRouterIT.Initializer.class})
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {


	private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

	@Container
	private static KeysContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf");

	@SuppressWarnings("rawtypes")
	@Container
    public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
			.dependsOn(keyContainer);


 	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
		    //need to set the logg follower somewhere, this seems like a "good" place to do it for now
			qpidContainer.followOutput(new Slf4jLogConsumer(logger));
			String httpsUrl = qpidContainer.getHttpsUrl();
			String httpUrl = qpidContainer.getHttpUrl();
			logger.info("server url: " + httpsUrl);
			logger.info("server url: " + httpUrl);
			TestPropertyValues.of(
					"routing-configurer.baseUrl=" + httpsUrl,
					"routing-configurer.vhost=localhost",
					"test.ssl.trust-store=" + testKeysPath.resolve("truststore.jks"),
					"test.ssl.key-store=" +  testKeysPath.resolve("routing_configurer.p12")
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
	MatchDiscoveryService matchDiscoveryService;

	@Test
	public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
		ServiceProvider nordea = new ServiceProvider("nordea");
		String queueName1 = "my-queue-1";
		String queueName2 = "my-queue-2";
		nordea.addLocalSubscription(createSubscription("DATEX2", "NO", "", queueName1));
		router.syncServiceProviders(Arrays.asList(nordea));
		Set<String> nordeaBindKeys1 = client.getQueueBindKeys(queueName1);
		assertThat(nordeaBindKeys1).hasSize(1);

		nordea.addLocalSubscription(createSubscription("DATEX2", "FI", "", queueName2));
		router.syncServiceProviders(Arrays.asList(nordea));
		Set<String> nordeaBindKeys2 = client.getQueueBindKeys(queueName2);
		assertThat(nordeaBindKeys2).hasSize(1);
	}

	private LocalSubscription createSubscription(String messageType, String originatingCountry, String consumerCommonName, String queueName) {
		String selector = "messageType = '" + messageType + "' and originatingCountry = '" + originatingCountry +"'";
		return new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector, consumerCommonName, queueName);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		String queueName = "king_gustaf_queue";
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"", "", queueName));

		router.syncServiceProviders(Arrays.asList(king_gustaf));

		SSLContext kingGustafSslContext = setUpTestSslContext("king_gustaf.p12");
		String amqpsUrl = qpidContainer.getAmqpsUrl();
		Sink readKingGustafQueue = new Sink(amqpsUrl, queueName, kingGustafSslContext);
		readKingGustafQueue.start();
		Source writeOnrampQueue = new Source(amqpsUrl, "onramp", kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(amqpsUrl, "onramp", kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on (onramp)");
		} catch (Exception ignore) {
		}
	}

	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribFederatedInterchangesGroup() {
		Subscription subscription = new Subscription();
		subscription.setExchangeName("subscription-exchange");
		subscription.setSubscriptionStatus(SubscriptionStatus.REQUESTED);

		ServiceProvider toreDownServiceProvider = new ServiceProvider("tore-down-service-provider");
		String queueName = "my-queue";
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "", queueName);
		toreDownServiceProvider.addLocalSubscription(localSubscription);

		Match match = new Match(localSubscription, subscription, "tore-down-service-provider", MatchStatus.SETUP_EXCHANGE);

		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Arrays.asList(match));

		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(toreDownServiceProvider.getName());
		assertThat(localSubscription.getStatus().equals(LocalSubscriptionStatus.CREATED));
		assertThat(client.exchangeExists("subscription-exchange")).isTrue();
		assertThat(client.queueExists(queueName)).isTrue();


		toreDownServiceProvider.setSubscriptions(
				toreDownServiceProvider.getSubscriptions().stream()
						.map(localSubscription1 -> localSubscription1.withStatus(LocalSubscriptionStatus.TEAR_DOWN))
						.collect(Collectors.toSet()));
		LocalSubscription tearDownLocalSubscription = toreDownServiceProvider.getSubscriptions().stream().findFirst().get();

		Match tearDownMatch = new Match(tearDownLocalSubscription, subscription, "tore-down-service-provider", MatchStatus.TEARDOWN_EXCHANGE);
		when(matchDiscoveryService.findMatchesToTearDownExchangesFor(any(String.class))).thenReturn(Arrays.asList(tearDownMatch));
		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Collections.emptyList());

		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(toreDownServiceProvider.getName());
		assertThat(client.exchangeExists("subscription-exchange")).isFalse();
		assertThat(client.queueExists(queueName)).isFalse();
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
	public void doNotSetUpQueueWhenOnlySubscriptionHasSameConsumerCommonNameAsServiceProviderName() {
		String queueName = "my-queue";
		LocalSubscription sub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO'", "my-service-provider-3", queueName);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider-3");
		serviceProvider.addLocalSubscription(sub);
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(serviceProvider.getName());
		assertThat(client.queueExists(serviceProvider.getName())).isFalse();
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasConsumerCommonNameSameAsIxnNameAndServiceProviderName() {
		String queueName1 = "my-queue-1";
		String queueName2 = "my-queue-2";
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'", "my-service-provider", queueName1);
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'", "", queueName2);

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		serviceProvider.addLocalSubscription(sub1);
		serviceProvider.addLocalSubscription(sub2);
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(serviceProvider.getName());
		assertThat(client.queueExists(queueName2)).isTrue();
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
	public void setUpSubscriptionExchange() {
		String serviceProviderName = "my-service-provider";
		String queueName = "my-queue";
		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		String selector = "a=b";
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "", queueName);
		serviceProvider.addLocalSubscription(localSubscription);
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED);
		subscription.setExchangeName("subscription-exchange");

		client.createQueue(queueName);

		Match match = new Match(localSubscription, subscription, serviceProviderName, MatchStatus.SETUP_EXCHANGE);

		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Arrays.asList(match));

		router.setUpSubscriptionExchanges(serviceProviderName);

		assertThat(client.exchangeExists(subscription.getExchangeName())).isTrue();
	}

	@Test
	public void tearDownSubscriptionExchange() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector);
		Subscription subscription = new Subscription(selector, SubscriptionStatus.TEAR_DOWN);
		subscription.setExchangeName("subscription-exchange");

		client.createQueue(serviceProviderName);

		Match match = new Match(localSubscription, subscription, MatchStatus.TEARDOWN_EXCHANGE);

		when(matchDiscoveryService.findMatchesToTearDownExchangesFor(any(String.class))).thenReturn(Arrays.asList(match));

		router.tearDownSubscriptionExchanges(serviceProviderName);

		assertThat(client.exchangeExists(subscription.getExchangeName())).isFalse();
	}

	@Test
	public void setupAndDeleteSubscriptionExchangeAndQueue() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		String queueName = "my-queue";
		String exchangeName = "my-exchange";
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "", queueName);
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED);
		subscription.setExchangeName(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		serviceProvider.addLocalSubscription(localSubscription);

		Match match = new Match(localSubscription, subscription, MatchStatus.SETUP_EXCHANGE);
		match.setServiceProviderName(serviceProviderName);

		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Arrays.asList(match));

		router.syncServiceProviders(Arrays.asList(serviceProvider));

		assertThat(client.exchangeExists(exchangeName)).isTrue();
		assertThat(client.queueExists(queueName)).isTrue();

		Match match1 = new Match(localSubscription, subscription, MatchStatus.TEARDOWN_EXCHANGE);

		when(matchDiscoveryService.findMatchesToTearDownExchangesFor(any(String.class))).thenReturn(Arrays.asList(match1));
		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Collections.emptyList());

		router.syncServiceProviders(Arrays.asList(serviceProvider));

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(client.queueExists(queueName)).isTrue();
	}

	@Test
	public void setupAndDeleteSubscriptionExchange() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		String queueName = "my-queue";
		String exchangeName = "my-exchange";
		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.CREATED, selector, "", queueName);
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED);
		subscription.setExchangeName(exchangeName);

		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		serviceProvider.addLocalSubscription(localSubscription);

		Match match = new Match(localSubscription, subscription, MatchStatus.SETUP_EXCHANGE);
		match.setServiceProviderName(serviceProviderName);

		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Arrays.asList(match));

		router.syncServiceProviders(Arrays.asList(serviceProvider));

		assertThat(client.exchangeExists(exchangeName)).isTrue();
		assertThat(client.queueExists(queueName)).isTrue();

		localSubscription.setStatus(LocalSubscriptionStatus.TEAR_DOWN);

		Match match1 = new Match(localSubscription, subscription, MatchStatus.TEARDOWN_EXCHANGE);

		when(matchDiscoveryService.findMatchesToTearDownExchangesFor(any(String.class))).thenReturn(Arrays.asList(match1));
		when(matchDiscoveryService.findMatchesToSetupExchangesFor(any(String.class))).thenReturn(Collections.emptyList());

		router.syncServiceProviders(Arrays.asList(serviceProvider));

		assertThat(client.exchangeExists(exchangeName)).isFalse();
		assertThat(client.queueExists(queueName)).isFalse();
	}

	@Test
	public void tearDownQueueWhenLocalSubscriptionIsDeletedAfterMatch() {
		String serviceProviderName = "my-service-provider";
		String selector = "a=b";
		String queueName = "my-queue";

		LocalSubscription localSubscription = new LocalSubscription(LocalSubscriptionStatus.TEAR_DOWN, selector, "", queueName);

		ServiceProvider serviceProvider = new ServiceProvider(serviceProviderName);
		serviceProvider.addLocalSubscription(localSubscription);

		client.createQueue(queueName);

		when(matchDiscoveryService.findMatchByLocalSubscriptionId(any(Integer.class))).thenReturn(null);

		router.processSubscription(serviceProviderName, localSubscription);

		assertThat(client.queueExists(queueName)).isFalse();

	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}
