package no.vegvesen.ixn;

/*-
 * #%L
 * jms-client
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.message.JmsTextMessage;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Source implements AutoCloseable {

	/**
     * A message source. Sends a single message, and exits.
     * Note that the main file does not make use of any Spring Boot stuff.
     * However, an instance of the class could easily be used from Spring Boot, as all
     * dependent settings are handled in the main method, and passed as parameters to the instance.
     * @param args name-of-properties-file (optional)
     */
    public static void main(String[] args) throws NamingException, JMSException, IOException {
		Properties props = getProperties(args, "/source.properties");

		String url = props.getProperty("source.url");
        String sendQueue = props.getProperty("source.sendQueue");
        String keystorePath = props.getProperty("source.keyStorepath") ;
        String keystorePassword = props.getProperty("source.keyStorepass");
        String keyPassword = props.getProperty("source.keypass");
        KeystoreDetails keystoreDetails = new KeystoreDetails(keystorePath,
                keystorePassword,
                KeystoreType.PKCS12, keyPassword);
        String trustStorePath = props.getProperty("source.trustStorepath");
        String trustStorePassword = props.getProperty("source.trustStorepass");
        KeystoreDetails trustStoreDetails = new KeystoreDetails(trustStorePath,
                trustStorePassword,KeystoreType.JKS);

        SSLContext sslContext = SSLContextFactory.sslContextFromKeyAndTrustStores(keystoreDetails, trustStoreDetails);

        try( Source s = new Source(url,sendQueue,sslContext)) {
            s.start();
            s.send("Yo!", "NO", "someukquadtile");
        }
    }

	static Properties getProperties(String[] args, String propertiesFile) throws IOException {
		InputStream in;
		if (args.length == 1) {
			in = new FileInputStream(args[0]);
		} else {
			in = Source.class.getResourceAsStream(propertiesFile);
		}
		Properties props = new Properties();
		props.load(in);
		return props;
	}

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
