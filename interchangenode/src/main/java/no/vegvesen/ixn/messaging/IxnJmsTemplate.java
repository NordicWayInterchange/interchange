package no.vegvesen.ixn.messaging;

import org.apache.qpid.jms.JmsQueue;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.JmsUtils;
import org.springframework.util.Assert;

import javax.jms.*;

@Configuration
public class IxnJmsTemplate extends JmsTemplate {

	public IxnJmsTemplate(ConnectionFactory connectionFactory) {
		super(connectionFactory);
		this.setExplicitQosEnabled(true);
	}


	public void send(final String destination,
					 final MessageCreator messageCreator, final long timeToLive)
			throws JmsException {
		execute(session -> {
			doSend(session, new JmsQueue(destination), messageCreator, timeToLive);
			return null;
		}, false);
	}

	/**
	 * In spring jms the message producer holds the time to live. This message creates one
	 */
	protected void doSend(Session session, Destination destination,
						  MessageCreator messageCreator, long timeToLive) throws JMSException {

		Assert.notNull(messageCreator, "MessageCreator must not be null");
		MessageProducer producer = createProducer(session, destination);
		try {
			Message message = messageCreator.createMessage(session);
			if (logger.isDebugEnabled()) {
				logger.debug("Sending created message: " + message);
			}
			doSend(producer, message, timeToLive);
			// Check commit - avoid commit call within a JTA transaction.
			if (session.getTransacted() && isSessionLocallyTransacted(session)) {
				// Transacted session created by this template -> commit.
				JmsUtils.commitIfNecessary(session);
			}
		} finally {
			JmsUtils.closeMessageProducer(producer);
		}
	}

	protected void doSend(MessageProducer producer, Message message,
						  long timeToLive) throws JMSException {
		if (isExplicitQosEnabled() && timeToLive > 0) {
			producer.send(message, getDeliveryMode(), getPriority(), timeToLive);
		} else {
			producer.send(message);
		}
	}
}
