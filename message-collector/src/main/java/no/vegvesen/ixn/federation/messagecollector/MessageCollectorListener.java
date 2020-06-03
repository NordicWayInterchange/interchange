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

import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageCollectorListener implements MessageListener, ExceptionListener {
    private AtomicBoolean running;
    private final Sink sink;
    private final Source source;
    private Logger log = LoggerFactory.getLogger(MessageCollectorListener.class);

    MessageCollectorListener(Sink sink, Source source) {
        this.sink = sink;
        this.source = source;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void onMessage(Message message) {
        log.debug("Message received!");
        if (running.get()) {
            try {
				MessageForwardUtil.send(source.getProducer(), message);
            } catch (JMSException e) {
                log.error("Problem receiving message", e);
                teardown();
                throw new MessageCollectorException(e);
            }
        } else {
            log.debug("Got message, but listener is not running");
            this.teardown();
            throw new MessageCollectorException("Not running!");
        }
    }

    public void teardown()  {
		try {
			sink.close();
		} catch (Exception ignore) {
		}
        try {
			source.close();
        } catch (Exception ignore) {
        } finally {
            running.set(false);
        }
    }

    @Override
    public void onException(JMSException e) {
        log.error("Exception caught",e);
        this.teardown();
    }


    boolean isRunning() {
        return running.get();
    }
}
