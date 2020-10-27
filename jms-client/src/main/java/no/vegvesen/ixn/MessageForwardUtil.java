package no.vegvesen.ixn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MessageForwardUtil {
	private static Logger logger = LoggerFactory.getLogger(MessageForwardUtil.class);
	public final static long DEFAULT_TTL_1_DAY = 86_400_000L;
	public final static long MAX_TTL_8_DAYS = 691_200_000L;

	public static long getRemainingTimeToLive(Message textMessage) {
		long expiration;
		try {
			expiration = textMessage.getJMSExpiration();
		} catch (JMSException e) {
			logger.warn("Could not get remaining ttl for message, returning default ttl {}", DEFAULT_TTL_1_DAY, e);
			return DEFAULT_TTL_1_DAY;
		}
		long currentTime = System.currentTimeMillis();
		if (expiration <= 0) {
			logger.debug("expiration {} is absent or illegal - setting to default ttl (1 day)", expiration);
			return DEFAULT_TTL_1_DAY;
		} else if (expiration > (MAX_TTL_8_DAYS + currentTime)) {
			logger.debug("expiration {} is too high, setting to maximum ttl (8 days)", expiration);
			return MAX_TTL_8_DAYS;
		} else {
			logger.debug("expiration {} is in the valid range (more than 0, less than 8 days)", expiration);
			return expiration - currentTime;
		}
	}

	public static void send(MessageProducer producer, Message message) throws JMSException {
		if (!isExpired(message)) {
			long remainingTimeToLive = getRemainingTimeToLive(message);
			logger.debug("Sending message with remaining time to live {} to {}", remainingTimeToLive, producer.getDestination());
			if (logger.isTraceEnabled()) {
				logger.trace("Message body {}", message.getBody(String.class));
			}
			producer.send(message, message.getJMSDeliveryMode(), Message.DEFAULT_PRIORITY, remainingTimeToLive);
			logger.debug("Sent message");
		}
	}

	private static boolean isExpired(Message message) throws JMSException {
		long jmsExpiration = message.getJMSExpiration();
		boolean expired = jmsExpiration != 0L && jmsExpiration < System.currentTimeMillis();
		if (expired) {
			logger.warn("Message expired, now: {}, expiry: {}", System.currentTimeMillis(), jmsExpiration);
		}
		return expired;
	}
}