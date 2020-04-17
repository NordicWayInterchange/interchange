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
import org.springframework.jms.support.QosSettings;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

@Component
public class IxnMessageProducer {

	final static long DEFAULT_TTL = 86_400_000L;
	final static long MAX_TTL = 6_911_200_000L;

	private final JmsTemplate jmsTemplate;

	private static Logger logger = LoggerFactory.getLogger(IxnMessageProducer.class);

	@Autowired
	public IxnMessageProducer(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void sendMessage(String destination, final Message textMessage) {
		long timeToLive = checkTimeToLive(textMessage);
		this.jmsTemplate.setQosSettings(new QosSettings(this.jmsTemplate.getDeliveryMode(), this.jmsTemplate.getPriority(), timeToLive));
		this.jmsTemplate.send(destination, session -> textMessage);
	}

	public static long checkTimeToLive(Message textMessage) {
		long expiration;
		try {
			expiration = textMessage.getJMSExpiration();
		} catch (JMSException e) {
			return DEFAULT_TTL;
		}
		long currentTime = System.currentTimeMillis();
		if (expiration <= 0) {
			// expiration is absent or illegal - setting to default ttl (1 day)
			return DEFAULT_TTL;
		} else if (expiration > (MAX_TTL + currentTime)) {
			// expiration is too high, setting to maximum ttl (8 days)
			return MAX_TTL;
		} else {
			// expiration is in the valid range (more than 0, less than 8 days)
			return expiration - currentTime;
		}
	}


}

