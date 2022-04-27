package no.vegvesen.ixn.model;

import javax.jms.Message;

public interface MessagePropertyValidator {

    boolean validateProperty(Message message, String propertyName);
}
