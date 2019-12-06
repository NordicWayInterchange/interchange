package no.vegvesen.ixn.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

public class Datex2Message extends IxnBaseMessage {
    private static Logger logger = LoggerFactory.getLogger(Datex2Message.class);

    public static final String PUBLICATION_TYPE = "publicationType";

    public Datex2Message(Message message) throws JMSException {
        super(message);
    }

    public String getPublicationType() throws JMSException {
        return getStringProperty(PUBLICATION_TYPE);
    }

    @Override
    public boolean isValid() {
        try {
            return getPublicationType() != null && super.isValid();
        } catch (JMSException e) {
            logger.error("Failed to get message property from Message.", e);
            return false;
        }
    }
}
