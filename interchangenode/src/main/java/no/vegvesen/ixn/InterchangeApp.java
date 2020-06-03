package no.vegvesen.ixn;

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

import no.vegvesen.ixn.messaging.IxnMessageConsumer;
import no.vegvesen.ixn.messaging.IxnMessageConsumerCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.naming.NamingException;

@Service
public class InterchangeApp {

	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageConsumerCreator consumerCreator;
	private IxnMessageConsumer consumer;

	@Autowired
	InterchangeApp(IxnMessageConsumerCreator consumerCreator) throws NamingException, JMSException {
		this.consumerCreator = consumerCreator;
		this.consumer = consumerCreator.setupConsumer();
	}

	@Scheduled(fixedRate = 30000L)
	public void checkConsumer() throws NamingException, JMSException {
		logger.debug("checking if consumer is running");
		if (this.consumer == null || !this.consumer.isRunning()) {
			consumer = consumerCreator.setupConsumer();
		}
	}
/*
	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
	*/
}
