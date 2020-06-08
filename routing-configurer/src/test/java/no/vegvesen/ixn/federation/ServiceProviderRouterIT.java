package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.ssl.TestSSLContextConfig;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class, TestSSLProperties.class})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = {ServiceProviderRouterIT.Initializer.class})
@ConfigurationPropertiesScan
@Testcontainers
public class ServiceProviderRouterIT extends QpidDockerBaseIT {

    @SuppressWarnings("rawtypes")
	@Container
    public static final GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

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
					"qpid.rest.api.baseUrl=" + httpsUrl,
					"qpid.rest.api.vhost=localhost"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

	@Autowired
	QpidClient client;
	ServiceProviderRouter router;

	@BeforeEach
	public void setUp() {
 		router = new ServiceProviderRouter(mock(ServiceProviderRepository.class),client);
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
	}

	private LocalSubscription createSubscription(String messageType, String originatingCountry) {
		DataType dataType = new DataType();
		HashMap<String, String> values = new HashMap<>();
		values.put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		values.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		dataType.setValues(values);
		return new LocalSubscription(LocalSubscriptionStatus.REQUESTED, dataType);
	}

	@Test
	public void newServiceProviderCanReadDedicatedOutQueue() throws NamingException, JMSException {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,new DataType()));

		router.syncServiceProviders(Arrays.asList(king_gustaf));

		SSLContext kingGustafSslContext = setUpTestSslContext("jks/king_gustaf.p12");
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
		LocalSubscription subscription = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, new DataType());
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


   	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(getFilePathFromClasspathResource(s), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(getFilePathFromClasspathResource("jks/truststore.jks"), "password", KeystoreType.JKS));
	}

	private static String getFilePathFromClasspathResource(String classpathResource) {
		URL resource = Thread.currentThread().getContextClassLoader().getResource(classpathResource);
		if (resource != null) {
			return resource.getFile();
		}
		throw new RuntimeException("Could not load classpath resource " + classpathResource);
	}


}
