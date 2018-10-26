package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnMessage;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.Arrays;
import java.util.List;

import static no.vegvesen.ixn.MessageProperties.*;

@SpringBootApplication
@EnableJms
public class InterchangeApp{
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);
	private final IxnMessageProducer producer;
	private final GeoLookup geoLookup;

	@Autowired
	InterchangeApp(IxnMessageProducer producer, GeoLookup geoLookup) {
		this.producer = producer;
		this.geoLookup = geoLookup;
	}

	public boolean isValid(TextMessage textMessage){
	    // TODO: Is userID from getstringProperty or somewhere else? ('JMSXUserID')
		// TODO: check that the message 'who' matches the message 'userID' (check against user database).

		logger.debug("Validating message.");

		try {
			float lon = textMessage.getFloatProperty(LON);
			float lat = textMessage.getFloatProperty(LAT);
			String who = textMessage.getStringProperty(WHO);
			String userID = textMessage.getStringProperty(USERID);
			String body = textMessage.getText();

			String whatString = textMessage.getStringProperty(WHAT);
			List<String> what = Arrays.asList(whatString.split("\\s*,\\s*"));

			return (lon != 0 && lat != 0 && who != null && userID != null && body != null && what.size() != 0);
		}catch(JMSException jmse){
			logger.error("Failed to get message property from TextMessage.", jmse);
			return false;
		}
	}

	@JmsListener(destination = "onramp")
	public void receiveMessage(TextMessage textMessage) throws JMSException{
		logger.info("============= Received: " + textMessage.getText());

		if(isValid(textMessage)){
			IxnMessage message = new IxnMessage(textMessage);
			handleOneMessage(message);
		}
		else{
			throw new IllegalArgumentException();
		}
	}

	void handleOneMessage(IxnMessage message){
	    // TODO: remove message.getText() and replace with message ID
		// TODO: MDCUtil.setLogVariables(message);
		logger.debug("handling one message body " + message.getBody());

		List<String> countries = geoLookup.getCountries(message.getLat(), message.getLon());
		message.setCountries(countries);
		logger.debug("Message has countries : " + countries);

		if (message.hasCountries()) {
			logger.debug("Sending valid message to test-out.");
            producer.sendMessage("test-out", message);

		} else {
            logger.warn("Sending bad message to dead letter queue. Invalid message, or message without 'where1'.");
            producer.sendMessage("dlqueue", message);
        }
		MDCUtil.removeLogVariables();
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
