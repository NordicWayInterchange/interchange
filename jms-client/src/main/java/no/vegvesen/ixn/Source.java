package no.vegvesen.ixn;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmDataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;

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

	public JmsMessage createTextMessage(String messageText, String originatingCountry, String messageQuadTreeTiles) throws JMSException {
		if (messageQuadTreeTiles != null && !messageQuadTreeTiles.startsWith(",")) {
			throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}

		JmsMessage message = new MessageBuilder().
				textMessage(messageText)
				.userId("localhost")
				.publisherId("NO-12345")
				.messageType(Datex2DataTypeApi.DATEX_2)
				.publicationType("Obstruction")
				.publicationSubType("WinterDrivingManagement")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry(originatingCountry)
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build();
		return message;
	}

	public JmsMessage getJmsTextMessage(String messageText, String originatingCountry) throws JMSException {
		JmsMessage message = new MessageBuilder()
				.textMessage(messageText)
				.userId("localhost")
				.messageType(Datex2DataTypeApi.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry(originatingCountry)
				.timestamp(System.currentTimeMillis())
				.build();
		return message;
	}

	public JmsMessage createDatex2TextMessage(String messageText, String originatingCountry, String messageQuadTreeTiles) throws JMSException {
		if (messageQuadTreeTiles != null && !messageQuadTreeTiles.startsWith(",")) {
			throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}
		JmsMessage message = new MessageBuilder()
				.textMessage(messageText)
				.userId("localhost")
				.messageType(Datex2DataTypeApi.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry(originatingCountry)
				.quadTreeTiles(messageQuadTreeTiles)
				.timestamp(System.currentTimeMillis())
				.build();
		return message;
	}

	public JmsMessage createMonotchMessage() throws JMSException {
		String messageText = "{}";
		byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
		JmsMessage message = new MessageBuilder()
				.bytesMessage(bytemessage)
				.userId("anna")
				.messageType(DenmDataTypeApi.DENM)
				.publisherId("NO-123")
				.originatingCountry("NO")
				.protocolVersion("1.0")
				.quadTreeTiles(",12003")
				.causeCode("6")
				.subCauseCode("76")
				.build();

		return message;
	}

	public JmsMessage getTextMessage(String messageText, String originatingCountry) throws JMSException {
		JmsMessage message = new MessageBuilder()
				.textMessage(messageText)
				.userId("localhost")
				.messageType(Datex2DataTypeApi.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion( "DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry(originatingCountry)
				.timestamp(System.currentTimeMillis())
				.build();
		return message;
	}

	public JmsMessage createDenmByteMessage(String messageText, String originatingCountry, String messageQuadTreeTiles) throws JMSException {
		byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
		JmsMessage message = new MessageBuilder()
				.bytesMessage(bytemessage)
				.userId("localhost")
				.messageType("DENM")
				.publisherId("NO-12345")
				.protocolVersion("DENM:1.2.2")
				.originatingCountry(originatingCountry)
				.quadTreeTiles(messageQuadTreeTiles)
				.serviceType("some-denm-service-type")
				.causeCode( "3")
				.subCauseCode("6")
				.timestamp(System.currentTimeMillis())
				.build();
		return message;
	}

	public JmsMessage createIviBytesMessage(String messageText, String originatingCountry, String messageQuadTreeTiles) throws JMSException {
		byte[] bytemessage = messageText.getBytes(StandardCharsets.UTF_8);
		JmsMessage message = new MessageBuilder()
				.bytesMessage(bytemessage)
				.userId("localhost")
				.messageType("IVI")
				.publisherId("NO-12345")
				.protocolVersion("IVI:1.2")
				.originatingCountry(originatingCountry)
				.quadTreeTiles(messageQuadTreeTiles)
				.serviceType("some-ivi-service-type")
				.timestamp(System.currentTimeMillis())
				.stringProperty(MessageProperty.IVI_TYPE.getName(), "128")
				.stringProperty(MessageProperty.PICTOGRAM_CATEGORY_CODE.getName(), "557")
				.build();

		return message;
	}

	private class MessageBuilder {
		private JmsMessage message;

		public JmsMessage build() {
			return message;
		}

		public MessageBuilder textMessage(String text) throws JMSException {
			message = (JmsMessage) session.createTextMessage(text);
			return this;
		}

		public MessageBuilder bytesMessage(byte[] messageBody) throws JMSException {
			JmsBytesMessage bytesMessage = (JmsBytesMessage) session.createBytesMessage();
			bytesMessage.writeBytes(messageBody);
			message = bytesMessage;
			return this;
		}

		public MessageBuilder userId(String user) {
			message.getFacade().setUserId(user);
			return this;
		}

		public MessageBuilder messageType(String messageType) throws JMSException {
			message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), messageType);
			return this;
		}

		public MessageBuilder publisherId(String publisher) throws JMSException {
			message.setStringProperty(MessageProperty.PUBLISHER_ID.getName(), publisher);
			return this;
		}

		public MessageBuilder protocolVersion(String version) throws JMSException {
			message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), version);
			return this;
		}

		public MessageBuilder originatingCountry(String originatingCountry) throws JMSException {
			message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
			return this;
		}

		public MessageBuilder quadTreeTiles(String messageQuadTreeTiles) throws JMSException {
			message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
			return this;
		}

		public MessageBuilder serviceType(String serviceType) throws JMSException {
			message.setStringProperty(MessageProperty.SERVICE_TYPE.getName(), serviceType);
			return this;
		}

		public MessageBuilder timestamp(long currentTimeMillis) throws JMSException {
			message.setLongProperty(MessageProperty.TIMESTAMP.getName(), currentTimeMillis);
			return this;
		}

		public MessageBuilder stringProperty(String name, String value) throws JMSException {
			message.setStringProperty(name,value);
			return this;
		}

		public MessageBuilder causeCode(String causeCode) throws JMSException {
			message.setStringProperty(MessageProperty.CAUSE_CODE.getName(), causeCode);
			return this;
		}

		public MessageBuilder subCauseCode(String subCauseCode) throws JMSException {
			message.setStringProperty(MessageProperty.SUB_CAUSE_CODE.getName(), subCauseCode);
			return this;
		}

		public MessageBuilder publicationType(String publicationType) throws JMSException {
			message.setStringProperty(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
			return this;
		}

		public MessageBuilder latitude(double latitude) throws JMSException {
			message.setDoubleProperty(MessageProperty.LATITUDE.getName(), latitude);
			return this;
		}

		public MessageBuilder longitude(double longitude) throws JMSException {
			message.setDoubleProperty(MessageProperty.LONGITUDE.getName(), longitude);
			return this;
		}

		public MessageBuilder publicationSubType(String subType) throws JMSException {
			message.setStringProperty(MessageProperty.PUBLICATION_SUB_TYPE.getName(), subType);
			return this;
		}
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
