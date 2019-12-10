package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;

@Component
public class IxnMessageFactory {

    public static final String DATEX_2 = "DATEX2";
    public static final String DENM = "DENM";
    public static final String IVI = "IVI";

    private IxnMessageFactory() {}

    public static IxnBaseMessage createIxnMessage(Message message) throws JMSException {
        String messageType = message.getStringProperty(CommonApplicationProperties.MESSAGE_TYPE.getPropertyName());
        if (messageType == null) {
            throw new MessageFactoryException("MessageType cannot be null");
        }
        switch (messageType) {
            case DATEX_2:
                return new Datex2Message(message);
            case DENM:
                return new DenmMessage(message);
            case IVI:
                return new IviMessage(message);
            default:
                throw new MessageFactoryException(String.format("Unknown message type %s",messageType));
        }
    }

}
