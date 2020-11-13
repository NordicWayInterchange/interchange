package no.vegvesen.ixn;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

public class BasicAuthSource extends Source {
	protected  final String username;
	protected final String password;

	public BasicAuthSource(String url, String sendQueue, String username, String password) {
		super(url, sendQueue, null);
		this.username = username;
		this.password = password;
	}

	public BasicAuthSource(String url, String sendQueue, String username, String password, SSLContext sslContext) {
		super(url, sendQueue, sslContext);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(username, password);
	}

}
