package no.vegvesen.ixn;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import no.vegvesen.ixn.model.DirectoryDoesNotExistException;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsTextMessage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class WriteToFileMessageListener implements MessageListener {
    File directory;
    Long messages = 0L;

    public WriteToFileMessageListener(String directoryName) {
        this.directory = new File(directoryName);
        if(!directory.exists()){
            throw new DirectoryDoesNotExistException("Directory does not exist: " + directoryName);
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            message.acknowledge();
            long delay = calcDelay(message);
            System.out.println("** Message received **");
            Map<String, Object> metadataContent = createMetadataContentMap(message);

            messages += 1;
            File messageFile = new File(directory, "file-"+messages);
            File metadataFile = new File(directory, "file-"+messages+"-metadata.txt");

            switch (message) {
                case JmsBytesMessage bytesMessage -> {
                    byte[] messageBytes = new byte[(int) bytesMessage.getBodyLength()];
                    bytesMessage.readBytes(messageBytes);
                    try (FileOutputStream fos = new FileOutputStream(messageFile)) {
                        fos.write(messageBytes);
                    }
                    try (PrintWriter printWriter = new PrintWriter(metadataFile)) {
                        printWriter.write(new ObjectMapper()
                                .writerWithDefaultPrettyPrinter()
                                .writeValueAsString(metadataContent));
                    }
                }
                case JmsTextMessage jmsTextMessage -> {
                    try (PrintWriter printWriter = new PrintWriter(messageFile)) {
                        printWriter.write(jmsTextMessage.getBody(String.class));
                    }
                    try (PrintWriter printWriter = new PrintWriter(metadataFile)) {
                        printWriter.write(new ObjectMapper()
                                .writeValueAsString(metadataContent));
                    }
                }
                default -> System.err.println("Message type unknown: " + message.getClass().getName());
            }
            System.out.println(String.format("Message written to %s, metadata written to %s", messageFile.getPath(), metadataFile.getPath()));
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

    private Map<String, Object> createMetadataContentMap(Message message) throws JMSException {
        Enumeration<String> propertyNames = message.getPropertyNames();
        Map<String, Object> metadataContent = new HashMap<>();
        while (propertyNames.hasMoreElements()) {
            String propertyName = propertyNames.nextElement();
            Object value = message.getObjectProperty(propertyName);
            metadataContent.put(propertyName, value);
        }
        return metadataContent;
    }
}
