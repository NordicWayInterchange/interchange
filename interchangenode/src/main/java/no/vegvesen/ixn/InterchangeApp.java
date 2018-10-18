package no.vegvesen.ixn;

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

@SpringBootApplication
@EnableJms
public class InterchangeApp implements CommandLineRunner {
	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	private IxnMessageProducer producer;

	@Autowired
	InterchangeApp(IxnMessageProducer producer) {
		this.producer = producer;
	}

	@JmsListener(destination = "onramp")
	public void receiveMessage(TextMessage message) throws JMSException {
		logger.info("============= Received: " + message.getText());
		DispatchMessage dispatchMessage = transform(message);
		handleOneMessage(dispatchMessage);
	}

	private DispatchMessage transform(TextMessage message) throws JMSException {
		return new DispatchMessage(message.getText());
	}

	void handleOneMessage(DispatchMessage message)  {
		MDCUtil.setLogVariables(message);
		logger.debug("handling one message body " + message.getBody());
		if (valid(message)) {
			producer.sendMessage("test-out", message.getBody());
		}
		MDCUtil.removeLogVariables();
	}

	private boolean valid(DispatchMessage message){
		return message.getBody() != null;
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}

	@Override
	public void run(String... args) {
		producer.sendMessage("onramp", "fisk");
	}
}
