package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageConsumer;
import no.vegvesen.ixn.messaging.IxnMessageConsumerCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.jms.JMSException;
import javax.naming.NamingException;

@EnableScheduling
@SpringBootApplication
public class InterchangeApp {

	private static Logger logger = LoggerFactory.getLogger(InterchangeApp.class);

	private final IxnMessageConsumerCreator consumerCreator;
	private IxnMessageConsumer consumer;

	@Autowired
	InterchangeApp(IxnMessageConsumerCreator consumerCreator) throws NamingException, JMSException {
		this.consumerCreator = consumerCreator;
		this.consumer = consumerCreator.setupConsumer();
	}

	@Scheduled(fixedRate = 30000L)
	public void checkConsumer() throws NamingException, JMSException {
		logger.debug("checking if consumer is running");
		if (!this.consumer.isRunning()) {
			consumer = consumerCreator.setupConsumer();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(InterchangeApp.class, args);
	}
}
