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

/*-
 * #%L
 * interchange-node
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

import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.exception.InterchangeException;
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
		if (!isRunning()) {
			teardown();
			throw new InterchangeException("Still listening for messages when not running, tearing down.");
		}
		try {
			MDCUtil.setLogVariables(message);
			if (messageValidator.isValid(message)) {
				MessageForwardUtil.send(nwExProducer.getProducer(), message);
			} else {
				logger.warn("Sending bad message to dead letter queue. Invalid message.");
				MessageForwardUtil.send(dlQueueProducer.getProducer(), message);
			}
		} catch (JMSException e) {
			teardown();
			throw new InterchangeException("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
		} finally {
			MDCUtil.removeLogVariables();
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean isRunning() {
		return running.get();
	}
}

