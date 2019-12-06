package no.vegvesen.ixn.model;

import javax.jms.JMSException;
import javax.jms.Message;

public class IviMessage extends IxnBaseMessage {

    public IviMessage(Message message) throws JMSException {
        super(message);
    }
}
