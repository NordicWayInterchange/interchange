package no.vegvesen.ixn.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

public class DenmMessage extends IxnBaseMessage {
    private static Logger logger = LoggerFactory.getLogger(DenmMessage.class);

    public DenmMessage(Message message) throws JMSException {
        super(message);
    }

    @Override
    public boolean isValid() {
        try {
            return getCauseCode() != null && super.isValid();
        } catch (JMSException e) {
            logger.error("Failed to get message property from Message.", e);
            return false;
        }
    }

    public String getCauseCode() throws JMSException {
        return getStringProperty("causeCode");
    }
}
