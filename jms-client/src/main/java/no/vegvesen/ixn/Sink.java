package no.vegvesen.ixn;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Sink implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Sink.class);


    protected final String url;
    private final String queueName;
    private final SSLContext sslContext;
	protected Connection connection;
	private MessageConsumer consumer;
	private final MessageListener listener;
	private ExceptionListener exceptionListener;

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

	public Sink(String url, String queueName, SSLContext sslContext, MessageListener listener, ExceptionListener exceptionListener) {
		this.url = url;
		this.queueName = queueName;
		this.sslContext = sslContext;
		this.listener = listener;
		this.exceptionListener = exceptionListener;
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

	public MessageConsumer createConsumer() throws NamingException, JMSException {
		IxnContext ixnContext = new IxnContext(this.url,null, this.queueName);
		connection = ixnContext.createConnection(sslContext);
		Destination destination = ixnContext.getReceiveQueue();
		connection.start();
		Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		MessageConsumer consumer = session.createConsumer(destination);
		logger.debug("Created message consumer for {}", this.queueName);
		return consumer;
	}

	public static class DefaultMessageListener implements MessageListener {

		Long messages = 0L;
		File directory;

		public DefaultMessageListener(){

		}

		public DefaultMessageListener(String directoryName){
			this.directory = new File(directoryName);
			if(!directory.exists()){
				directory.mkdir();
			}
		}

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

				Map<String, Object> metadataContent = new HashMap<>();

				while (propertyNames.hasMoreElements()) {
					String propertyName = propertyNames.nextElement();
					Object value = message.getObjectProperty(propertyName);
					if(directory != null){
						metadataContent.put(propertyName, value);
					}
					else {
						if (value instanceof String) {
							System.out.printf("%s:'%s'%n", propertyName, value);
						} else {
							System.out.printf("%s:%s:%s%n", propertyName, value.getClass().getSimpleName(), value);
						}
					}
				}

				String messageBody = null;
				messages += 1;
				File messageFile = new File(directory, "file-"+messages);
				File metadataFile = new File(directory, "file-"+messages+"-metadata.txt");

				switch (message){
					case JmsBytesMessage bytesMessage -> {
						byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
						bytesMessage.readBytes(messageBytes);
						if(directory != null) {
							try (FileOutputStream fos = new FileOutputStream(messageFile)) {
								fos.write(messageBytes);
							}
							try (PrintWriter printWriter = new PrintWriter(metadataFile)) {
								printWriter.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(metadataContent));
							}
						}
						else{
							System.out.println(" BYTES message");
							messageBody = Base64.getEncoder().encodeToString(messageBytes);
						}
					}
					case JmsTextMessage jmsTextMessage -> {
						messageBody = jmsTextMessage.getBody(String.class);
						if(directory != null){
							try(PrintWriter printWriter = new PrintWriter(messageFile)){
								printWriter.write(messageBody);
							}
							try(PrintWriter printWriter = new PrintWriter(metadataFile)){
								printWriter.write(new ObjectMapper().writeValueAsString(messageBody));
							}
						}
						else{
							System.out.println(" TEXT message");
						}
					}
					default -> System.err.println("Message type unknown: " + message.getClass().getName());
				}

				if(directory == null) {
					System.out.println("Body ------------");
					System.out.println(messageBody);
					System.out.println("/Body -----------");
				}
				else {
					System.out.println(String.format("Message written to %s, metadata written to %s", messageFile.getPath(), metadataFile.getPath()));
				}

				System.out.println("Delay " + delay + " ms \n");

			} catch (Exception e) {
				//e.printStackTrace();
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
			this.exceptionListener = exceptionListener;
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
