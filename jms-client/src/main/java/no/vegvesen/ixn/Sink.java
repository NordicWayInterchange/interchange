package no.vegvesen.ixn;

import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.Session;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Base64;
import java.util.Enumeration;

public class Sink implements AutoCloseable {

	private static Logger logger = LoggerFactory.getLogger(Sink.class);


    protected final String url;
    private final String queueName;
    private final SSLContext sslContext;
    protected Connection connection;
	private MessageConsumer consumer;
	private MessageListener listener;

    public Sink(String url, String queueName, SSLContext sslContext) {
        this.url = url;
        this.queueName = queueName;
        this.sslContext = sslContext;
		this.listener = new DefaultMessageListener();
    }

	public Sink(String url, String queueName, SSLContext sslContext, MessageListener listener) {
		this.url = url;
		this.queueName = queueName;
		this.sslContext = sslContext;
		this.listener = listener;
	}

	public void startWithMessageListener(MessageListener newListener) throws JMSException, NamingException {
    	if (this.consumer != null) {
			try {
				this.consumer.close();
				logger.debug("Closed message consumer before creating new consumer");
			} catch (JMSException ignore) {
			}
		}
		this.consumer = createConsumer();
		this.consumer.setMessageListener(newListener);
		logger.debug("Consuming messages from {} with listener {}", this.queueName, newListener);
	}


    public void start() throws JMSException, NamingException {
		this.consumer = createConsumer();
		consumer.setMessageListener(listener);
		logger.debug("Consuming messages from {} with listener {}", this.queueName, this);
    }

	public MessageConsumer createConsumer() throws NamingException, JMSException {
		IxnContext ixnContext = new IxnContext(this.url,null, this.queueName);
		createConnection(ixnContext);
		Destination destination = ixnContext.getReceiveQueue();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(destination);
		logger.debug("Created message consumer for {}", this.queueName);
		return consumer;
	}

	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(sslContext);
	}

	public static class DefaultMessageListener implements MessageListener {

		@Override
		public void onMessage(Message message) {
			try {
				message.acknowledge();
				long delay = -1;
				try {
					long  timestamp = message.getLongProperty(MessageProperty.TIMESTAMP.getName());
					delay = System.currentTimeMillis() - timestamp;
				} catch (Exception e) {
					System.err.printf("Could not get message property '%s' to calculate delay;\n", MessageProperty.TIMESTAMP.getName());
				}
				System.out.println("** Message received **");
				Enumeration<String> propertyNames =  message.getPropertyNames();

				while (propertyNames.hasMoreElements()) {
					String propertyName = propertyNames.nextElement();
					Object value = message.getObjectProperty(propertyName);
					if (value instanceof String) {
						System.out.println(String.format("%s:'%s'",propertyName,value));
					} else {
						System.out.println(String.format("%s:%s:%s", propertyName, value.getClass().getSimpleName(), value));
					}
				}

				String messageBody;
				if (message instanceof JmsBytesMessage){
					System.out.println(" BYTES message");
					JmsBytesMessage bytesMessage = (JmsBytesMessage) message;
					byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
					bytesMessage.readBytes(messageBytes);
					messageBody = Base64.getEncoder().encodeToString(messageBytes);
				}
				else if (message instanceof JmsTextMessage) {
					System.out.println(" TEXT message");
					messageBody = message.getBody(String.class);
				}
				else {
					System.err.println("Message type unknown: " + message.getClass().getName());
					messageBody = null;
				}
				System.out.println("Body ------------");
				System.out.println(messageBody);
				System.out.println("/Body -----------");
				System.out.println("Delay " + delay + " ms \n");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void close() throws Exception {
		if (connection != null)  {
			connection.close();
		}
	}

	public void setExceptionListener(ExceptionListener exceptionListener) {
		try {
			this.connection.setExceptionListener(exceptionListener);
		} catch (JMSException e) {
			logger.error("Could not set exceptionListener {}", exceptionListener, e);
			throw new RuntimeException(e);
		}
	}

	//TODO this should go!
	public MessageListener getListener() {
		return listener;
	}
}
