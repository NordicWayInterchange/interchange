package no.vegvesen.ixn.model;

import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmDataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.IviDataTypeApi;
import no.vegvesen.ixn.properties.MessageProperty;
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
			logger.error("Could not get messageType from message");
			return false;
		}
		Set<String> mandatoryPropertyNames;
		switch (messageType) {
			case Datex2DataTypeApi.DATEX_2:
				mandatoryPropertyNames = MessageProperty.mandatoryDatex2PropertyNames;
				break;
			case DenmDataTypeApi.DENM:
				mandatoryPropertyNames = MessageProperty.mandatoryDenmPropertyNames;
				break;
			case IviDataTypeApi.IVI:
				mandatoryPropertyNames = MessageProperty.mandatoryIviPropertyNames;
				break;
			default:
				return false;
		}
		return validProperties(message, mandatoryPropertyNames);
    }

    private boolean validProperties(Message message, Set<String> propertyNames) {
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
