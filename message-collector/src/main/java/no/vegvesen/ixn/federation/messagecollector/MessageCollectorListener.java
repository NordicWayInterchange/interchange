package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.MessageForwardUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageCollectorListener implements MessageListener, ExceptionListener {
    private AtomicBoolean running;
    private final MessageConsumer messageConsumer;
    private final MessageProducer producer;
    private Logger log = LoggerFactory.getLogger(MessageCollectorListener.class);

    MessageCollectorListener(MessageConsumer messageConsumer, MessageProducer producer) {
        this.messageConsumer = messageConsumer;
        this.producer = producer;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void onMessage(Message message) {
        log.debug("Message received!");
        if (running.get()) {
            try {
				MessageForwardUtil.send(producer, message);
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
            producer.close();
            messageConsumer.close();
        } catch (JMSException ignore) {
        } finally {
            running.set(false);
        }
    }

    @Override
    public void onException(JMSException e) {
        log.error("Exception caught",e);
        running.set(false);
    }


    boolean isRunning() {
        return running.get();
    }
}
