package no.vegvesen.ixn;

import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

public class Source implements AutoCloseable {

	private final String url;
    private final String sendQueue;
    private final SSLContext sslContext;
    protected Connection connection;
    private Session session;
    private Destination queueS;
	private MessageProducer producer;
	private static Logger logger = LoggerFactory.getLogger(Source.class);

	public Source(String url, String sendQueue, SSLContext context) {
        this.url = url;
        this.sendQueue = sendQueue;
        this.sslContext = context;
    }

    public void start() throws NamingException, JMSException {
        IxnContext context = new IxnContext(url, sendQueue, null);
        createConnection(context);
        queueS = context.getSendQueue();
		connection.start();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		producer = session.createProducer(queueS);
    }

	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(sslContext);
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

	@Override
    public void close() {
		try {
			session.close();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }
		try {
			producer.close();
		} catch (JMSException e) {
			throw new RuntimeException(e);
		}
		connection = null;
		session = null;
		producer = null;
		queueS = null;
	}

	public JmsTextMessage createTextMessage(String msg) throws JMSException {
		return (JmsTextMessage) session.createTextMessage(msg);
	}

	public JmsTextMessage createTextMessage() throws JMSException {
		return (JmsTextMessage) session.createTextMessage();
	}

	public JmsBytesMessage createBytesMessage() throws JMSException {
		return (JmsBytesMessage) session.createBytesMessage();
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
}
