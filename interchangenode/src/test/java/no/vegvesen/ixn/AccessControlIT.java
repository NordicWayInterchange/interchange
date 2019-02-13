package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.Test;

import javax.jms.*;

/**
 * Verifies access control lists where username comes from the common name (CN) of the user certificate.
 */
public class AccessControlIT extends IxnBaseIT {

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "jks/king_gustaf.p12";
	private static final String JKS_IMPOSTER_KING_HARALD_P_12 = "jks/imposter_king_harald.p12";
	private static final String TRUSTSTORE_JKS = "jks/truststore.jks";

	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";

	private static final String URI = "amqps://localhost:62671";

	@Test(expected = JMSSecurityException.class)
	public void testKingHaraldCanNotConsumeSE_OUT() throws Exception {
		Session session = getTlsSession(URI, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		session.createConsumer(new JmsQueue(SE_OUT));
	}

	@Test(expected = JMSSecurityException.class)
	public void testKingGustafCanNotConsumeNO_OUT() throws Exception {
		Session session = getTlsSession(URI, JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS);
		session.createConsumer(new JmsQueue(NO_OUT));
	}

	@Test(expected = JMSSecurityException.class)
	public void KingHaraldCanNotConsumeFromOnramp() throws Exception {
		Session session = getTlsSession(URI, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		session.createConsumer(new JmsQueue("onramp"));
	}


	@Test(expected = JMSException.class)
	public void KingHaraldCanNotSendToNwEx() throws Exception {
		Session session = getTlsSession(URI, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		MessageProducer messageProducer = session.createProducer(new JmsQueue("nwEx"));
		MessageConsumer noOut = session.createConsumer(new JmsQueue(NO_OUT));

		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello world");
		message.getFacade().setUserId("king_harald");
		message.setStringProperty("who", "Norwegian Public Roads Administration");
		message.setStringProperty("how", "Datex2");
		message.setStringProperty("what", "Conditions");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where1", "NO");

		messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	@Test(expected = JMSException.class)
	public void userWithInvalidCertificateCannotConnect() throws Exception {
		getTlsSession(URI, JKS_IMPOSTER_KING_HARALD_P_12, TRUSTSTORE_JKS);
	}

	@Test
	public void userWithValidCertificateCanConnect() throws Exception {
		getTlsSession(URI, JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
	}


}
