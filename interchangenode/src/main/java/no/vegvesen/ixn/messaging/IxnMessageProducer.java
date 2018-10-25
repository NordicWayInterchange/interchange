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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

import static no.vegvesen.ixn.MessageProperties.*;

@Component
public class IxnMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);
	private final JmsTemplate jmsTemplate;

	@Autowired
	public IxnMessageProducer(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	// Sending messages with multiple countries and situation records to 'test-out'
	public void sendMessage(final TextMessage textMessage, List<String> countries, List<String> situationRecordTypes) {

		String destinationName = "test-out";
		for(String country : countries){
			for(String situationRecordType : situationRecordTypes){

				this.jmsTemplate.send(destinationName, session -> {

					// Copies the body from the original message
					TextMessage outgoing = session.createTextMessage(textMessage.getText());
					logger.debug("Sending message {} to {}", outgoing, destinationName);

					// Sets country and situation record type
					outgoing.setStringProperty(WHERE, country);
					outgoing.setStringProperty(WHAT, situationRecordType);
					logger.info("Creating packet with country " + country + " and situation record " + situationRecordType);

					return outgoing;
				});
			}
		}
    }

    // Is used for sending invalid messages straigt to 'dlqueue'
	public void dropMessage(final TextMessage textMessage) {

		String destinationName = "dlqueue";
		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", textMessage, destinationName);

			return textMessage;
		});
    }

}

