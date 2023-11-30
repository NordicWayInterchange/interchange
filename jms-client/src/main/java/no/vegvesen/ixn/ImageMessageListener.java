package no.vegvesen.ixn;

import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;

public class ImageMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            message.acknowledge();
            long delay = -1;
            try {
                long timestamp = message.getLongProperty(MessageProperty.TIMESTAMP.getName());
                delay = System.currentTimeMillis() - timestamp;
            } catch (Exception e) {
                System.err.printf("Could not get message property '%s' to calculate delay;\n", MessageProperty.TIMESTAMP.getName());
            }
            System.out.println("** Message received **");
            @SuppressWarnings("rawtypes") Enumeration messageNames = message.getPropertyNames();

            while (messageNames.hasMoreElements()) {
                String messageName = (String) messageNames.nextElement();
                String value = message.getStringProperty(messageName);
                System.out.println(String.format("%s:%s", messageName, value));
            }

            System.out.println(" BYTES message");
            JmsBytesMessage bytesMessage = (JmsBytesMessage) message;
            byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(messageBytes);
            Path imagePath = Paths.get("target/receivedImage.jpg");
            Files.write(imagePath, messageBytes, StandardOpenOption.CREATE);

            System.out.println("Body ------------");
            System.out.println("IMAGE RECEIVED");
            System.out.println("/Body -----------");
            System.out.println("Delay " + delay + " ms \n");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
