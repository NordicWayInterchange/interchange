package no.vegvesen.ixn.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

public class DenmMessage extends IxnBaseMessage {
    public static final String CAUSE_CODE = "causeCode";
    private static Logger logger = LoggerFactory.getLogger(DenmMessage.class);

    public DenmMessage(Message message) throws JMSException {
        super(message);
    }

    @Override
    public boolean isValid() {
        try {
            return propertyExist(CAUSE_CODE) && super.isValid();
        } catch (JMSException e) {
            logger.error("Failed to get message property from Message.", e);
            return false;
        }
    }

}
