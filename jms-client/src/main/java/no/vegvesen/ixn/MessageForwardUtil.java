package no.vegvesen.ixn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageProducer;

public class MessageForwardUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageForwardUtil.class);

	public static long getRemainingTimeToLive(Message textMessage) {
		long expiration;
		try {
			expiration = textMessage.getJMSExpiration();
		} catch (JMSException e) {
			return -1;
		}
		if (expiration == 0L) {
			return 0L;
		}
		long currentTime = System.currentTimeMillis();
		return expiration - currentTime;
	}

	public static void send(MessageProducer producer, Message message) throws JMSException {
		if (!isExpired(message)) {
			long remainingTimeToLive = getRemainingTimeToLive(message);
			if (remainingTimeToLive >= 0L) {
				producer.send(message, message.getJMSDeliveryMode(), Message.DEFAULT_PRIORITY, remainingTimeToLive);
			} else {
				logger.warn("Discarding message with remaining ttl {}", remainingTimeToLive);
			}

		} else {
			logger.warn("Discarding expired message");
		}
	}

	private static boolean isExpired(Message message) throws JMSException {
		long jmsExpiration = message.getJMSExpiration();
        return jmsExpiration != 0L && jmsExpiration < System.currentTimeMillis();
	}
}
