package no.vegvesen.ixn.model;

import no.vegvesen.ixn.CommonApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

//TODO abstract?
public class IxnBaseMessage {

    private static Logger logger = LoggerFactory.getLogger(IxnBaseMessage.class);
    final static long DEFAULT_TTL = 86_400_000L;
    final static long MAX_TTL = 6_911_200_000L;


    private final Message message;

    public IxnBaseMessage(Message message) throws JMSException {
        this.message = message;
        message.setJMSExpiration(checkExpiration(message.getJMSExpiration(),System.currentTimeMillis()));
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
    public boolean isValid() {
        try {
            boolean valid = getUserId() != null &&
                    getPublisherName() != null &&
                    getOriginatingCountry() != null &&
                    getProtocolVersion() != null &&
                    getMessageType() != null &&
                    hasDoubleProperty(CommonApplicationProperties.LATITUDE.getPropertyName()) &&
                    hasDoubleProperty(CommonApplicationProperties.LONGITUDE.getPropertyName());
            logger.debug("valid: {}",valid);
            return valid;
        } catch (JMSException e) {
            logger.error("Failed to get message property from Message.", e);
            return false;

        }
    }

    protected String getStringProperty(CommonApplicationProperties property) throws JMSException {
        return getStringProperty(property.getPropertyName());
    }

    protected String getStringProperty(String property) throws JMSException {
        return message.getStringProperty(property);
    }

    protected double getDoubleProperty(String property) throws JMSException {
        return message.getDoubleProperty(property);
    }

    protected boolean propertyExist(String property) throws JMSException {
        return message.propertyExists(property);
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

    static long checkExpiration(long expiration, long currentTime){
        if(expiration <= 0){
            // expiration is absent or illegal - setting to default ttl (1 day)
            return (DEFAULT_TTL + currentTime);
        }else if(expiration > (MAX_TTL + currentTime)){
            // expiration is too high, setting to maximum ttl (8 days)
            return (MAX_TTL + currentTime);
        }else{
            // expiration is in the valid range (more than 0, less than 8 days)
            return expiration;
        }
    }

}
