package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
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

	public void send(String messageText) throws JMSException {
		this.send(messageText, "SE", null);
	}


    public void send(String messageText, String originatingCountry, String messageQuadTreeTiles) throws JMSException {
    	if (messageQuadTreeTiles != null && !messageQuadTreeTiles.startsWith(",")) {
    		throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}

        JmsTextMessage message = createTextMessage(messageText);
        message.getFacade().setUserId("localhost");
        message.setStringProperty(MessageProperty.PUBLISHER_NAME.getName(), "Norwegian Public Roads Administration");
        message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
        message.setStringProperty(MessageProperty.PUBLICATION_TYPE.getName(), "Obstruction");
        message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), "DATEX2;2.3");
        message.setStringProperty(MessageProperty.LATITUDE.getName(), "60.352374");
        message.setStringProperty(MessageProperty.LONGITUDE.getName(), "13.334253");
        message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
        message.setLongProperty(MessageProperty.TIMESTAMP.getName(), System.currentTimeMillis());
        sendTextMessage(message, Message.DEFAULT_TIME_TO_LIVE);
    }

	public void sendTextMessage(JmsTextMessage message, long timeToLive) throws JMSException {
		producer.send(message,  DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, timeToLive);
	}

	public void sendNonPersistentMessage(JmsTextMessage message, long timeToLive) throws JMSException {
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

	public void send(String messageText, String originatingCountry, long timeToLive) throws JMSException {
		JmsTextMessage message = createTextMessage(messageText);
		message.getFacade().setUserId("localhost");
		message.setStringProperty(MessageProperty.PUBLISHER_NAME.getName(), "Norwegian Public Roads Administration");
		message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		message.setStringProperty(MessageProperty.PUBLICATION_TYPE.getName(), "Obstruction");
		message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), "DATEX2;2.3");
		message.setStringProperty(MessageProperty.LATITUDE.getName(), "60.352374");
		message.setStringProperty(MessageProperty.LONGITUDE.getName(), "13.334253");
		message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		message.setLongProperty(MessageProperty.TIMESTAMP.getName(), System.currentTimeMillis());
		sendTextMessage(message, timeToLive);
	}

	public MessageProducer createProducer() throws JMSException, NamingException {
    	this.start();
    	return this.session.createProducer(this.queueS);
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
