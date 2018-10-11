package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;

public interface MessagingClient {
	TextMessage receive() throws JMSException, NamingException;
	void close() throws JMSException;
	void send(DispatchMessage dispatchMessage) throws NamingException, JMSException;
}
