package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.qpid.RoutingConfigurerProperties;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.assertj.core.api.Assertions;
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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {ServiceProviderRouter.class, QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {ServiceProviderRouterIT.Initializer.class})
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {


	private static Path testKeysPath = getFolderPath("target/test-keys" + ServiceProviderRouterIT.class.getSimpleName());

	@Container
	private static GenericContainer keyContainer = getKeyContainer(testKeysPath,"my_ca", "localhost", "routing_configurer", "king_gustaf");

    @SuppressWarnings("rawtypes")
	@Container
    public static final GenericContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost")
			.dependsOn(keyContainer);

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);
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
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	QpidClient client;

 	@Autowired
	ServiceProviderRouter router;

	@Test
	public void newServiceProviderCanAddSubscriptionsThatWillBindToTheQueue() {
		ServiceProvider nordea = new ServiceProvider("nordea");
		nordea.addLocalSubscription(createSubscription("DATEX2", "NO"));
		router.syncServiceProviders(Arrays.asList(nordea));
		Set<String> nordeaBindKeys = client.getQueueBindKeys("nordea");
		assertThat(nordeaBindKeys).hasSize(1);

		nordea.addLocalSubscription(createSubscription("DATEX2", "FI"));
		router.syncServiceProviders(Arrays.asList(nordea));
		nordeaBindKeys = client.getQueueBindKeys("nordea");
		assertThat(nordeaBindKeys).hasSize(2);
	}

	private LocalSubscription createSubscription(String messageType, String originatingCountry) {
		DataType dataType = new DataType();
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		dataType.setValues(values);
		String selector = "messageType = '" + messageType + "' and originatingCountry = '" + originatingCountry +"'";
		return new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,""));

		router.syncServiceProviders(Arrays.asList(king_gustaf));

		SSLContext kingGustafSslContext = setUpTestSslContext("king_gustaf.p12");
		Sink readKingGustafQueue = new Sink(AMQPS_URL, "king_gustaf", kingGustafSslContext);
		readKingGustafQueue.start();
		Source writeOnrampQueue = new Source(AMQPS_URL, "onramp", kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(AMQPS_URL, "onramp", kingGustafSslContext);
			readDlqueue.start();
			fail("Should not allow king_gustaf to read from queue not granted access on (onramp)");
		} catch (Exception ignore) {
		}
	}

	@Test
	public void subscriberToreDownWillBeRemovedFromSubscribFederatedInterchangesGroup() {
		ServiceProvider toreDownServiceProvider = new ServiceProvider("tore-down-service-provider");
		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "");
		toreDownServiceProvider.addLocalSubscription(subscription);

		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(toreDownServiceProvider.getName());
		assertThat(subscription.getStatus().equals(LocalSubscriptionStatus.CREATED));
		assertThat(client.queueExists(toreDownServiceProvider.getName())).isTrue();

		toreDownServiceProvider.setSubscriptions(
				toreDownServiceProvider.getSubscriptions().stream()
						.map(localSubscription -> localSubscription.withStatus(LocalSubscriptionStatus.TEAR_DOWN))
						.collect(Collectors.toSet()));
		router.syncServiceProviders(Arrays.asList(toreDownServiceProvider));
		assertThat(toreDownServiceProvider.getSubscriptions()).isEmpty();
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(toreDownServiceProvider.getName());
		assertThat(client.queueExists(toreDownServiceProvider.getName())).isFalse();
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
	public void doNotSetUpQueueWhenOnlySubscriptionHasCreateNewQueue() {
		LocalSubscription sub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
				"AND messageType = 'DATEX2' " +
				"AND originatingCountry = 'NO'", true, "my-service-provider1");

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider");
		serviceProvider.addLocalSubscription(sub);
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).doesNotContain(serviceProvider.getName());
		assertThat(client.queueExists(serviceProvider.getName())).isFalse();
	}

	@Test
	public void doSetUpQueueWhenSubscriptionHasCreateNewQueueAndNot() {
		LocalSubscription sub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'NO'", true, "my-service-provider");
		LocalSubscription sub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED,
				"((quadTree like '%,01230122%') OR (quadTree like '%,01230123%'))" +
						"AND messageType = 'DATEX2' " +
						"AND originatingCountry = 'SE'", false, "");

		ServiceProvider serviceProvider = new ServiceProvider("my-service-provider2");
		serviceProvider.addLocalSubscription(sub1);
		serviceProvider.addLocalSubscription(sub2);
		router.syncServiceProviders(Arrays.asList(serviceProvider));
		assertThat(client.getGroupMemberNames(QpidClient.SERVICE_PROVIDERS_GROUP_NAME)).contains(serviceProvider.getName());
		assertThat(client.queueExists(serviceProvider.getName())).isTrue();
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
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).contains(privateChannel.getClientName());
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
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).doesNotContain(privateChannel.getClientName());
		assertThat(client.getGroupMemberNames(QpidClient.CLIENTS_PRIVATE_CHANNELS_GROUP_NAME)).doesNotContain(serviceProvider.getName());

	}

   	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}
