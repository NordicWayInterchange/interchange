package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import javax.jms.*;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {AccessControlIT.Initializer.class})
public class AccessControlIT extends DockerBaseIT {

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "jks/king_gustaf.p12";
	private static final String JKS_IMPOSTER_KING_HARALD_P_12 = "jks/imposter_king_harald.p12";
	private static final String TRUSTSTORE_JKS = "jks/truststore.jks";

	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";

	private static String URI;

	@ClassRule
	public static GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	@ClassRule
	public static GenericContainer postgisContainer = getPostgisContainer("interchangenode/src/test/docker/postgis");


	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			Integer amqpsPort = localContainer.getMappedPort(AMQPS_PORT);
			URI = "amqps://localhost:" + amqpsPort;
			TestPropertyValues.of(
					"amqphub.amqp10jms.remote-url=" + URI,
					"amqphub.amqp10jms.username=interchange",
					"amqphub.amqp10jms.password=12345678",
					"spring.datasource.url: jdbc:postgresql://localhost:" + postgisContainer.getMappedPort(JDBC_PORT) + "/geolookup",
					"spring.datasource.username: geolookup",
					"spring.datasource.password: geolookup",
					"spring.datasource.driver-class-name: org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}


	@Test(expected = JMSSecurityException.class)
	public void testKingHaraldCanNotConsumeSE_OUT() throws Exception {
		IxnContext context = new IxnContext(URI, "onramp", SE_OUT);

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.createConsumer(queueR);
	}

	@Test(expected = JMSSecurityException.class)
	public void testKingGustafCanNotConsumeNO_OUT() throws Exception {
		IxnContext context = new IxnContext(URI, "onramp", NO_OUT);
		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		session.createConsumer(queueR);
	}

	@Test(expected = JMSSecurityException.class)
	public void KingHaraldCanNotConsumeFromOnramp() throws Exception {
		IxnContext context = new IxnContext(URI, "onramp", "onramp");
		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.createConsumer(queueR);
	}


	@Test(expected = JMSException.class)
	public void KingHaraldCanNotSendToNwEx() throws Exception {
		IxnContext context = new IxnContext(URI, "nwEx", NO_OUT);

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		Destination queueS = context.getSendQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		session.createConsumer(queueR);
		MessageProducer messageProducer = session.createProducer(queueS);

		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello world");
		message.getFacade().setUserId("king_harald");
		message.setStringProperty("who", "Norwegian Public Roads Administration");
		message.setStringProperty("how", "Datex2");
		message.setStringProperty("what", "Conditions");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where", "NO");

		messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test(expected = JMSException.class)
	public void userWithInvalidCertificateCannotConnect() throws Exception {
		IxnContext context = new IxnContext(URI, "onramp", "test-out");

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_IMPOSTER_KING_HARALD_P_12, TRUSTSTORE_JKS));
		connection.start();
	}

	@Test
	public void userWithValidCertificateCanConnect() throws Exception {
		IxnContext context = new IxnContext(URI, "onramp", NO_OUT);

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		connection.start();
	}

}
