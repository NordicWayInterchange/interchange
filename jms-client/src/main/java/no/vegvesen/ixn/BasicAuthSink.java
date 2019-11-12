package no.vegvesen.ixn;

import javax.jms.*;
import javax.naming.NamingException;

public class BasicAuthSink extends Sink  {
	private final String username;
	private final String password;

	public BasicAuthSink(String url, String queueName, String username, String password) {
		super(url, queueName, null);
		this.username = username;
		this.password = password;
	}

	@Override
	protected void createConnection(IxnContext ixnContext) throws NamingException, JMSException {
		connection = ixnContext.createConnection(username, password);
	}
}
