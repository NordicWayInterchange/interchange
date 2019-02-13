package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public class IxnBaseIT {

	private static Hashtable<Object, Object> envWithUri(String URI) {
		Hashtable<Object, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory.myFactoryLookupTLS", URI);
		return env;
	}

	private static Context setContext(String URI) throws Exception {
		Hashtable<Object, Object> env = envWithUri(URI);
		return new javax.naming.InitialContext(env);
	}

	static Context setContext(String URI, String receiveQueue, String sendQueue) throws Exception {
		Hashtable<Object, Object> env = envWithUri(URI);
		env.put("queue.receiveQueue", receiveQueue);
		env.put("queue.sendQueue", sendQueue);
		return new javax.naming.InitialContext(env);
	}

	Connection createConnection(Context context, String keystore, String truststore) throws NamingException, TestKeystoreHelper.InvalidSSLConfig, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);
		factory.setSslContext(TestKeystoreHelper.sslContext(keystore, truststore));
		return factory.createConnection();
	}

	protected Session getSession(String uri, String user, String password) throws Exception {
		Context context = setContext(uri);
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);
		Connection connection = factory.createConnection(user, password);
		connection.start();
		return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}


}
