package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.Message;

public class Datex2Message extends IxnBaseMessage {

    public static final String PUBLICATION_TYPE = "publicationType";

    public Datex2Message(Message message) {
        super(message);
    }

    public String getPublicationType() throws JMSException {
        return getStringProperty(PUBLICATION_TYPE);
    }

    @Override
    public boolean isValid() throws JMSException {
        return getPublicationType() != null && super.isValid();
    }
}
