package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.BasicAuthSink;
import no.vegvesen.ixn.BasicAuthSource;
import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.model.MessageValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.naming.NamingException;

@Component
public class IxnMessageConsumerCreator {
	public static final String ONRAMP = "onramp";
	public static final String DLQUEUE = "dlqueue";
	public static final String NWEXCHANGE = "nwEx";

    private Logger logger = LoggerFactory.getLogger(IxnMessageConsumerCreator.class);
	private final String amqpUrl;
	private final String username;
	private final String password;
	private final MessageValidator messageValidator;

	@Autowired
    public IxnMessageConsumerCreator(@Value("${amqphub.amqp10jms.remote-url}") String amqpUrl,
									 @Value("${amqphub.amqp10jms.username}") String username,
									 @Value("${amqphub.amqp10jms.password}") String password,
									 MessageValidator messageValidator) {
    	this.amqpUrl = amqpUrl;
    	this.username = username;
    	this.password = password;
		this.messageValidator = messageValidator;
	}

    public IxnMessageConsumer setupConsumer() throws JMSException, NamingException {
		logger.debug("setting up consumer for onramp and producers for nwEx and dlqueue");
    	Source dlQueue = new BasicAuthSource(amqpUrl, DLQUEUE, username, password);
    	Source nwEx = new BasicAuthSource(amqpUrl, NWEXCHANGE, username, password);
		Sink onramp = new BasicAuthSink(amqpUrl, ONRAMP, username, password);
		IxnMessageConsumer consumer = new IxnMessageConsumer(onramp, nwEx, dlQueue, messageValidator);
		onramp.startWithMessageListener(consumer);
		onramp.setExceptionListener(consumer);
		dlQueue.start();
		dlQueue.setExceptionListener(consumer);
		nwEx.start();
		nwEx.setExceptionListener(consumer);
		return consumer;
    }

}
