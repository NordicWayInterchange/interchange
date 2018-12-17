package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.Hashtable;

public class IxnBaseIT {
	protected static Context setContext(Object URI, String receiveQueue, String sendQueue) throws Exception {
		Hashtable<Object, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory.myFactoryLookupTLS", URI);
		env.put("queue.receiveQueue", receiveQueue);
		env.put("queue.sendQueue", sendQueue);
		return new javax.naming.InitialContext(env);
	}

	protected Connection createConnection(Context context, String keystore, String truststore) throws NamingException, TestKeystoreHelper.InvalidSSLConfig, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup("myFactoryLookupTLS");
		factory.setPopulateJMSXUserID(true);
		factory.setSslContext(TestKeystoreHelper.sslContext(keystore, truststore));
		return factory.createConnection();
	}
}
