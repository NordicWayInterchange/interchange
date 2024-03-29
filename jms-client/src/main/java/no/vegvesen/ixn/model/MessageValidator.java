package no.vegvesen.ixn.model;

import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.properties.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import java.util.Set;

public class MessageValidator {

	private static Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    private PropertyExistsValidator propertyExistsValidator = new PropertyExistsValidator();

    public boolean isValid(Message message) {
		String messageType = getMessageType(message);
		if (messageType == null) {
			logger.error("Could not get messageType from message");
			return false;
		}
		Set<String> mandatoryPropertyNames;
		switch (messageType) {
			case Constants.DATEX_2:
				mandatoryPropertyNames = MessageProperty.mandatoryDatex2PropertyNames;
				break;
			case Constants.DENM:
				mandatoryPropertyNames = MessageProperty.mandatoryDenmPropertyNames;
				break;
			case Constants.IVIM:
				mandatoryPropertyNames = MessageProperty.mandatoryIvimPropertyNames;
				break;
			case Constants.SPATEM:
				mandatoryPropertyNames = MessageProperty.mandatorySpatemMapemPropertyNames;
				break;
			case Constants.MAPEM:
				mandatoryPropertyNames = MessageProperty.mandatorySpatemMapemPropertyNames;
				break;
			case Constants.SREM:
				mandatoryPropertyNames = MessageProperty.mandatorySremSsemPropertyNames;
				break;
			case Constants.SSEM:
				mandatoryPropertyNames = MessageProperty.mandatorySremSsemPropertyNames;
				break;
			case Constants.CAM:
				mandatoryPropertyNames = MessageProperty.mandatoryCamPropertyNames;
				break;
			default:
				logger.warn("MessageType '{}' is not valid",messageType);
				return false;
		}
		return validProperties(message, mandatoryPropertyNames);
    }

    private boolean validProperties(Message message, Set<String> propertyNames) {
		boolean valid = true;
		for (String propertyName : propertyNames) {
			if (!propertyExistsValidator.validateProperty(message, propertyName)) {
				logger.warn("propertyName '{}' does not exist on message",propertyName);
				valid =  false;
			}
		}
		return valid;
	}

	private String getMessageType(Message message) {
		try {
			return message.getStringProperty(MessageProperty.MESSAGE_TYPE.getName());
		} catch (JMSException e) {
			logger.error("Could not get message type due to exception",e);
			return null;
		}
	}


}
