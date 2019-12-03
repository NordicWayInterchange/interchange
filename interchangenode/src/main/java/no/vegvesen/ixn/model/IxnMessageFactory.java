package no.vegvesen.ixn.model;

import org.springframework.stereotype.Component;

import javax.jms.Message;

@Component
public class IxnMessageFactory {
    public IxnMessageFactory() {}

    //TODO this could probably be static...
    public IxnBaseMessage createIxnMessage(Message message) {
        return new IxnBaseMessage(message);
    }

}
