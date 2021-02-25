package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
@Testcontainers
public class QuadTreeFilteringIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QuadTreeFilteringIT.class);
	private static Path testKeysPath = generateKeys(QuadTreeFilteringIT.class,
			"my_ca",
			"localhost", "routing_configurer", "king_gustaf");

	@Container
	public QpidContainer qpidContainer = getQpidTestContainer("quadtree",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

	private String AMQPS_URL;

	private QpidClient qpidClient;

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	@Test
	public void matchingFilterAndQuadTreeExactMatchGetsRouted() throws Exception {

		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		String messageQuadTreeTiles = ",abcdefghijklmnop";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'NO') and (quadTree like '%,abcdefghijklmnop%')");
		assertThat(message).isNotNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,abcdefgh%')");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessageNeighbour(messageQuadTreeTiles, "(originatingCountry = 'SE') and (quadTree like '%,cdefghij%')");
		assertThat(message).isNull();
	}

	@Test
	public void sendMessageOverlappingQuadAndOriginatingCountry() throws Exception {
		AMQPS_URL = qpidContainer.getAmqpsUrl();
		qpidClient = createClient();
		Map<String, String> props = Maps.newHashMap(MessageProperty.MESSAGE_TYPE.getName(), "DATEX2");
		props.put(MessageProperty.ORIGINATING_COUNTRY.getName(), "NO");
		props.put(MessageProperty.QUAD_TREE.getName(), "abcdef");
		DataType datexNoAbcdef = new DataType(props);
		Message recievedMsg = sendReceiveMessageServiceProvider(",abcdefghijklmno,cdefghijklmnop", datexNoAbcdef);
		assertThat(recievedMsg).isNotNull();
	}

	private Message sendReceiveMessageNeighbour(String messageQuadTreeTiles, String selector) throws Exception {
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED, false, "");
		Neighbour king_gustaf = new Neighbour("king_gustaf", null, new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Sets.newLinkedHashSet(subscription)), null);
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.getSelector(), king_gustaf.getName(), subscription.bindKey(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");

		Message receivedMessage;

		try (Source source = new Source(AMQPS_URL, "nwEx", sslContext)) {
			source.start();
			source.sendNonPersistent("fisk", "NO", messageQuadTreeTiles);
		}
		try (MessageConsumer consumer = new Sink(AMQPS_URL, "king_gustaf", sslContext).createConsumer()) {
			receivedMessage = consumer.receive(1000);

		}
		return receivedMessage;
	}

	private Message sendReceiveMessageServiceProvider(String messageQuadTreeTiles, DataType subscription) throws Exception {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		king_gustaf.addLocalSubscription(new LocalSubscription(LocalSubscriptionStatus.REQUESTED,"messageType = 'Datex2' and originatingCountry= 'NO' and quadTree like '%,abcdef%"));
		qpidClient.createQueue(king_gustaf.getName());
		qpidClient.addReadAccess(king_gustaf.getName(), king_gustaf.getName());
		qpidClient.addBinding(subscription.toSelector(), king_gustaf.getName(), ""+subscription.toSelector().hashCode(), "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "king_gustaf.p12", "truststore.jks");
		Message receivedMessage;
		try (Source source = new Source(AMQPS_URL, "nwEx", sslContext)) {
			source.start();
			source.sendNonPersistent("fisk", "NO", messageQuadTreeTiles);
		}

		try (MessageConsumer consumer = new Sink(AMQPS_URL, "king_gustaf", sslContext).createConsumer()) {
			receivedMessage = consumer.receive(1000);
		}


		return receivedMessage;
	}


	public QpidClient createClient() {
		RestTemplate restTemplate = new QpidClientConfig(setUpTestSslContext("routing_configurer.p12")).qpidRestTemplate();
		return new QpidClient(qpidContainer.getHttpsUrl(), qpidContainer.getvHostName(), restTemplate);
	}

	public SSLContext setUpTestSslContext(String s) {
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(testKeysPath.resolve(s).toString(), "password", KeystoreType.PKCS12, "password"),
				new KeystoreDetails(testKeysPath.resolve("truststore.jks").toString(), "password", KeystoreType.JKS));
	}

}
