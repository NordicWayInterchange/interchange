package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.MessageValidator;
import no.vegvesen.ixn.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;
import javax.jms.Message;

@SuppressWarnings("WeakerAccess")
@SpringBootApplication
@EnableJms
public class InterchangeApp{

	private static Logger logger =LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageProducer producer;
	private final MessageValidator messageValidator;


	@Autowired
	InterchangeApp(IxnMessageProducer producer, MessageValidator messageValidator) {
		this.producer = producer;
		this.messageValidator = messageValidator;
	}


	@JmsListener(destination = "onramp")
	public void receiveMessage(Message message) throws JMSException {
		try {
			MDCUtil.setLogVariables(message);
			if (messageValidator.isValid(message)) {
				producer.sendMessage(message);
			} else {
				logger.warn("Sending bad message to dead letter queue. Invalid message.");
				producer.toDlQueue(message);
			}
		} catch (Exception e) {
			logger.error("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
			producer.toDlQueue(message);
		} finally {
			MDCUtil.removeLogVariables();
		}
	}


	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
