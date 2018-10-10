package no.vegvesen.ixn;

import javax.jms.JMSException;
import javax.naming.NamingException;

public interface MessagingClient {
	void send(String queueName, String body) throws JMSException, NamingException;

	String receive(String queueName) throws JMSException, NamingException;
}
