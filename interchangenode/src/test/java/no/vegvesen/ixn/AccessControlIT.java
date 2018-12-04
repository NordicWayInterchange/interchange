package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.JMSSecurityException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import java.util.Hashtable;

public class AccessControlIT {

	// TODO: Finne ut av hvordan vi skal gi path som er lik for alle miljøer.

	// Keystore and trust store files for integration testing.
	private static final String JKS_KING_HARALD_P_12 = "jks/king_harald.p12";
	private static final String JKS_KING_GUSTAF_P_12 = "jks/king_gustaf.p12";
	private static final String UNCERTIFIED_P_12 = "jks/uncertified.p12";
	private static final String TRUSTSTORE_JKS = "jks/truststore.jks";

	private static final String SE_OUT = "SE-out";
	private static final String NO_OUT = "NO-out";

	private static final String URI = "amqps://localhost:63671";



	@Test
	public void testKingHaraldCanNotConsumeSE_OUT() throws Exception {

		// Set which keystore to use
		TestKeystoreHelper.useTestKeystore(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, SE_OUT, "onramp");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = factory.createConnection();
		Destination queueR = (Destination) context.lookup("receiveQueue");
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		boolean exceptionThrown = false;

		MessageConsumer messageConsumer = null;
		try {
			messageConsumer = session.createConsumer(queueR);
		} catch (JMSSecurityException e) {
			exceptionThrown = true;
		}

		Assert.assertEquals(true, exceptionThrown);

	}

	@Test
	public void testKingGustafCanNotConsumeNO_OUT() throws Exception {

		TestKeystoreHelper.useTestKeystore(JKS_KING_GUSTAF_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, NO_OUT, "onramp");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = factory.createConnection();
		Destination queueR = (Destination) context.lookup("receiveQueue");
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		boolean exceptionThrown = false;

		MessageConsumer messageConsumer = null;
		try {
			messageConsumer = session.createConsumer(queueR);
		} catch (JMSSecurityException e) {
			exceptionThrown = true;
		}

		Assert.assertEquals(true, exceptionThrown);

	}

	@Test
	public void KingHaraldCanNotConsumeFromOnramp() throws Exception {
		TestKeystoreHelper.useTestKeystore(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, "onramp", "onramp");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = factory.createConnection();
		Destination queueR = (Destination) context.lookup("receiveQueue");
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		boolean exceptionThrown = false;

		MessageConsumer messageConsumer = null;
		try {
			messageConsumer = session.createConsumer(queueR);
		} catch (JMSSecurityException e) {
			exceptionThrown = true;
		}

		Assert.assertEquals(true, exceptionThrown);
	}


	@Test
	public void KingHaraldCanNotSendToNwEx() throws Exception {

		TestKeystoreHelper.useTestKeystore(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, NO_OUT, "nwEx");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = factory.createConnection();
		Destination queueR = (Destination) context.lookup("receiveQueue");
		Destination queueS = (Destination) context.lookup("sendQueue");
		connection.start();

		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		MessageConsumer messageConsumer = session.createConsumer(queueR);
		MessageProducer messageProducer = session.createProducer(queueS);

		JmsTextMessage message = (JmsTextMessage) session.createTextMessage("hello world");
		message.getFacade().setUserId("king_harald");
		message.setStringProperty("who", "Norwegian Public Roads Administration");
		message.setStringProperty("how", "Datex2");
		message.setStringProperty("what", "Conditions");
		message.setStringProperty("lat", "63.0");
		message.setStringProperty("lon", "10.0");
		message.setStringProperty("where1", "NO");

		boolean exceptionThrown = false;

		try {
			messageProducer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		} catch (JMSException e) {
			exceptionThrown = true;
		}

		Assert.assertEquals(true, exceptionThrown);

	}

	@Test
	public void userWithInvalidCertificateCannotConnect() throws Exception {
		TestKeystoreHelper.useTestKeystore(UNCERTIFIED_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, "test-out", "onramp");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = null;

		boolean throwsException = false;

		try {
			connection = factory.createConnection();
			Destination queueR = (Destination) context.lookup("receiveQueue");
			Destination queueS = (Destination) context.lookup("sendQueue");
			connection.start();


		} catch (Exception e) {
			System.out.println(e.getClass().getName());
			throwsException = true;
		}


		Assert.assertEquals(true, throwsException);

	}

	@Test
	public void userWithValidCertificateCanConnect() throws Exception{
		TestKeystoreHelper.useTestKeystore(JKS_KING_HARALD_P_12, TRUSTSTORE_JKS);
		Context context = setContext(URI, NO_OUT, "onramp");

		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);

		Connection connection = null;

		boolean throwsException = false;

		try {
			connection = factory.createConnection();
			Destination queueR = (Destination) context.lookup("receiveQueue");
			Destination queueS = (Destination) context.lookup("sendQueue");
			connection.start();


		} catch (Exception e) {
			System.out.println(e.getClass().getName());

			throwsException = true;
		}

		Assert.assertEquals(false, throwsException);

	}

	private static Context setContext(String uri, String receiveQueue, String sendQueue) throws Exception {
		Hashtable<Object, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory.myFactoryLookupTLS", uri);
		env.put("queue.receiveQueue", receiveQueue);
		env.put("queue.sendQueue", sendQueue);
		javax.naming.Context context = new javax.naming.InitialContext(env);

		return context;
	}



}
