package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;

import javax.jms.JMSException;
import javax.jms.Message;

//TODO abstract?
//TODO have userId, but how do I solve TTL?
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

    public String getMessageType() throws JMSException {
        return getStringProperty(CommonApplicationProperties.MESSAGE_TYPE);
    }

    public Message getMessage() {
        return message;
    }

    //TODO should probably be abstract, implemented in inherited classes.
    //TODO could use the mandatory field in CommonApplicationProperties.
    public boolean isValid() throws JMSException {
        return  getUserId() != null &&
                getPublisherName() != null &&
                getOriginatingCountry() != null &&
                getProtocolVersion() != null &&
                getMessageType() != null &&
                hasDoubleProperty(CommonApplicationProperties.LATITUDE.name()) &&
                hasDoubleProperty(CommonApplicationProperties.LONGITUDE.name());
    }

    protected String getStringProperty(CommonApplicationProperties property) throws JMSException {
        return getStringProperty(property.name());
    }

    protected String getStringProperty(String property) throws JMSException {
        return message.getStringProperty(property);
    }

    protected double getDoubleProperty(String property) throws JMSException {
        return message.getDoubleProperty(property);
    }

    /*
        NullPointer is thrown when the header does not exist, as this is what is thrown by Double.valueOf(null)
        see https://docs.oracle.com/javaee/6/api/javax/jms/Message.html and IxnBaseMessageTest#testParseNullValueForDouble
     */
    protected boolean hasDoubleProperty(String property) throws JMSException {
        try {
            getDoubleProperty(property);
            return true; //No exception thrown
        } catch (NullPointerException e) {
            return false;
        }
    }

}
