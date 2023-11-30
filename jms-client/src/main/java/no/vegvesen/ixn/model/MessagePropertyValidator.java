package no.vegvesen.ixn.model;

import jakarta.jms.Message;

public interface MessagePropertyValidator {

    boolean validateProperty(Message message, String propertyName);
}
