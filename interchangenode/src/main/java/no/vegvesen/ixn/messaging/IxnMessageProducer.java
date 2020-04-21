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

@Component
public class IxnMessageProducer {

	private static Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);
	private MessageProducer nwExProducer;
	private MessageProducer dlQueueProducer;

	@Autowired
	public IxnMessageProducer(@Value("${amqphub.amqp10jms.remote-url}") String amqpUrl,
							  @Value("${amqphub.amqp10jms.username}") String username,
							  @Value("${amqphub.amqp10jms.password}") String password
	) throws JMSException, NamingException {
		nwExProducer = createProducer("nwEx", amqpUrl, username, password);
		dlQueueProducer = createProducer("dlqueue", amqpUrl, username, password);
	}

	private MessageProducer createProducer(String destination, String amqpUrl, String username, String password) throws NamingException, JMSException {
		IxnContext writeContext = new IxnContext(amqpUrl, destination, null);
		Destination writeDestination = writeContext.getSendQueue();
		Connection writeConnection = writeContext.createConnection(username, password);
		Session writeSession = writeConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		return writeSession.createProducer(writeDestination);
	}

	public void sendMessage(final Message textMessage) throws JMSException {
		MessageForwardUtil.send(nwExProducer, textMessage);
	}

	public void toDlQueue(final Message textMessage) throws JMSException {
		MessageForwardUtil.send(dlQueueProducer, textMessage);
	}

}

