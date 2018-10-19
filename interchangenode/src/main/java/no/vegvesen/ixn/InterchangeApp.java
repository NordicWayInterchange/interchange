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
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.List;

@SpringBootApplication
@EnableJms
public class InterchangeApp implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageProducer producer;
	private final GeoLookup geoLookup;

	@Autowired
	InterchangeApp(IxnMessageProducer producer, GeoLookup geoLookup) {
		this.producer = producer;
		this.geoLookup = geoLookup;
	}

	@JmsListener(destination = "onramp")
	public void receiveMessage(TextMessage message) throws JMSException {
		logger.info("============= Received: " + message.getText());
		DispatchMessage dispatchMessage = transform(message);
		handleOneMessage(dispatchMessage);
	}

	private DispatchMessage transform(TextMessage message) throws JMSException {
		return new DispatchMessage(message.getText(), message.getFloatProperty("lat"), message.getFloatProperty("lon"));
	}

	void handleOneMessage(DispatchMessage message)  {
		MDCUtil.setLogVariables(message);
		logger.debug("handling one message body " + message.getBody());
		if (message.isValid()) {
			List<String> countries = geoLookup.getCountries(message.getLat(), message.getLong());
			logger.debug("countries " + countries);
			producer.sendMessage("test-out", message);
		}
		MDCUtil.removeLogVariables();
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}

	@Override
	public void run(String... args) {
		DispatchMessage dispatchMessage = new DispatchMessage("fisk", 10.0f, 60.0f);
		producer.sendMessage("onramp", dispatchMessage);
	}
}
