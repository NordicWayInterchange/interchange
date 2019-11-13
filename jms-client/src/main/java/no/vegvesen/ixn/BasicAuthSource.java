package no.vegvesen.ixn;

import javax.jms.JMSException;
import javax.naming.NamingException;

public class BasicAuthSource extends Source {
	private final String username;
	private final String password;

	public BasicAuthSource(String url, String sendQueue, String username, String password) {
		super(url, sendQueue, null);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(username, password);
	}

}
