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
	public static final String DLQUEUE = "dlqueue";
	public static final String TEST_OUT = "test-out";
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);
	private final IxnMessageProducer producer;
	private final GeoLookup geoLookup;


	// TODO: Lookup in user db on 'userID' - check if it matches 'who'. If not, log it.

	@Autowired
	InterchangeApp(IxnMessageProducer producer, GeoLookup geoLookup) {
		this.producer = producer;
		this.geoLookup = geoLookup;
	}

	public boolean isValid(TextMessage textMessage){
		logger.debug("Validating message.");

		try {
			float lon = textMessage.getFloatProperty(LON);
			float lat = textMessage.getFloatProperty(LAT);
			String who = textMessage.getStringProperty(WHO);
			String userID = textMessage.getStringProperty(USERID);
			String body = textMessage.getText();
			List<String> what = IxnMessage.parseWhat(textMessage.getStringProperty(WHAT));

			return (lon != 0 && lat != 0 && who != null && userID != null && body != null && what.size() != 0);
		}catch(JMSException jmse){
			logger.error("Failed to get message property from TextMessage.", jmse);
			return false;
		}
	}

	@JmsListener(destination = "onramp")
	public void receiveMessage(TextMessage textMessage) throws JMSException{
		MDCUtil.setLogVariables(textMessage);

		logger.info("============= Received: {}", textMessage.getText());

		if(isValid(textMessage)){
			IxnMessage message = new IxnMessage(textMessage);
			handleOneMessage(message);
		}
		else{
			logger.error("Sending bad message to dead letter queue. Invalid message.");
			producer.sendMessage(DLQUEUE, textMessage);
		}

		MDCUtil.removeLogVariables();
	}

	void handleOneMessage(IxnMessage message){
		logger.debug("handling one message body: {}", message.getBody());

		List<String> countries = geoLookup.getCountries(message.getLat(), message.getLon());
		message.setCountries(countries);
		logger.debug("Message has countries : {} ", countries);

		if(!message.hasCountries()){
			// Message does not have any countries
			logger.warn("Sending bad message to dead letter queue. 'where1' not set.");
			producer.sendMessage(DLQUEUE, message);
		}else if(!message.hasWhat()){
			// Message does not have any situation records
			logger.warn("Sending bad message to dead letter queue. 'what' not set.");
			producer.sendMessage(DLQUEUE, message);
		} else{
			logger.debug("Sending valid message to test-out.");
			producer.sendMessage(TEST_OUT, message);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
