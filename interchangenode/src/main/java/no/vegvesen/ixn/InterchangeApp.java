package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static no.vegvesen.ixn.MessageProperties.LAT;
import static no.vegvesen.ixn.MessageProperties.LON;
import static no.vegvesen.ixn.MessageProperties.WHAT;

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


	public boolean isValid(TextMessage message){
		// TODO: check that the message 'who' matches the message 'userID' (check against user database).

		try{
            logger.info("Validating message");

            // Getting all the header fields and converting them to a list of Strings
            Enumeration propertyNames = message.getPropertyNames();
            List<String> headerFields = Collections.list(propertyNames);

			String what = message.getStringProperty(WHAT);
			String body = message.getText();

            return (headerFields.contains(LAT) && headerFields.contains(LON) && what != null && body != null);

		} catch(JMSException jmse) {
            logger.error("Could not get header attributes for message.", jmse);
            return false;
        }
	}

	@JmsListener(destination = "onramp")
	void handleOneMessage(TextMessage message) throws JMSException {
	    // TODO: remove message.getText() and replace with message ID
		logger.info("============= Received: " + message.getText());

		MDCUtil.setLogVariables(message);

		logger.debug("handling one message body " + message.getText());
		if (isValid(message)) {


			List<String> countries = geoLookup.getCountries(message.getFloatProperty(LAT), message.getFloatProperty(LON));
            logger.info("Countries : " + countries);

            String what = message.getStringProperty(WHAT);
            List<String> situationRecordTypes = Arrays.asList(what.split("\\s*,\\s*"));

            if(countries.size() == 0 || situationRecordTypes.size() == 0){
                logger.warn("Sending bad message to dead letter queue. Country or situation record type not set.");
                producer.dropMessage(message);
            }else {
                logger.info("Message is valid. Sending to test-out.");
                producer.sendMessage(message, countries, situationRecordTypes);
            }
		} else {
            logger.warn("Sending bad message to dead letter queue. Missing header fields or empty message.");
            producer.dropMessage(message);
        }
		MDCUtil.removeLogVariables();
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}


}
