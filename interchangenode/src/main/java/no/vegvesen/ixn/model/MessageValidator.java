package no.vegvesen.ixn.model;

/*-
 * #%L
 * interchange-node
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
