package no.vegvesen.ixn.federation.forwarding;

import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageForwardListener implements MessageListener, ExceptionListener {
    private AtomicBoolean running;
    private final MessageConsumer messageConsumer;
    private final MessageProducer producer;

    public MessageForwardListener(MessageConsumer messageConsumer, MessageProducer producer) {
        this.messageConsumer = messageConsumer;
        this.producer = producer;
        this.running = new AtomicBoolean(true);
    }

    @Override
    public void onMessage(Message message) {
        if (running.get()) {
            try {
                producer.send(message, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
            } catch (JMSException e) {
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
            throw new MessageForwarderException("Not running!");
        }
    }

    @Override
    public void onException(JMSException e) {
        //TODO log the exception
        running.set(false);
    }

    public boolean isRunning() {
        return running.get();
    }
}
