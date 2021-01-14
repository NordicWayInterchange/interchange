package no.vegvesen.ixn;

import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

public class ImageSink extends Sink {

    private static Logger logger = LoggerFactory.getLogger(ImageSink.class);

    public ImageSink(String url, String queueName, SSLContext sslContext) {
        super(url, queueName, sslContext);
    }

    @Override
    public void onMessage(Message message) {
        try {
            message.acknowledge();
            long delay = -1;
            try {
                long  timestamp = message.getLongProperty(MessageProperty.TIMESTAMP.getName());
                delay = System.currentTimeMillis() - timestamp;
            } catch (Exception e) {
                System.err.printf("Could not get message property '%s' to calculate delay;\n", MessageProperty.TIMESTAMP.getName());
            }
            System.out.println("** Message received **");
            @SuppressWarnings("rawtypes") Enumeration messageNames =  message.getPropertyNames();

            while (messageNames.hasMoreElements()) {
                String messageName = (String) messageNames.nextElement();
                String value = message.getStringProperty(messageName);
                System.out.println(String.format("%s:%s",messageName,value));
            }

            System.out.println(" BYTES message");
            JmsBytesMessage bytesMessage = (JmsBytesMessage) message;
            byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(messageBytes);
            File image = new File("target/receivedImage.jpg");
            Path imagePath = image.toPath();
            Files.write(imagePath, messageBytes);

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
