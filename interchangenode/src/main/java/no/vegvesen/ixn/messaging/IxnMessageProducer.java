/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.MessageForwardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.*;
import javax.naming.NamingException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class IxnMessageProducer implements ExceptionListener {
	public static final String DLQUEUE = "dlqueue";
	public static final String NWEXCHANGE = "nwEx";

	private static Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);
	private final String amqpUrl;
	private final String username;
	private final String password;
	private MessageProducer nwExProducer;
	private MessageProducer dlQueueProducer;
	private AtomicBoolean running = new AtomicBoolean(false);

	@Autowired
	public IxnMessageProducer(@Value("${amqphub.amqp10jms.remote-url}") String amqpUrl,
							  @Value("${amqphub.amqp10jms.username}") String username,
							  @Value("${amqphub.amqp10jms.password}") String password) {
		this.amqpUrl = amqpUrl;
		this.username = username;
		this.password = password;
		connect();
	}

	private MessageProducer createProducer(String destination, String amqpUrl, String username, String password) throws NamingException, JMSException {
		logger.debug("Connecting to produce message to {}", destination);
		IxnContext writeContext = new IxnContext(amqpUrl, destination, null);
		Destination writeDestination = writeContext.getSendQueue();
		Connection writeConnection = writeContext.createConnection(username, password);
		writeConnection.setExceptionListener(this);
		logger.debug("Connection established");
		Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		writeConnection.setExceptionListener(this);
		MessageProducer producer = writeSession.createProducer(writeDestination);
		logger.info("Created producer for {}", destination);
		return producer;
	}

	public void sendMessage(final Message textMessage) throws JMSException {
		send(textMessage, nwExProducer, NWEXCHANGE);
	}

	public void toDlQueue(final Message textMessage) throws JMSException {
		send(textMessage, dlQueueProducer, DLQUEUE);
	}

	private void send(Message textMessage, MessageProducer messageProducer, String destinationName) throws JMSException {
		try {
			if (!running.get()) {
				logger.warn("Reconnecting");
				connect();
			}
			MessageForwardUtil.send(messageProducer, textMessage);
		} catch (JMSException e) {
			logger.error("Exception occurred when sending message {}, reconnecting to {}", textMessage.getJMSMessageID(), destinationName, e);
			onException(e);
			throw e;
		}
	}

	@Override
	public void onException(JMSException exception) {
		logger.error("Exception listener triggered by exception", exception);
		teardown();
		this.running.set(false);
	}

	public void teardown()  {
		try {
			nwExProducer.close();
			dlQueueProducer.close();
		} catch (JMSException ignore) {
		} finally {
			running.set(false);
		}
	}

	private void connect() {
		logger.info("Connecting to qpid");
		try {
			this.nwExProducer = createProducer(NWEXCHANGE, amqpUrl, username, password);
			this.dlQueueProducer = createProducer(DLQUEUE, amqpUrl, username, password);
			this.running.set(true);
			logger.info("Connected successfully");
		} catch (NamingException | JMSException e) {
			logger.error("Could not connect to qpid ", e);
			this.running.set(false);
		}
	}
}

