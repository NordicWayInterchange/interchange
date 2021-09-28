package no.vegvesen.ixn;

import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsBytesMessage;

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
        JmsBytesMessage message = createBytesMessage();
        message.getFacade().setUserId("localhost");
        message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), "DENM");
        message.setStringProperty(MessageProperty.PUBLISHER_ID.getName(), "NO-12345");
        message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), "DENM:1.2.2");
        message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
        message.setDoubleProperty(MessageProperty.LATITUDE.getName(), 60.352374);
        message.setDoubleProperty(MessageProperty.LONGITUDE.getName(), 13.334253);
        message.setStringProperty(MessageProperty.SERVICE_TYPE.getName(), "some-denm-service-type");
        message.setStringProperty(MessageProperty.CAUSE_CODE.getName(), "3");
        message.setStringProperty(MessageProperty.SUB_CAUSE_CODE.getName(), "6");
        message.setLongProperty(MessageProperty.TIMESTAMP.getName(), System.currentTimeMillis());

        message.writeBytes(convertImageToByteArray(imageName));
        send(message, Message.DEFAULT_TIME_TO_LIVE);
    }

    public void sendNonPersistentByteMessageWithImage(String originatingCountry, String messageQuadTreeTiles, String imageName) throws JMSException, IOException {
        JmsBytesMessage message = createBytesMessage();
        message.getFacade().setUserId("localhost");
        message.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(), "DENM");
        message.setStringProperty(MessageProperty.PUBLISHER_ID.getName(), "NO-12345");
        message.setStringProperty(MessageProperty.PROTOCOL_VERSION.getName(), "DENM:1.2.2");
        message.setStringProperty(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
        message.setStringProperty(MessageProperty.QUAD_TREE.getName(), messageQuadTreeTiles);
        message.setDoubleProperty(MessageProperty.LATITUDE.getName(), 60.352374);
        message.setDoubleProperty(MessageProperty.LONGITUDE.getName(), 13.334253);
        message.setStringProperty(MessageProperty.SERVICE_TYPE.getName(), "some-denm-service-type");
        message.setStringProperty(MessageProperty.CAUSE_CODE.getName(), "3");
        message.setStringProperty(MessageProperty.SUB_CAUSE_CODE.getName(), "6");
        message.setLongProperty(MessageProperty.TIMESTAMP.getName(), System.currentTimeMillis());

        message.writeBytes(convertImageToByteArray(imageName));
        sendNonPersistentMessage(message, Message.DEFAULT_TIME_TO_LIVE);
    }

    public static byte[] convertImageToByteArray(String imageName) throws IOException {
        File image = new File(imageName);
        byte [] bytes = Files.readAllBytes(image.toPath());
        return bytes;
    }
}
