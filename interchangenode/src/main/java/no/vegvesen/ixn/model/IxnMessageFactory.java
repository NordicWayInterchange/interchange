package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

@Component
public class IxnMessageFactory {
    private IxnMessageFactory() {}

    //TODO this could probably be static...
    public static IxnBaseMessage createIxnMessage(Message message) throws JMSException {
        String messageType = message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.name());
        if (messageType == null) {
            throw new MessageFactoryException("MessageType cannot be null");
        }
        switch (messageType) {
            case "DATEX2":
                return new Datex2Message(message);
            case "DENM":
                return new DenmMessage(message);
            case "IVI":
                return new IviMessage(message);
            default:
                throw new MessageFactoryException(String.format("Unknown message type %s",messageType));
        }
    }

}
