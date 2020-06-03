package no.vegvesen.ixn;

/*-
 * #%L
 * jms-client
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import org.apache.qpid.jms.JmsConnectionFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Hashtable;

public class IxnContext {

	private static final String JMS_JNDI_INITIAL_CONTEXT_FACTORY = "myInitialContextFactoryLookup";
	private static final String JMS_JNDI_RECEIVE_QUEUE_PROPERTY = "receiveQueue";
	private static final String JMS_JNDI_SEND_QUEUE_PROPERTY = "sendQueue";
	private final Context context;

	public IxnContext(Object URI, String sendQueue, String receiveQueue) throws NamingException {
		Hashtable<Object, Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.qpid.jms.jndi.JmsInitialContextFactory");
		env.put("connectionfactory." + JMS_JNDI_INITIAL_CONTEXT_FACTORY, URI);
		if (receiveQueue != null) {
			env.put("queue." + JMS_JNDI_RECEIVE_QUEUE_PROPERTY, receiveQueue);
		}
		if (sendQueue != null) {
			env.put("queue." + JMS_JNDI_SEND_QUEUE_PROPERTY, sendQueue);
		}
		this.context = new javax.naming.InitialContext(env);
	}

	/**
	 * uses basic authentication
	 */
	public Connection createConnection(String username, String password) throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		return factory.createConnection(username, password);
	}

	/**
	 * uses a preconfigured ssl context
	 */
	public Connection createConnection(SSLContext sslContext) throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		factory.setSslContext(sslContext);
		return factory.createConnection();
	}

	/**
	 * uses default PKI or settings provided by system properties
	 */
	public Connection createConnection() throws NamingException, JMSException {
		JmsConnectionFactory factory = (JmsConnectionFactory) context.lookup(JMS_JNDI_INITIAL_CONTEXT_FACTORY);
		factory.setPopulateJMSXUserID(true);
		return factory.createConnection();
	}

	public  Destination getReceiveQueue() throws NamingException {
		return (Destination) context.lookup(JMS_JNDI_RECEIVE_QUEUE_PROPERTY);
	}

	public Destination getSendQueue() throws NamingException {
		return (Destination) context.lookup(JMS_JNDI_SEND_QUEUE_PROPERTY);
	}
}
