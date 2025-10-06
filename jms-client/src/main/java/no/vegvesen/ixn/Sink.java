package no.vegvesen.ixn;

import jakarta.jms.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

public class Sink implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Sink.class);

    protected final String url;

    private final String queueName;

    private final SSLContext sslContext;

	protected Connection connection;

	private MessageConsumer consumer;

	private final MessageListener listener;

	private ExceptionListener exceptionListener;

	private String dynamicFilter;

    public Sink(String url, String queueName, SSLContext sslContext) {
        this.url = url;
        this.queueName = queueName;
        this.sslContext = sslContext;
		this.listener = new WriteToScreenMessageListener();
    }

	public Sink(String url, String queueName, SSLContext sslContext, MessageListener listener) {
		this.url = url;
		this.queueName = queueName;
		this.sslContext = sslContext;
		this.listener = listener;
	}

	public Sink(String url, String queueName, SSLContext sslContext, MessageListener listener, ExceptionListener exceptionListener) {
		this.url = url;
		this.queueName = queueName;
		this.sslContext = sslContext;
		this.listener = listener;
		this.exceptionListener = exceptionListener;
	}

	public Sink(String url, String queueName, SSLContext sslContext, MessageListener listener, ExceptionListener exceptionListener, String dynamicFilter) {
		this.url = url;
		this.queueName = queueName;
		this.sslContext = sslContext;
		this.listener = listener;
		this.exceptionListener = exceptionListener;
		this.dynamicFilter = dynamicFilter;
	}

	public void startWithMessageListener(MessageListener newListener, Integer prefetch) throws JMSException, NamingException {
		if (this.consumer != null) {
			try {
				this.consumer.close();
				logger.debug("Closed message consumer before creating new consumer");
			} catch (JMSException ignore) {
			}
		}
		this.consumer = createConsumerWithPrefetch(prefetch);
		this.consumer.setMessageListener(newListener);
		if (this.exceptionListener != null) {
			connection.setExceptionListener(this.exceptionListener);
		}
		logger.debug("Consuming messages from {} with listener {}", this.queueName, newListener);
	}

	public void start() throws JMSException, NamingException {
		this.consumer = createConsumer();
		consumer.setMessageListener(listener);
		if (exceptionListener != null) {
			connection.setExceptionListener(exceptionListener);
		}
		logger.debug("Consuming messages from {} with listener {}", this.queueName, this);
	}

	public MessageConsumer createConsumerWithPrefetch(Integer prefetch) throws NamingException, JMSException {
		IxnContext ixnContext = new IxnContext(this.url,null, this.queueName, prefetch);
		connection = ixnContext.createConnection(sslContext);
		Destination destination = ixnContext.getReceiveQueue();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = dynamicFilter == null ? session.createConsumer(destination) : session.createConsumer(destination, dynamicFilter);
		logger.debug("Created message consumer for {}", this.queueName);
		return consumer;
	}

	public MessageConsumer createConsumer() throws NamingException, JMSException {
		IxnContext ixnContext = new IxnContext(this.url,null, this.queueName);
		connection = ixnContext.createConnection(sslContext);
		Destination destination = ixnContext.getReceiveQueue();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = dynamicFilter == null ? session.createConsumer(destination) : session.createConsumer(destination, dynamicFilter);
		logger.debug("Created message consumer for {}", this.queueName);
		return consumer;
	}

    @Override
	public void close() throws Exception {
		if (connection != null)  {
			connection.close();
		}
	}

	public void setExceptionListener(ExceptionListener exceptionListener) {
		try {
			this.exceptionListener = exceptionListener;
			this.connection.setExceptionListener(exceptionListener);
		} catch (JMSException e) {
			logger.error("Could not set exceptionListener {}", exceptionListener, e);
			throw new RuntimeException(e);
		}
	}
}
