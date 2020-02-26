package no.vegvesen.ixn.federation.messagecollector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
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
                log.debug("Sending message!");
                //TODO: adhere to the originally published time to live, persistence from the message
                producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
                log.debug("Message sendt!");
            } catch (JMSException e) {
                log.error("Problem receiving message", e);
                teardown();
                throw new MessageCollectorException(e);
            }
        } else {
            log.debug("Got message, but listener is not running");
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
