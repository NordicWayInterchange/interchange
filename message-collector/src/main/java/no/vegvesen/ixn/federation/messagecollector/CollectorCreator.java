package no.vegvesen.ixn.federation.messagecollector;

/*-
 * #%L
 * message-collector
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

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

@Component
public class CollectorCreator {


    private final SSLContext sslContext;
    private Logger logger = LoggerFactory.getLogger(CollectorCreator.class);
    private String localIxnDomainName;
    private String localIxnFederationPort;
    private String writeQueue;

    @Autowired
    public CollectorCreator(SSLContext sslContext,
                            @Value("${collector.localIxnDomainName}") String localIxnDomainName,
                            @Value("${collector.localIxnFederationPort}") String localIxnFederationPort,
                            @Value("${collector.writequeue}") String writequeue) {
        this.sslContext = sslContext;
        this.localIxnDomainName = localIxnDomainName;
        this.localIxnFederationPort = localIxnFederationPort;
        this.writeQueue = writequeue;
    }

    MessageCollectorListener setupCollection(Neighbour ixn) {
        String writeUrl = String.format("amqps://%s:%s", localIxnDomainName, localIxnFederationPort);
        logger.debug("Write URL: {}, queue {}", writeUrl, writeQueue);
        Source writeSource = new Source(writeUrl, writeQueue, sslContext);

        String readUrl = ixn.getMessageChannelUrl();
        String readQueue = this.localIxnDomainName;
		Sink readSink = new Sink(readUrl, readQueue, sslContext);

        MessageCollectorListener listener = new MessageCollectorListener(readSink, writeSource);
		try {
			writeSource.start();
			writeSource.setExceptionListener(listener);
			readSink.startWithMessageListener(listener);
			readSink.setExceptionListener(listener);
		} catch (NamingException | JMSException e) {
			listener.teardown();
			throw new MessageCollectorException("Tried to set up a new MessageCollectorListener, tearing down.", e);
		}
        return listener;
    }


}
