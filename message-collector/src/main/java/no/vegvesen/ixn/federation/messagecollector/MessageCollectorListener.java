package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.MessageForwardUtil;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.*;
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
