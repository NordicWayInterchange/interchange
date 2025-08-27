package no.vegvesen.ixn;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import no.vegvesen.ixn.properties.MessageProperty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class WriteToFileMessageListener implements MessageListener {
    public WriteToFileMessageListener(String directoryName) {
        this.directory = new File(directoryName);
    }
    File directory;
    Long messages = 0L;

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
            Enumeration<String> propertyNames =  message.getPropertyNames();

            Map<String, Object> metadataContent = new HashMap<>();

            while (propertyNames.hasMoreElements()) {
                String propertyName = propertyNames.nextElement();
                Object value = message.getObjectProperty(propertyName);
                if(directory != null){
                    metadataContent.put(propertyName, value);
                }
                else {
                    if (value instanceof String) {
                        System.out.printf("%s:'%s'%n", propertyName, value);
                    } else {
                        System.out.printf("%s:%s:%s%n", propertyName, value.getClass().getSimpleName(), value);
                    }
                }
            }

            String messageBody = null;
            messages += 1;
            File messageFile = new File(directory, "file-"+messages);
            File metadataFile = new File(directory, "file-"+messages+"-metadata.txt");

            byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(messageBytes);
            if(directory != null) {
                try (FileOutputStream fos = new FileOutputStream(messageFile)) {
                    fos.write(messageBytes);
                }
                try (PrintWriter printWriter = new PrintWriter(metadataFile)) {
                    printWriter.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(metadataContent));
                }
            }
            else{
                System.out.println(" BYTES message");
                messageBody = Base64.getEncoder().encodeToString(messageBytes);
            }
            System.out.println("Delay " + delay + " ms \n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
