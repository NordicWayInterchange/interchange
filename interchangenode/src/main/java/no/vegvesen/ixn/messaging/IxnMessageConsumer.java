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
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class IxnMessageConsumer implements MessageListener, ExceptionListener {

	private static Logger logger = LoggerFactory.getLogger(IxnMessageConsumer.class);
	private final Sink onrampConsumer;
	private final Source nwExProducer;
	private final Source dlQueueProducer;
	private AtomicBoolean running;
	private final MessageValidator messageValidator;

	public IxnMessageConsumer(Sink onrampConsumer,
							  Source nwExProducer,
							  Source dlQueueProducer,
							  MessageValidator messageValidator) {
		this.onrampConsumer = onrampConsumer;
		this.nwExProducer = nwExProducer;
		this.dlQueueProducer = dlQueueProducer;
		this.messageValidator = messageValidator;
		this.running = new AtomicBoolean(true);
	}

	public void teardown() {
		logger.info("Closing consumer");
		try {
			onrampConsumer.close();
		} catch (Exception ignore) {
		} finally {
			running.set(false);
		}

		logger.info("Closing nwExProducer");
		try {
			nwExProducer.close();
		} catch (Exception ignore) {
		}

		logger.info("Closing dlqueueProducer");
		try {
			dlQueueProducer.close();
		} catch (Exception ignore) {
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
				MessageForwardUtil.send(nwExProducer.getProducer(), message);
			} else {
				logger.warn("Sending bad message to dead letter queue. Invalid message.");
				MessageForwardUtil.send(dlQueueProducer.getProducer(), message);
			}
		} catch (Exception e) {
			logger.error("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
			try {
				MessageForwardUtil.send(dlQueueProducer.getProducer(), message);
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

