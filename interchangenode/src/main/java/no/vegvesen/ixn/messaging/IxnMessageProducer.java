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

import no.vegvesen.ixn.model.IxnMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static no.vegvesen.ixn.MessageProperties.*;

@Component
public class IxnMessageProducer {

	private final IxnJmsTemplate jmsTemplate;

	private static Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);

	@Autowired
	public IxnMessageProducer(IxnJmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}



	// Duplicates message for each country and situation record type.
	public void sendMessage(String destination, final IxnMessage message) {

		for (String country : message.getCountries()) {
			for (String situationRecordType : message.getWhat()) {

				logger.debug("*** Sending message ***");


				MessageCreator messageCreator = session -> {

					TextMessage outgoingMessage = session.createTextMessage();
					outgoingMessage.setDoubleProperty(LAT, message.getLat());
					outgoingMessage.setDoubleProperty(LON, message.getLon());
					outgoingMessage.setStringProperty(WHO, message.getWho());
					outgoingMessage.setStringProperty(USERID, message.getUserID());
					outgoingMessage.setStringProperty(WHERE, country);
					outgoingMessage.setStringProperty(WHAT, situationRecordType);
					if (message.getHow() != null) {
						outgoingMessage.setStringProperty(HOW, message.getHow());
					}
					if (message.getWhen() != null) {
						outgoingMessage.setStringProperty(WHEN, message.getWhen());
					}
					long expiration = message.getExpiration();
					logger.debug("message expire time " + expiration);
					outgoingMessage.setJMSExpiration(expiration);
					((JmsMessage) outgoingMessage).getFacade().setExpiration(expiration);
					((JmsMessage) outgoingMessage).getFacade().setExpiration(expiration);
					outgoingMessage.setText(message.getBody());
					logger.debug("sending lon: {} lat: {} who: {} userID: {} country:  {} what: {} body: {}",
							message.getLon(),
							message.getLat(),
							message.getWho(),
							message.getUserID(),
							country,
							situationRecordType,
							message.getBody());
					return outgoingMessage;
				};
				this.jmsTemplate.send(destination, messageCreator, ttl(message.getExpiration()));
			}
		}
	}

	public void sendMessage(String destination, final TextMessage textMessage) {
		this.jmsTemplate.send(destination, session -> textMessage, ttl(textMessage));
	}

	private long ttl(TextMessage textMessage) {
		try {
			return ttl(textMessage.getJMSExpiration());
		} catch (JMSException e) {
			return 0;
		}
	}

	private long ttl(long expiration) {
		if (expiration > 0) {
			return expiration - System.currentTimeMillis();
		}
		return 0;
	}

}

