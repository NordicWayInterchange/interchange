package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import no.vegvesen.ixn.model.IxnBaseMessage;
import no.vegvesen.ixn.model.IxnMessageFactory;
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
	public static final String DLQUEUE = "dlqueue";
	public static final String NWEXCHANGE = "nwEx";
	private static Logger logger =LoggerFactory.getLogger(InterchangeApp.class);
	private final IxnMessageProducer producer;


	@Autowired
	InterchangeApp(IxnMessageProducer producer) {
		this.producer = producer;
	}


	@JmsListener(destination = "onramp")
	public void receiveMessage(Message message) throws JMSException{
		IxnBaseMessage ixnMessage;
		try {
			MDCUtil.setLogVariables(message);

			ixnMessage = IxnMessageFactory.createIxnMessage(message);

			if (ixnMessage.isValid()) {
				producer.sendMessage(NWEXCHANGE, ixnMessage);
			} else {
				logger.error("Sending bad message to dead letter queue. Invalid message.");
				//producer.sendMessage(DLQUEUE, textMessage);
				producer.sendMessage(DLQUEUE, ixnMessage);
			}
		}  catch (JMSException jmse){
			logger.error("Error while receiving message, rethrowing exception to keep the message on the queue.", jmse);
			throw jmse;
		} catch (Exception e) {
			logger.error("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
			producer.sendMessage(DLQUEUE, message);
		} finally {
			MDCUtil.removeLogVariables();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
