package no.vegvesen.ixn;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

public class BasicAuthSSLSource extends BasicAuthSource {

	public BasicAuthSSLSource(String url, String sendQueue, String username, String password, SSLContext sslContext) {
		super(url, sendQueue, username, password, sslContext);
	}

	@Override
	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(username, password, sslContext);
	}

}
