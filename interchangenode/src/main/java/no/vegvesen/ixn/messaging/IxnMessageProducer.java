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

@Component
public class IxnMessageProducer {

    private static final Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);

	private final JmsTemplate jmsTemplate;

	@Autowired
	public IxnMessageProducer(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendMessage(final String destinationName, final TextMessage textMessage) {
		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", textMessage, destinationName);

			return textMessage;
		});
    }

    public void sendMessage(final String destinationName, final float lat, final float lon, final String what, final String message){
		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", message, destinationName);
			TextMessage textMessage = session.createTextMessage(message);

			textMessage.setFloatProperty("lat", lat);
			textMessage.setFloatProperty("lon", lon);
			textMessage.setStringProperty("what", what);

			return textMessage;
		});

	}

	public void sendBadMessage(final String destinationName, final float lat, final float lon,  String message){
		// Sends a bad message that will not pass validation.

		this.jmsTemplate.send(destinationName, session -> {
			logger.debug("Sending message {} to {}", message, destinationName);
			TextMessage textMessage = session.createTextMessage();
			textMessage.setFloatProperty("lat", lat);
			textMessage.setFloatProperty("lon", lon);
			return textMessage;
		});
	}
}

