package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import javax.jms.*;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
public class AccessControlIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(AccessControlIT.class);

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "jks/king_gustaf.p12";
	private static final String JKS_IMPOSTER_KING_HARALD_P_12 = "jks/imposter_king_harald.p12";
	private static final String TRUSTSTORE_JKS = "jks/truststore.jks";

	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";

	@ClassRule
	public static GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private String getQpidURI() {
		String url = "amqps://localhost:" + localContainer.getMappedPort(AMQPS_PORT);
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	@Test(expected = JMSSecurityException.class)
	public void testKingHaraldCanNotConsumeSE_OUT() throws Exception {
		IxnContext context = new IxnContext(getQpidURI(), "onramp", SE_OUT);

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.createConsumer(queueR);
	}

	@Test(expected = JMSSecurityException.class)
	public void testKingGustafCanNotConsumeNO_OUT() throws Exception {
		IxnContext context = new IxnContext(getQpidURI(), "onramp", NO_OUT);
		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		session.createConsumer(queueR);
	}

	@Test(expected = JMSSecurityException.class)
	public void KingHaraldCanNotConsumeFromOnramp() throws Exception {
		IxnContext context = new IxnContext(getQpidURI(), "onramp", "onramp");
		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		Destination queueR = context.getReceiveQueue();
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		session.createConsumer(queueR);
	}


	@Test(expected = JMSException.class)
	public void KingHaraldCanNotSendToNwEx() throws Exception {
		IxnContext context = new IxnContext(getQpidURI(), "nwEx", NO_OUT);

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
		IxnContext context = new IxnContext(getQpidURI(), "onramp", "test-out");

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_IMPOSTER_KING_HARALD_P_12, TRUSTSTORE_JKS));
		connection.start();
	}

	@Test
	public void userWithValidCertificateCanConnect() throws Exception {
		IxnContext context = new IxnContext(getQpidURI(), "onramp", NO_OUT);

		Connection connection = context.createConnection(TestKeystoreHelper.sslContext(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS));
		connection.start();
		connection.close();
	}

}
