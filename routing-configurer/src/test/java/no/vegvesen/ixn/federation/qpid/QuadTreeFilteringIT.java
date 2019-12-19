package no.vegvesen.ixn.federation.qpid;

import com.google.common.collect.Sets;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.net.ssl.SSLContext;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {QpidClient.class, QpidClientConfig.class, TestSSLContextConfig.class})
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {QuadTreeFilteringIT.Initializer.class})
public class QuadTreeFilteringIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(QuadTreeFilteringIT.class);

	@ClassRule
	public static GenericContainer qpidContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private static String AMQPS_URL;

	static class Initializer  implements ApplicationContextInitializer<ConfigurableApplicationContext> {
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
	private QpidClient qpidClient;

	@Before
	public void setUp() {
		//It is not normal for a service provider to be administrator - just to avoid setting up InterchangeApp by letting service provider send to nwEx
		List<String> administrators = qpidClient.getInterchangesUserNames("administrators");
		if (!administrators.contains("king_gustaf")) {
			qpidClient.addInterchangeUserToGroups("king_gustaf", "administrators");
		}
	}

	@Test
	public void matchingFilterAndQuadTreeGetsRouted() throws Exception {
		HashSet<String> subscriptionQuadTreeTiles = Sets.newHashSet("abcdefgh");
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessage(subscriptionQuadTreeTiles, messageQuadTreeTiles, "originatingCountry = 'NO'");
		assertThat(message).isNotNull();
	}

	@Test
	public void matchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		HashSet<String> subscriptionQuadTreeTiles = Sets.newHashSet("cdefghij");
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessage(subscriptionQuadTreeTiles, messageQuadTreeTiles, "originatingCountry = 'NO'");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndMatcingQuadTreeDoesNotGetRouted() throws Exception {
		HashSet<String> subscriptionQuadTreeTiles = Sets.newHashSet("abcdefgh");
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessage(subscriptionQuadTreeTiles, messageQuadTreeTiles, "originatingCountry = 'SE'");
		assertThat(message).isNull();
	}

	@Test
	public void nonMatchingFilterAndNonMatcingQuadTreeDoesNotGetRouted() throws Exception {
		HashSet<String> subscriptionQuadTreeTiles = Sets.newHashSet("cdefghij");
		String messageQuadTreeTiles = ",somerandomtile,abcdefghijklmnop,anotherrandomtile,";
		Message message = sendReceiveMessage(subscriptionQuadTreeTiles, messageQuadTreeTiles, "originatingCountry = 'SE'");
		assertThat(message).isNull();
	}

	private Message sendReceiveMessage(HashSet<String> subscriptionQuadTreeTiles, String messageQuadTreeTiles, String selector) throws Exception {
		ServiceProvider king_gustaf = new ServiceProvider("king_gustaf");
		Subscription subscription = new Subscription(selector, SubscriptionStatus.REQUESTED);
		subscription.setQuadTreeTiles(subscriptionQuadTreeTiles);
		king_gustaf.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Sets.newHashSet(subscription)));
		qpidClient.setupRouting(king_gustaf, "nwEx");

		SSLContext sslContext = TestKeystoreHelper.sslContext("jks/king_gustaf.p12", "jks/truststore.jks");

		Sink sink = new Sink(AMQPS_URL, "king_gustaf", sslContext);
		MessageConsumer consumer = sink.createConsumer();

		Source source = new Source(AMQPS_URL, "nwEx", sslContext);
		source.start();
		source.send("fisk", "NO", messageQuadTreeTiles);

		Message receivedMessage = consumer.receive(1000);
		sink.close();
		source.close();
		return receivedMessage;
	}
}
