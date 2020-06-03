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

import no.vegvesen.ixn.BasicAuthSink;
import no.vegvesen.ixn.BasicAuthSource;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.model.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;

@Component
public class IxnMessageConsumerCreator {
	public static final String ONRAMP = "onramp";
	public static final String DLQUEUE = "dlqueue";
	public static final String NWEXCHANGE = "nwEx";

    private Logger logger = LoggerFactory.getLogger(IxnMessageConsumerCreator.class);
	private final String amqpUrl;
	private final String username;
	private final String password;
	private final MessageValidator messageValidator;

	@Autowired
    public IxnMessageConsumerCreator(@Value("${amqphub.amqp10jms.remote-url}") String amqpUrl,
									 @Value("${amqphub.amqp10jms.username}") String username,
									 @Value("${amqphub.amqp10jms.password}") String password,
									 MessageValidator messageValidator) {
    	this.amqpUrl = amqpUrl;
    	this.username = username;
    	this.password = password;
		this.messageValidator = messageValidator;
	}

    public IxnMessageConsumer setupConsumer() throws JMSException, NamingException {
		logger.debug("setting up consumer for onramp and producers for nwEx and dlqueue");
    	Source dlQueue = new BasicAuthSource(amqpUrl, DLQUEUE, username, password);
    	Source nwEx = new BasicAuthSource(amqpUrl, NWEXCHANGE, username, password);
		Sink onramp = new BasicAuthSink(amqpUrl, ONRAMP, username, password);
		IxnMessageConsumer consumer = new IxnMessageConsumer(onramp, nwEx, dlQueue, messageValidator);
		onramp.startWithMessageListener(consumer);
		onramp.setExceptionListener(consumer);
		dlQueue.start();
		dlQueue.setExceptionListener(consumer);
		nwEx.start();
		nwEx.setExceptionListener(consumer);
		return consumer;
    }

}
