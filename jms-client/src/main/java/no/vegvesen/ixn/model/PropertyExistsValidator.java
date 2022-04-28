package no.vegvesen.ixn.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;

public class PropertyExistsValidator implements MessagePropertyValidator {

	private static Logger logger = LoggerFactory.getLogger(PropertyExistsValidator.class);

    @Override
    public boolean validateProperty(Message message, String propertyName) {
		try {
			return message.propertyExists(propertyName);
		} catch (JMSException e) {
			logger.error("Could not determine if property {} exists", propertyName,e);
			return false;
		}
	}

}
