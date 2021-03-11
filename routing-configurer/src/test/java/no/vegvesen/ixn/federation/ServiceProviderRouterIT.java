package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.DatexCapability;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {


	@Container
	private static KeysContainer keyContainer = DockerBaseIT.getKeysContainer(ServiceProviderRouterIT.class,"my_ca", "localhost", "routing_configurer", "king_gustaf");

	@Container
    public QpidContainer qpidContainer = getQpidTestContainer("qpid", keyContainer.getKeyFolderOnHost(), "localhost.p12", "password", "truststore.jks", "password","localhost")
			.dependsOn(keyContainer);

    private static Logger logger = LoggerFactory.getLogger(ServiceProviderRouterIT.class);

	private ServiceProviderRepository serviceProviderRepository;
	private QpidClient client;
	private ServiceProviderRouter router;

	@BeforeEach
	public void setUp() {
		serviceProviderRepository = mock(ServiceProviderRepository.class);
		client = createClient();
		router = createRouter(serviceProviderRepository, client);

	}

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
		Mockito.verify(serviceProviderRepository,times(2)).save(any());
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		String connectionUrl = qpidContainer.getAmqpsUrl();

		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,""));

		router.syncServiceProviders(Arrays.asList(king_gustaf));

		SSLContext kingGustafSslContext = setUpTestSslContext("king_gustaf.p12");
		Sink readKingGustafQueue = new Sink(connectionUrl, "king_gustaf", kingGustafSslContext);
		readKingGustafQueue.start();
		Source writeOnrampQueue = new Source(connectionUrl, "onramp", kingGustafSslContext);
		writeOnrampQueue.start();
		try {
			Sink readDlqueue = new Sink(connectionUrl, "onramp", kingGustafSslContext);
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

	public ServiceProviderRouter createRouter(ServiceProviderRepository serviceProviderRepository, QpidClient client) {
		return new ServiceProviderRouter(serviceProviderRepository, client);
	}

	public QpidClient createClient() {
		RestTemplate restTemplate = new QpidClientConfig(setUpTestSslContext("routing_configurer.p12")).qpidRestTemplate();
		return new QpidClient(qpidContainer.getHttpsUrl(), "localhost", restTemplate);
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(keyContainer.getKeyFolderOnHost().resolve(s).toString(), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(keyContainer.getKeyFolderOnHost().resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
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

}
