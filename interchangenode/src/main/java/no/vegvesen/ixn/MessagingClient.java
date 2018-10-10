package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public interface MessagingClient {
	TextMessage receive(String queueName) throws JMSException, NamingException;
	void send(DispatchMessage dispatchMessage) throws NamingException, JMSException;
}
