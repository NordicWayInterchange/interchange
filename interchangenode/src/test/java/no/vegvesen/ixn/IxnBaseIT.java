package no.vegvesen.ixn;

import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.Connection;
import javax.jms.Session;

class IxnBaseIT {

	Session getSession(String uri, String user, String password) throws Exception {
		JmsConnectionFactory factory = new JmsConnectionFactory(uri);
		factory.setPopulateJMSXUserID(true);
		Connection connection = factory.createConnection(user, password);
		connection.start();
		return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

	Session getTlsSession(String uri, String keystore, String truststore) throws Exception {
		JmsConnectionFactory factory = new JmsConnectionFactory(uri);
		factory.setPopulateJMSXUserID(true);
		factory.setSslContext(TestKeystoreHelper.sslContext(keystore, truststore));
		Connection connection = factory.createConnection();
		connection.start();
		return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}

}
