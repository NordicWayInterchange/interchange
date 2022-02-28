package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import no.vegvesen.ixn.federation.SelectorBuilder;
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
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
@ContextConfiguration(initializers = {QuadTreeFilteringIT.Initializer.class})
@Testcontainers
public class QuadTreeFilteringIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QuadTreeFilteringIT.class);
	private static Path testKeysPath = generateKeys(QuadTreeFilteringIT.class, "my_ca", "localhost", "routing_configurer", "king_gustaf");

	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password","localhost");

	private static String AMQPS_URL;

	static class Initializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
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

	@Autowired
	private QpidClient qpidClient;

	@BeforeEach
	public void setUp() {
		//It is not normal for a service provider to be administrator - just to avoid setting up InterchangeApp by letting service provider send to outgoingExchange
		List<String> administrators = qpidClient.getGroupMemberNames("administrators");
		if (!administrators.contains("king_gustaf")) {
			qpidClient.addMemberToGroup("king_gustaf", "administrators");
		}
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf);
		Message message = receivedMessage;
		assertThat(message).isNull();
	}

	@Test
	public void matchingFilterAndQuadTreeExactMatchGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",abcdefghijklmnop";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefghijklmnop%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf);
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf);
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf);
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void sendMessageOverlappingQuadAndOriginatingCountry() throws Exception {
		SelectorBuilder datexNoAbcdef = new SelectorBuilder()
				.messageType("DATEX2")
				.originatingCountry("NO")
				.quadTree("abcdef");
		String dataTypeSelector = datexNoAbcdef.toSelector();
		String kingGustaf = "king_gustaf";
		String messageQuadTreeTiles = ",abcdefghijklmno,cdefghijklmnop";
		Message receivedMessage = sendMessageServiceProvider(kingGustaf, dataTypeSelector, messageQuadTreeTiles, "" + dataTypeSelector.hashCode());
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void sendMessageWhereQuadTreeTileIsLongerThanEighteen() throws Exception {
		SelectorBuilder datexNoAbcdef = new SelectorBuilder()
				.messageType("DATEX2")
				.originatingCountry("NO")
				.quadTree("abcdefghijklmnopqrs");
		String selector = datexNoAbcdef.toSelector();
		String kingGustaf = "king_gustaf";
		String messageQuadTreeTiles = ",abcdefghijklmnopqrs,cdefghijklmnop";
		Message receivedMessage = sendMessageServiceProvider(kingGustaf, selector, messageQuadTreeTiles, Integer.toString(selector.hashCode()));
		assertThat(receivedMessage).isNotNull();
	}

	private Message sendMessageServiceProvider(String serviceProviderName, String selector, String messageQuadTreeTiles, String bindKey) throws Exception {
		qpidClient.createQueue(serviceProviderName);
		qpidClient.addReadAccess(serviceProviderName, serviceProviderName);
		qpidClient.addBinding(selector, serviceProviderName, bindKey, "outgoingExchange");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");

		Sink sink = new Sink(AMQPS_URL, serviceProviderName, sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "outgoingExchange", sslContext);
		source.start();
		source.send(source.createMessageBuilder()
				.textMessage("fisk")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("NO")
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build());
		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

	private Message sendNeighbourMessage(String messageQuadTreeTiles, String selector, String kingGustaf) throws Exception {
		qpidClient.createQueue(kingGustaf);
		qpidClient.addReadAccess(kingGustaf, kingGustaf);
		qpidClient.addBinding(selector, kingGustaf, Integer.toString(selector.hashCode()), "outgoingExchange");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");

		Sink sink = new Sink(AMQPS_URL, kingGustaf, sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "outgoingExchange", sslContext);
		source.start();
		if (messageQuadTreeTiles != null && !messageQuadTreeTiles.startsWith(",")) {
			throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}
		source.send(source.createMessageBuilder()
				.textMessage("fisk")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("NO")
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build());

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

}
