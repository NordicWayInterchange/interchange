package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.Message;

public class DenmMessage extends IxnBaseMessage {
    public DenmMessage(Message message) {
        super(message);
    }

    @Override
    public boolean isValid() throws JMSException {
        return getCauseCode() != null && super.isValid();
    }

    public String getCauseCode() throws JMSException {
        return getStringProperty("causeCode");
    }
}
