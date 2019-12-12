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

import javax.jms.Message;

@SuppressWarnings("WeakerAccess")
@SpringBootApplication
@EnableJms
public class InterchangeApp{
	public static final String DLQUEUE = "dlqueue";
	public static final String NWEXCHANGE = "nwEx";

	final static long DEFAULT_TTL = 86_400_000L;
	final static long MAX_TTL = 6_911_200_000L;

	private static Logger logger =LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageProducer producer;
	private final MessageValidator messageValidator;


	@Autowired
	InterchangeApp(IxnMessageProducer producer, MessageValidator messageValidator) {
		this.producer = producer;
		this.messageValidator = messageValidator;
	}


	@JmsListener(destination = "onramp")
	public void receiveMessage(Message message) {
		try {
			MDCUtil.setLogVariables(message);
			message.setJMSExpiration(checkExpiration(message.getJMSExpiration(),System.currentTimeMillis()));
			if (messageValidator.isValid(message)) {
				producer.sendMessage(NWEXCHANGE, message);
			} else {
				logger.error("Sending bad message to dead letter queue. Invalid message.");
				producer.sendMessage(DLQUEUE, message);
			}
		} catch (Exception e) {
			logger.error("Exception when processing message, sending bad message to dead letter queue. Invalid message.", e);
			producer.sendMessage(DLQUEUE, message);
		} finally {
			MDCUtil.removeLogVariables();
		}
	}

	public static long checkExpiration(long expiration, long currentTime){
		if(expiration <= 0){
			// expiration is absent or illegal - setting to default ttl (1 day)
			return (DEFAULT_TTL + currentTime);
		}else if(expiration > (MAX_TTL + currentTime)){
			// expiration is too high, setting to maximum ttl (8 days)
			return (MAX_TTL + currentTime);
		}else{
			// expiration is in the valid range (more than 0, less than 8 days)
			return expiration;
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
