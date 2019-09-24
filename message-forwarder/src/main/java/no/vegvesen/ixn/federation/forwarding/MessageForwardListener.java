package no.vegvesen.ixn.federation.forwarding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageForwardListener implements MessageListener, ExceptionListener {
    private AtomicBoolean running;
    private final MessageConsumer messageConsumer;
    private final MessageProducer producer;
    private Logger log = LoggerFactory.getLogger(MessageForwardListener.class);

    MessageForwardListener(MessageConsumer messageConsumer, MessageProducer producer) {
        this.messageConsumer = messageConsumer;
        this.producer = producer;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void onMessage(Message message) {
        log.debug("Message received!");
        if (running.get()) {
            try {
                log.debug("Sending message!");
                //TODO: adhere to the originally published time to live, persistence from the message
                producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                log.debug("Message sendt!");
            } catch (JMSException e) {
                log.error("Problem receiving message", e);
                //TODO what to do? Probably need to mark as unusable, and tear down???
                try {
                    producer.close();
                    messageConsumer.close();
                } catch (JMSException e1) {
                    throw new MessageForwarderException(e1);
                } finally {
                    running.set(false);
                }
                throw new MessageForwarderException(e);
            }
        } else {
            log.debug("Got message, but listener is not running");
            throw new MessageForwarderException("Not running!");
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
