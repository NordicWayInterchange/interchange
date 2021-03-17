package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {QuadTreeFilteringIT.Initializer.class})
@Testcontainers
public class QuadTreeFilteringIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QuadTreeFilteringIT.class);
	private static Path testKeysPath = generateKeys(QuadTreeFilteringIT.class, "my_ca", "localhost", "routing_configurer", "king_gustaf");

	@Container
	public static final GenericContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost");

	private static String AMQPS_URL;

	static class Initializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
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

	@Autowired
	private QpidClient qpidClient;

	@BeforeEach
	public void setUp() {
		//It is not normal for a service provider to be administrator - just to avoid setting up InterchangeApp by letting service provider send to nwEx
		List<String> administrators = qpidClient.getGroupMemberNames("administrators");
		if (!administrators.contains("king_gustaf")) {
			qpidClient.addMemberToGroup("king_gustaf", "administrators");
		}
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	@Test
	public void matchingFilterAndQuadTreeExactMatchGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",abcdefghijklmnop";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,abcdefghijklmnop%')");
		assertThat(message).isNotNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	private Message sendReceiveMessageNeighbour(String messageQuadTreeTiles, String selector) throws Exception {
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED, false, "");
		Neighbour king_gustaf = new Neighbour("king_gustaf", null, new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(subscription)), null);
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.getSelector(), king_gustaf.getName(), subscription.bindKey(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");

		Sink sink = new Sink(AMQPS_URL, "king_gustaf", sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "nwEx", sslContext);
		source.start();
		source.sendNonPersistent("fisk", "NO", messageQuadTreeTiles);

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

	@Test
	public void sendMessageOverlappingQuadAndOriginatingCountry() throws Exception {
		Map<String, String> props = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		props.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		props.put(MessageProperty.QUAD_TREE.getName(), "abcdef");
		DataType datexNoAbcdef = new DataType(props);
		Message recievedMsg = sendReceiveMessageServiceProvider(",abcdefghijklmno,cdefghijklmnop", datexNoAbcdef);
		assertThat(recievedMsg).isNotNull();
	}

	@Test
	public void sendMessageWhereQuadTreeTileIsLongerThanEighteen() throws Exception {
		Map<String, String> props = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		props.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		props.put(MessageProperty.QUAD_TREE.getName(), "abcdefghijklmnopqrs");
		DataType datexNoAbcdef = new DataType(props);
		Message recievedMsg = sendReceiveMessageServiceProvider(",abcdefghijklmnopqrs,cdefghijklmnop", datexNoAbcdef);
		assertThat(recievedMsg).isNotNull();
	}

	private Message sendReceiveMessageServiceProvider(String messageQuadTreeTiles, DataType subscription) throws Exception {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'Datex2' and originatingCountry= 'NO' and quadTree like '%,abcdef%"));
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.toSelector(), king_gustaf.getName(), ""+subscription.toSelector().hashCode(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");

		Sink sink = new Sink(AMQPS_URL, "king_gustaf", sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "nwEx", sslContext);
		source.start();
		source.sendNonPersistent("fisk", "NO", messageQuadTreeTiles);

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

}
