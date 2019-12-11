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

    public Message getMessage() {
        return message;
    }

    //TODO should probably be abstract, implemented in inherited classes.
    //TODO could use the mandatory field in CommonApplicationProperties.
    public boolean isValid() {
        try {
            boolean valid = propertyExist(CommonApplicationProperties.USER_ID) &&
                    propertyExist(CommonApplicationProperties.PUBLISHER_NAME) &&
                    propertyExist(CommonApplicationProperties.ORIGINATING_COUNTRY) &&
                    propertyExist(CommonApplicationProperties.PROTOCOL_VERSION) &&
                    propertyExist(CommonApplicationProperties.MESSAGE_TYPE) &&
                    propertyExist(CommonApplicationProperties.LATITUDE) &&
                    propertyExist(CommonApplicationProperties.LONGITUDE);
            return valid;

        } catch (JMSException e) {
            logger.error("Failed to get message property from Message.", e);
            return false;

        }
    }

    protected boolean propertyExist(CommonApplicationProperties property) throws JMSException {
        return propertyExist(property.getPropertyName());
    }

    protected boolean propertyExist(String property) throws JMSException {
        return message.propertyExists(property);
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
