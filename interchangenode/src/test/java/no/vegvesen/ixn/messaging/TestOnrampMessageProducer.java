package no.vegvesen.ixn.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

import static no.vegvesen.ixn.MessageProperties.*;

@Component
public class TestOnrampMessageProducer {

    @Autowired
    JmsTemplate jmsTemplate;

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
}