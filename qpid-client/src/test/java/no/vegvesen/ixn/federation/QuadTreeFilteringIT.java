package no.vegvesen.ixn.federation;

import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.shared.Constants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static no.vegvesen.ixn.docker.DockerBaseIT.getDockerHost;
import static no.vegvesen.ixn.docker.DockerBaseIT.getTargetFolderPathForTestClass;
import static no.vegvesen.ixn.docker.QpidDockerBaseIT.*;
import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class QuadTreeFilteringIT {

    public static final String HOST_NAME = getDockerHost();
	private static final CaStores stores = generateStores(getTargetFolderPathForTestClass(QuadTreeFilteringIT.class),"my_ca", HOST_NAME,"routing_configurer","king_gustaf");


	@Container
	public static final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("qpid")
			);


	private QpidClient qpidClient;

	@BeforeAll
	public static void reportContainerUrl() {
		System.out.println(qpidContainer.getHttpUrl());
	}

	@BeforeEach
	public void setUp() {
		qpidClient = new QpidClient(
				qpidContainer.getHttpsUrl(),
				qpidContainer.getvHostName(),
				new QpidClientConfig(sslClientContext(stores,"routing_configurer")).qpidRestTemplate()
		);
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')";
		String kingGustaf = "king_gustaf";
		Message receivedMessage = sendAndReceive(messageQuadTreeTiles, selector, kingGustaf,"queue1","exchange1");
		assertThat(receivedMessage).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		String selector = "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')";
		String kingGustaf = "king_gustaf";
        Message message = sendAndReceive(messageQuadTreeTiles, selector, kingGustaf,"queue2","exchange2");
		assertThat(message).isNull();
	}

        @Test
        public void matchingFilterAndQuadTreeExactMatchGetsRouted() throws Exception {
            String messageQuadTreeTiles = ",abcdefghijklmnop";
            String selector = "(originatingCountry = 'NO') and (quadTree like '%,abcdefghijklmnop%')";
            String kingGustaf = "king_gustaf";
            Message receivedMessage = sendAndReceive(messageQuadTreeTiles, selector, kingGustaf,"queue3","exchange3");
            assertThat(receivedMessage).isNotNull();
        }

        @Test
        public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
            String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
            String selector = "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')";
            String kingGustaf = "king_gustaf";
            Message receivedMessage = sendAndReceive(messageQuadTreeTiles, selector, kingGustaf,"queue4","exchange4");
            assertThat(receivedMessage).isNull();
        }

        @Test
        public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
            String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
            String selector = "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')";
            String kingGustaf = "king_gustaf";
            Message receivedMessage = sendAndReceive(messageQuadTreeTiles, selector, kingGustaf,"queue5","exchange5" );
            assertThat(receivedMessage).isNull();
        }

		/*
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


     */
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

	private Message sendAndReceive(String messageQuadTreeTiles, String selector, String spName, String queueName, String exchangeName) throws Exception {
		qpidClient.createQueue(queueName);
		qpidClient.addReadAccess(spName, queueName);
		qpidClient.createHeadersExchange(exchangeName);
		qpidClient.addBinding(exchangeName , new Binding(exchangeName, queueName, new Filter(selector)));
		qpidClient.addWriteAccess(spName, exchangeName);

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
