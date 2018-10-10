package no.vegvesen.ixn.qpid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QpidMessagingClient implements no.vegvesen.ixn.MessagingClient {
	private final ConnectionFactory factory;
	private final Context context;
	private final String user;
	private final String password;

	private static Logger logger = LoggerFactory.getLogger(QpidMessagingClient.class);

	@SuppressWarnings("WeakerAccess")
	public QpidMessagingClient() throws NamingException {
		context = new InitialContext();
		factory = (ConnectionFactory) context.lookup("myFactoryLookup");
		user = System.getProperty("USER");
		password = System.getProperty("PASSWORD");
		logger.debug("started client");
	}

	@Override
	public void send(String queueName, String body) throws JMSException, NamingException {
		logger.debug("sending to queue %s", queueName);
		Destination queue = (Destination) context.lookup(queueName);
		Connection connection = createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageProducer messageProducer = session.createProducer(queue);

		TextMessage message = session.createTextMessage(body);
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		logger.debug("sent to queue %s", queueName);
		connection.close();
		logger.debug("client connection closed");
	}

	private Connection createConnection() throws JMSException {
		Connection connection = factory.createConnection(user, password);
		connection.setExceptionListener(new MyExceptionListener());
		connection.start();
		return connection;
	}

	@Override
	public String receive(String queueName) throws JMSException, NamingException {
		Destination queue = (Destination) context.lookup(queueName);
		Connection connection = createConnection();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer messageConsumer = session.createConsumer(queue);

		TextMessage receivedMessage = (TextMessage) messageConsumer.receive(2000L);

		connection.close();
		return receivedMessage.getText();
	}

	private class MyExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException e) {
			System.out.println("Klarte ikke å sende eller motta melding " + e);
		}
	}
}
