package no.vegvesen.ixn.qpid;

import no.vegvesen.ixn.model.DispatchMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class QpidMessagingClient implements no.vegvesen.ixn.MessagingClient {
	private static final String JNDI_NAME_OUT = "out";
	private static final String JNDI_NAME_IN = "in";
	private final Connection connection;
	private final Session session;
	private MessageProducer messageProducer;
	private MessageConsumer messageConsumer;

	private static Logger logger = LoggerFactory.getLogger(QpidMessagingClient.class);

	@SuppressWarnings("WeakerAccess")
	public QpidMessagingClient() throws NamingException, JMSException {
		Context context = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) context.lookup("myFactoryLookup");
		String user = System.getProperty("USER");
		String password = System.getProperty("PASSWORD");
		connection = factory.createConnection(user, password);
		connection.setExceptionListener(new MyExceptionListener());
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		logger.debug("started client");
		Destination outQueue = (Destination) context.lookup(JNDI_NAME_OUT);
		messageProducer = session.createProducer(outQueue);
		Destination inQueue = (Destination) context.lookup(JNDI_NAME_IN);
		messageConsumer = session.createConsumer(inQueue);
	}

	private void send(String body) throws JMSException{
		logger.debug("sending to queue " + messageProducer.getDestination());
		TextMessage message = session.createTextMessage(body);
		messageProducer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
		logger.debug("sent to queue " + messageProducer.getDestination());
	}

	@Override
	public TextMessage receive() throws JMSException{
		logger.debug("receiving from " + messageConsumer);
		return (TextMessage) messageConsumer.receive();
	}

	@Override
	public void close() throws JMSException {
		connection.close();
		logger.debug("client connection closed");
	}

	@Override
	public void send(DispatchMessage dispatchMessage) throws JMSException {
		send(dispatchMessage.getBody());
	}

	private class MyExceptionListener implements ExceptionListener {
		@Override
		public void onException(JMSException e) {
			System.out.println("Klarte ikke å sende eller motta melding " + e);
		}
	}
}
