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
import org.springframework.stereotype.Component;

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


	// Is used for sending messages with multiple countries and situation records to 'test-out'
	public void sendMessage(final String destinationName, final TextMessage textMessage, List<String> countries, List<String> situationRecordTypes) {

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
	public void sendMessage(final String destinationName, final TextMessage textMessage) {
		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", textMessage, destinationName);

			return textMessage;
		});
    }

	// Is used to send the initial message to 'onramp'
    public void sendMessage(final String destinationName, final float lat, final float lon, final String what, final String message){
		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", message, destinationName);
			TextMessage textMessage = session.createTextMessage(message);

			textMessage.setFloatProperty(LAT, lat);
			textMessage.setFloatProperty(LON, lon);
			textMessage.setStringProperty(WHAT, what);

			return textMessage;
		});
	}
}

