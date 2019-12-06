package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.CommonApplicationProperties;
import no.vegvesen.ixn.model.KeyValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

import static no.vegvesen.ixn.MessageProperties.*;

@Component
public class TestOnrampMessageProducer {

    @Autowired
    JmsTemplate jmsTemplate;
/*
    public void sendMessage(float lat, float lon, String who, String userID, String what, String body, long expiration){
        this.jmsTemplate.send("onramp", session -> {

            TextMessage outgoingMessage = session.createTextMessage();
            outgoingMessage.setFloatProperty(LAT, lat);
            outgoingMessage.setFloatProperty(LON, lon);
            outgoingMessage.setStringProperty(WHO, who);
            outgoingMessage.setStringProperty(USERID, userID);
            outgoingMessage.setStringProperty(WHAT, what);
            outgoingMessage.setText(body);
            outgoingMessage.setJMSExpiration(expiration);
            return outgoingMessage;
        });

    }
*/
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
            TextMessage ougoingMessage = session.createTextMessage();
            ougoingMessage.setFloatProperty(CommonApplicationProperties.LATITUDE.getPropertyName(),latitude);
            ougoingMessage.setFloatProperty(CommonApplicationProperties.LONGITUDE.getPropertyName(),longitude);
            ougoingMessage.setStringProperty(CommonApplicationProperties.USER_ID.getPropertyName(),userId);
            ougoingMessage.setStringProperty(CommonApplicationProperties.PUBLISHER_NAME.getPropertyName(),publisher);
            ougoingMessage.setStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY.getPropertyName(),originatingCountry);
            ougoingMessage.setStringProperty(CommonApplicationProperties.PROTOCOL_VERSION.getPropertyName(),protocolVersion);
            ougoingMessage.setStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName(),messageType);
            for (KeyValue kv : additionalValues) {
               ougoingMessage.setStringProperty(kv.getKey(),kv.getValue());
            }
            ougoingMessage.setText(body);
            ougoingMessage.setJMSExpiration(expiration);
            return ougoingMessage;
        });
    }
}