package no.vegvesen.ixn;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import java.util.Base64;
import java.util.Enumeration;

public class WriteToScreenMessageListener implements MessageListener {
    public WriteToScreenMessageListener() {
    }

    @Override
    public void onMessage(Message message) {
        try {
            message.acknowledge();
            long delay = calcDelay(message);
            System.out.println("** Message received **");
            printMetadataContent(message);
            switch (message) {
                case JmsBytesMessage bytesMessage -> {
                    System.out.println(" BYTES message");
                    byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(messageBytes);
                    printMessage(Base64.getEncoder().encodeToString(messageBytes));
                }
                case JmsTextMessage jmsTextMessage -> {
                    System.out.println(" TEXT message");
                    printMessage(jmsTextMessage.getBody(String.class));
                }
                default -> System.err.println("Message type unknown: " + message.getClass().getName());
            }
            System.out.println("Delay " + delay + " ms \n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private long calcDelay(Message message) {
        long delay = -1;
        try {
            long timestamp = message.getLongProperty(MessageProperty.TIMESTAMP.getName());
            delay = System.currentTimeMillis() - timestamp;
        } catch (Exception e) {
            System.err.printf("Could not get message property '%s' to calculate delay;\n",
                    MessageProperty.TIMESTAMP.getName());
        }
        return delay;
    }

    private void printMetadataContent(Message message) throws JMSException {
        //TODO: Enumeration er erstattet av Iterator, vurder om dette kan endres.
        Enumeration<String> propertyNames = message.getPropertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            Object value = message.getObjectProperty(propertyName);
            if (value instanceof String) {
                System.out.printf("%s:'%s'%n", propertyName, value);
            } else {
                System.out.printf("%s:%s:%s%n", propertyName,
                        value.getClass().getSimpleName(), value);
            }
        }
    }

    private void printMessage(String messageBody) {
        System.out.println("Body ------------");
        System.out.println(messageBody);
        System.out.println("/Body -----------");
    }
}
