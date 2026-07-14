package no.vegvesen.ixn;

import org.apache.qpid.jms.message.JmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

public class Source implements AutoCloseable {

	private final String url;
    private final String sendQueue;
    private final SSLContext sslContext;
    protected Connection connection;
    private Session session;
	private MessageProducer producer;
	private static Logger logger = LoggerFactory.getLogger(Source.class);

	public Source(String url, String sendQueue, SSLContext context) {
        this.url = url;
        this.sendQueue = sendQueue;
        this.sslContext = context;
    }

    public void start() throws NamingException, JMSException {
		SourceConnectionCreator connectionCreator = new SourceConnectionCreator(url, sslContext);
		connection = connectionCreator.createConnection();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination destination = session.createQueue(this.sendQueue);
		producer = session.createProducer(destination);
    }

	public MessageBuilder createMessageBuilder() {
		return new MessageBuilder(session);
	}


	public void send(JmsMessage message, long timeToLive) throws JMSException {
		producer.send(message,DeliveryMode.PERSISTENT,Message.DEFAULT_PRIORITY,timeToLive);
	}

	public void send(JmsMessage message) throws JMSException {
		producer.send(message,DeliveryMode.PERSISTENT,Message.DEFAULT_PRIORITY,Message.DEFAULT_TIME_TO_LIVE);
	}

	public void sendNonPersistentMessage(JmsMessage message) throws JMSException {
		logger.info("Message: {}", message);
		producer.send(message,  DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
	}

	public void sendNonPersistentMessage(JmsMessage message, long timeToLive) throws JMSException {
		logger.info("Message: {}", message);
		producer.send(message,  DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
	}

	public JmsMessage createTextMessage(String messageBody) throws JMSException {
		return (JmsMessage) session.createTextMessage(messageBody);
	}

	@Override
    public void close() {
		if (session != null) {
			try {
				session.close();
			} catch (JMSException e) {
				logger.error("Error closing session", e);
			} finally {
				session = null;
			}
		}
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                logger.error("Error closing connection", e);
            } finally {
				connection = null;
			}
        }
		if (producer != null) {
			try {
				producer.close();
			} catch (JMSException e) {
				logger.error("Error closing producer", e);
			} finally {
				producer = null;
			}
		}
	}

	public void setExceptionListener(ExceptionListener exceptionListener) throws JMSException {
		this.connection.setExceptionListener(exceptionListener);
	}

	public boolean isConnected() {
		return connection != null && this.producer != null;
	}

	public MessageProducer getProducer() {
		return producer;
	}

	public Session getSession() {
		return session;
	}
}
