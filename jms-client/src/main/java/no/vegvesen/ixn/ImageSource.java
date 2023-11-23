package no.vegvesen.ixn;

import org.apache.qpid.jms.message.JmsMessage;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ImageSource extends Source{

    public ImageSource(String url, String sendQueue, SSLContext context) {
        super(url, sendQueue, context);
    }

    public void sendByteMessageWithImage(String originatingCountry, String messageQuadTreeTiles, String imageName) throws JMSException, IOException {
        JmsMessage message = createMessageBuilder()
                .userId("localhost")
                .messageType("DENM")
                .publisherId("NO-12345")
                .protocolVersion("DENM:1.2.2")
                .originatingCountry(originatingCountry)
                .quadTreeTiles(messageQuadTreeTiles)
                .latitude(60.352374)
                .longitude(13.334253)
                .serviceType("some-denm-service-type")
                .causeCode(3)
                .subCauseCode(6)
                .timestamp(System.currentTimeMillis())
                .bytesMessage(convertImageToByteArray(imageName))
                .build();
        send(message, Message.DEFAULT_TIME_TO_LIVE);
    }

    public void sendNonPersistentByteMessageWithImage(String originatingCountry, String messageQuadTreeTiles, String imageName) throws JMSException, IOException {
        JmsMessage message = createMessageBuilder()
                .bytesMessage(convertImageToByteArray(imageName))
                .userId("localhost")
                .messageType("DENM")
                .publisherId("NO-12345")
                .publicationId("123")
                .protocolVersion("DENM:1.2.2")
                .originatingCountry(originatingCountry)
                .quadTreeTiles(messageQuadTreeTiles)
                .latitude(60.352374)
                .longitude(13.334253)
                .serviceType("some-denm-service-type")
                .causeCode(3)
                .subCauseCode(6)
                .timestamp(System.currentTimeMillis())
                .build();
        sendNonPersistentMessage(message, Message.DEFAULT_TIME_TO_LIVE);
    }

    public static byte[] convertImageToByteArray(String imageName) throws IOException {
        File image = new File(imageName);
        byte [] bytes = Files.readAllBytes(image.toPath());
        return bytes;
    }
}
