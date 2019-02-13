package no.vegvesen.ixn.messaging;

import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@EnableJms
@Component
@Profile("63")
public class CountIxnMessageConsumer {
	private Map<String, List<TextMessage>> receivedMessages = new HashMap<>();

	@JmsListener(destination = "NO-out")
	public void receiveNoMessage(TextMessage textMessage) throws JMSException {
		recordReceivedMessage("NO-out", textMessage);
		debugMessage(textMessage);
	}

	@JmsListener(destination = "SE-out")
	public void receiveSeMessage(TextMessage textMessage) throws JMSException {
		recordReceivedMessage("SE-out", textMessage);
		debugMessage(textMessage);
	}

	@JmsListener(destination = "dlqueue")
	public void receiveDlqueueMessage(TextMessage textMessage) throws JMSException {
		recordReceivedMessage("dlqueue", textMessage);
		debugMessage(textMessage);
	}

	private void debugMessage(TextMessage textMessage) throws JMSException {
		System.out.println("timestamp:    " + textMessage.getJMSTimestamp());
		System.out.println("deliveryTime: " + textMessage.getJMSDeliveryTime());
		long now = System.currentTimeMillis();
		System.out.println("now         : " + now);
		System.out.println("latency : " + (now - textMessage.getJMSDeliveryTime()));
		System.out.println("where1: " + textMessage.getStringProperty("where1"));
		System.out.println("body: " + textMessage.getText());
	}

	private synchronized void recordReceivedMessage(String key, TextMessage textMessage) {
		List<TextMessage> textMessages = receivedMessages.get(key);
		System.out.println("received message to " + key);
		if (textMessages == null) {
			textMessages = new LinkedList<>();
		}
		textMessages.add(textMessage);
		receivedMessages.put(key, textMessages);
	}

	public synchronized void emptyQueue(String queue) {
		receivedMessages.put(queue, new LinkedList<>());
	}

	public int numberOfMessages(String queue) {
		List<TextMessage> receivedMessagesOfQueue = receivedMessages.get(queue);
		return receivedMessagesOfQueue == null ? 0 : receivedMessagesOfQueue.size();
	}
}
