package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.MessageProperty;
import no.vegvesen.ixn.util.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

@Component
public class TestOnrampMessageProducer {

    @Autowired
    JmsTemplate jmsTemplate;

    public void sendMessage(String userId,
                            String publisher,
                            String originatingCountry,
                            String protocolVersion,
                            String messageType,
                            float latitude,
                            float longitude,
                            String body,
                            long expiration,
                            KeyValue ... additionalValues
                            ) {
        this.jmsTemplate.send("onramp", session -> {
            TextMessage outgoingMessage = session.createTextMessage();
            outgoingMessage.setFloatProperty("latitude",latitude);
            outgoingMessage.setFloatProperty("longitude",longitude);
            outgoingMessage.setStringProperty(MessageProperty.USER_ID.getName(),userId);
            outgoingMessage.setStringProperty("publisherName",publisher);
            outgoingMessage.setStringProperty("originatingCountry",originatingCountry);
            outgoingMessage.setStringProperty("protocolVersion",protocolVersion);
            outgoingMessage.setStringProperty(MessageProperty.MESSAGE_TYPE.getName(),messageType);
            for (KeyValue kv : additionalValues) {
               outgoingMessage.setStringProperty(kv.getKey(),kv.getValue());
            }
            outgoingMessage.setText(body);
            outgoingMessage.setJMSExpiration(expiration);
            return outgoingMessage;
        });
    }
}