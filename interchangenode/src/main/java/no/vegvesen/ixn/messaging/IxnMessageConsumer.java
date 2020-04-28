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

import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class IxnMessageConsumer implements MessageListener, ExceptionListener {

	private static Logger logger = LoggerFactory.getLogger(IxnMessageConsumer.class);
	private final MessageConsumer onrampConsumer;
	private final MessageProducer nwExProducer;
	private final MessageProducer dlQueueProducer;
	private AtomicBoolean running = new AtomicBoolean(false);
	private final MessageValidator messageValidator;

	public IxnMessageConsumer(MessageConsumer onrampConsumer,
							  MessageProducer nwExProducer,
							  MessageProducer dlQueueProducer,
							  MessageValidator messageValidator) {
		this.onrampConsumer = onrampConsumer;
		this.nwExProducer = nwExProducer;
		this.dlQueueProducer = dlQueueProducer;
		this.messageValidator = messageValidator;
	}

	public void teardown() {
		logger.info("Closing consumer");
		try {
			onrampConsumer.close();
		} catch (JMSException ignore) {
		} finally {
			running.set(false);
		}

		logger.info("Closing nwExProducer");
		try {
			nwExProducer.close();
		} catch (JMSException ignore) {
		}

		logger.info("Closing dlqueueProducer");
		try {
			dlQueueProducer.close();
		} catch (JMSException ignore) {
		}
	}


	@Override
	public void onException(JMSException e) {
		logger.error("Exception caught", e);
		this.teardown();
	}

	@Override
	public void onMessage(Message message) {
		try {
			MDCUtil.setLogVariables(message);
			if (messageValidator.isValid(message)) {
				MessageForwardUtil.send(nwExProducer, message);
			} else {
				logger.warn("Sending bad message to dead letter queue. Invalid message.");
				MessageForwardUtil.send(dlQueueProducer, message);
			}
		} catch (Exception e) {
			logger.error("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
			try {
				MessageForwardUtil.send(dlQueueProducer, message);
			} catch (JMSException ex) {
				logger.error("Can not send bad message to dead letter queue.", ex);
			}
		} finally {
			MDCUtil.removeLogVariables();
		}
	}

	public boolean isRunning() {
		return running.get();
	}
}

