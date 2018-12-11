package no.vegvesen.ixn.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.TextMessage;

import static no.vegvesen.ixn.MessageProperties.*;

@Component
@Profile("63")
public class TestOnrampMessageProducer {

    @Autowired
    @Qualifier("partnerJmsTemplate")
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