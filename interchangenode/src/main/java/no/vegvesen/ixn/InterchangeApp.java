package no.vegvesen.ixn;

import no.vegvesen.ixn.geo.GeoLookup;
import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.DispatchMessage;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@SpringBootApplication
@EnableJms
public class InterchangeApp implements CommandLineRunner{
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageProducer producer;
	private final GeoLookup geoLookup;

	@Autowired
	InterchangeApp(IxnMessageProducer producer, GeoLookup geoLookup) {
		this.producer = producer;
		this.geoLookup = geoLookup;
	}




	public boolean isValid(TextMessage message) throws JMSException{
		// TODO: check that the message 'who' matches the message 'userID' (check against user database).

		try{
            logger.info("Validating message");

            // Getting all the header fields and converting them to a list of Strings
            Enumeration propertyNames = message.getPropertyNames();
            List<String> headerFields = Collections.list(propertyNames);

			String what = message.getStringProperty("what");
			String body = message.getText();

            return (headerFields.contains("lat") && headerFields.contains("lon") && what != null && body != null);

		} catch(JMSException jmse) {
            logger.error("Could not get header attributes for message", jmse);
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
			List<String> countries = geoLookup.getCountries(message.getFloatProperty("lat"), message.getFloatProperty("lon"));
			logger.debug("countries " + countries);
			producer.sendMessage("test-out", message);
		} else {
		    // Message is not valid, send to dlqueue
            logger.info("Sending bad message to dead letter queue");
            producer.sendMessage("dlqueue", message);
        }
		MDCUtil.removeLogVariables();
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}

	@Override
	public void run(String... args){

		//producer.sendMessage("onramp", 10.0f, 63.0f, "Obstruction", "This is a message");
        producer.sendBadMessage("onramp", 10.0f, "This message has no 'lon' ");


	}

}
