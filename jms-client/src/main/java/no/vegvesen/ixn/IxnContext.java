package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Hashtable;

public class IxnContext {

	private static final String JMS_JNDI_INITIAL_CONTEXT_FACTORY = "myInitialContextFactoryLookup";
	private static final String JMS_JNDI_RECEIVE_QUEUE_PROPERTY = "receiveQueue";
	private static final String JMS_JNDI_SEND_QUEUE_PROPERTY = "sendQueue";
	private final Context context;

	public IxnContext(Object URI, String sendQueue, String receiveQueue) throws NamingException {
		Hashtable<Object, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory." + JMS_JNDI_INITIAL_CONTEXT_FACTORY, URI);
		if (receiveQueue != null) {
			env.put("queue." + JMS_JNDI_RECEIVE_QUEUE_PROPERTY, receiveQueue);
		}
		if (sendQueue != null) {
			env.put("queue." + JMS_JNDI_SEND_QUEUE_PROPERTY, sendQueue);
		}
		this.context = new javax.naming.InitialContext(env);
	}

	/**
	 * uses basic authentication
	 */
	public Connection createConnection(String username, String password) throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		return factory.createConnection(username, password);
	}

	/**
	 * uses a preconfigured ssl context
	 */
	public Connection createConnection(SSLContext sslContext) throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		factory.setSslContext(sslContext);
		return factory.createConnection();
	}

	/**
	 * uses default PKI or settings provided by system properties
	 */
	public Connection createConnection() throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		return factory.createConnection();
	}

	public  Destination getReceiveQueue() throws NamingException {
		return (Destination) context.lookup(JMS_JNDI_RECEIVE_QUEUE_PROPERTY);
	}

	public Destination getSendQueue() throws NamingException {
		return (Destination) context.lookup(JMS_JNDI_SEND_QUEUE_PROPERTY);
	}
}
