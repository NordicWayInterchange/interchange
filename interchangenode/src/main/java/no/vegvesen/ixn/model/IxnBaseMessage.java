package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;

import javax.jms.JMSException;
import javax.jms.Message;

//TODO abstract?
public class IxnBaseMessage {

    private final Message message;

    public IxnBaseMessage(Message message) {
        this.message = message;
    }

    //TODO should we use runtimeExceptions instead?
    public String getUserId() throws JMSException {
        return getStringProperty(CommonApplicationProperties.USER_ID);
    }

    public String getPublisherName() throws JMSException {
        return getStringProperty(CommonApplicationProperties.PUBLISHER_NAME);
    }

    public String getOriginatingCountry() throws JMSException {
        return getStringProperty(CommonApplicationProperties.ORIGINATING_COUNTRY);
    }

    public String getProtocolVersion() throws JMSException {
        return getStringProperty(CommonApplicationProperties.PROTOCOL_VERSION);
    }

    public Message getMessage() {
        return message;
    }

    //TODO should probably be abstract, implemented in inherited classes.
    public boolean isValid() throws JMSException {
        return  getUserId() != null &&
                getPublisherName() != null &&
                getOriginatingCountry() != null &&
                getProtocolVersion() != null;
    }

    private String getStringProperty(CommonApplicationProperties property) throws JMSException {
        return message.getStringProperty(property.name());
    }
}
