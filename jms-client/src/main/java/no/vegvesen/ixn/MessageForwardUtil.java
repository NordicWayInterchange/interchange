package no.vegvesen.ixn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MessageForwardUtil {
	private static Logger logger = LoggerFactory.getLogger(MessageForwardUtil.class);
	public final static long DEFAULT_TTL = 86_400_000L;
	public final static long MAX_TTL = 6_911_200_000L;

	public static long getRemainingTimeToLive(Message textMessage) {
		long expiration;
		try {
			expiration = textMessage.getJMSExpiration();
		} catch (JMSException e) {
			return DEFAULT_TTL;
		}
		long currentTime = System.currentTimeMillis();
		if (expiration <= 0) {
			// expiration is absent or illegal - setting to default ttl (1 day)
			return DEFAULT_TTL;
		} else if (expiration > (MAX_TTL + currentTime)) {
			// expiration is too high, setting to maximum ttl (8 days)
			return MAX_TTL;
		} else {
			// expiration is in the valid range (more than 0, less than 8 days)
			return expiration - currentTime;
		}
	}

	public static void send(MessageProducer producer, Message message) throws JMSException {
		if (!isExpired(message)) {
			long remainingTimeToLive = getRemainingTimeToLive(message);
			logger.debug("Sending message with remaining time to live {} to {}", remainingTimeToLive, producer.getDestination());
			producer.send(message, DeliveryMode.PERSISTENT, Message.DEFAULT_PRIORITY, remainingTimeToLive);
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