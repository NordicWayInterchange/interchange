package no.vegvesen.ixn.federation.qpid;

import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.SelectorBuilder;
import no.vegvesen.ixn.federation.TestSSLContextConfigGeneratedExternalKeys;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.ssl.TestSSLProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, RoutingConfigurerProperties.class, TestSSLContextConfigGeneratedExternalKeys.class, TestSSLProperties.class})
public class QuadTreeFilteringIT extends QpidDockerBaseIT {

    public static final String HOST_NAME = getDockerHost();
	private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(QuadTreeFilteringIT.class),"my_ca", HOST_NAME,"routing_configurer","king_gustaf");


	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
			);

	@DynamicPropertySource
	static void datasourceProperties(DynamicPropertyRegistry registry) {
		registry.add("routing-configurer.baseUrl", qpidContainer::getHttpsUrl);
		registry.add("routing-configurer.vhost", () -> "localhost");
		registry.add("test.ssl.trust-store", () -> getTrustStorePath(stores));
		registry.add("test.ssl.keystore-password", () -> stores.trustStore().password());
		registry.add("test.ssl.key-store", () -> getClientStorePath("routing_configurer", stores.clientStores()));
	}

	@Autowired
	private QpidClient qpidClient;

	@BeforeAll
	static void setup(){
		qpidContainer.start();
	}

	@BeforeEach
	public void setUp() {
		//It is not normal for a service provider to be administrator - just to avoid setting up InterchangeApp by letting service provider send to outgoingExchange
		GroupMember groupMember = qpidClient.getGroupMember("king_gustaf", "administrators");
		if (groupMember == null) {
			qpidClient.addMemberToGroup("king_gustaf", "administrators");
		}
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf,"queue1","exchange1");
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')";
		String kingGustaf = "king_gustaf";
        Message message = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf,"queue2","exchange2");
		assertThat(message).isNull();
	}

	@Test
	public void matchingFilterAndQuadTreeExactMatchGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",abcdefghijklmnop";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefghijklmnop%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf,"queue3","exchange3");
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf,"queue4","exchange4");
		assertThat(receivedMessage).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendNeighbourMessage(messageQuadTreeTiles, selector, kingGustaf,"queue5","exchange5" );
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
		Message receivedMessage = sendMessageServiceProvider(kingGustaf, dataTypeSelector, messageQuadTreeTiles,"spQ1","spEx1");
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
		Message receivedMessage = sendMessageServiceProvider(kingGustaf, selector, messageQuadTreeTiles, "spQ2","spEx2");
		assertThat(receivedMessage).isNotNull();
	}

	private Message sendMessageServiceProvider(String serviceProviderName, String selector, String messageQuadTreeTiles, String queueName, String exchangeName) throws Exception {
		qpidClient.createQueue(queueName);
		qpidClient.addReadAccess(serviceProviderName, queueName);
		qpidClient.createHeadersExchange(exchangeName);
		qpidClient.addBinding(exchangeName, new Binding(exchangeName, queueName, new Filter(selector)));

		SSLContext sslContext = sslClientContext(stores, "king_gustaf");

		Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext);
		source.start();
		source.sendNonPersistentMessage(source.createMessageBuilder()
				.textMessage("fisk")
				.userId(HOST_NAME)
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.publisherName("publishername")
				.protocolVersion("DATEX2;2.3")
				.publisherId("NO-123")
				.publicationId("NO-123-pub")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("NO")
				.shardId(1)
				.shardCount(1)
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build());
		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

	private Message sendNeighbourMessage(String messageQuadTreeTiles, String selector, String spName, String queueName, String exchangeName) throws Exception {
		qpidClient.createQueue(queueName);
		qpidClient.addReadAccess(spName, queueName);
		qpidClient.createHeadersExchange(exchangeName);
		qpidClient.addBinding(exchangeName , new Binding(exchangeName, queueName, new Filter(selector)));

		SSLContext sslContext = sslClientContext(stores, "king_gustaf");

		Sink sink = new Sink(qpidContainer.getAmqpsUrl(), queueName, sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(qpidContainer.getAmqpsUrl(), exchangeName, sslContext);
		source.start();
		if (messageQuadTreeTiles != null && !messageQuadTreeTiles.startsWith(",")) {
			throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}
		source.sendNonPersistentMessage(source.createMessageBuilder()
				.textMessage("fisk")
				.userId(HOST_NAME)
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.publisherName("publishername")
				.protocolVersion("DATEX2;2.3")
				.publisherId("NO-123")
				.publicationId("NO-123-pub")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("NO")
				.shardId(1)
				.shardCount(1)
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build());

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}

}
