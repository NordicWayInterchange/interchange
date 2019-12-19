package no.vegvesen.ixn.model;

import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Set;

@Component
public class MessageValidator {

	private static Logger logger = LoggerFactory.getLogger(MessageValidator.class);

    private PropertyExistsValidator propertyExistsValidator = new PropertyExistsValidator();


    public boolean isValid(Message message) {
		String messageType = getMessageType(message);
		if (messageType == null) {
			return false;
		}
		try {
			String jmsXUserId = message.getStringProperty("JMSXUserID");
			logger.debug("JMSXUserID is {}",jmsXUserId);
		} catch (JMSException e) {
			logger.error("Could not get userId from message");
		}
		if (message instanceof JmsMessage) {
			JmsMessage jmsMessage = (JmsMessage)message;
			String userId = jmsMessage.getFacade().getUserId();
			logger.debug("userId is '{}'",userId);
		} else {
			logger.debug("Message is not an instance of jmsMessage, but {}", message.getClass().getName());
		}
		switch (messageType) {
			case "DATEX2":
				//validate the datex message;
				if (!validateProperties(message, MessageProperty.mandatoryDatex2PropertyNames)) {
					return false;
				}
				break;
			case "DENM":
				//validate the DENM message;
				if (!validateProperties(message, MessageProperty.mandatoryDenmPropertyNames)) {
					return false;
				}
				break;
			case "IVI":
				//validate the IVI message;
				if (!validateProperties(message, MessageProperty.mandatoryIviPropertyNames)) {
					return false;
				}
				break;
			default:
				return false;

		}
		return true;
    }

    private boolean validateProperties(Message message, Set<String> propertyNames) {
		for (String propertyName : propertyNames) {
			if (!propertyExistsValidator.validateProperty(message, propertyName)) {
				logger.warn("propertyName '{}' does not exist on message",propertyName);
				return false;
			}
		}
		return true;
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
