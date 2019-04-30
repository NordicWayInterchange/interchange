package no.vegvesen.ixn.messaging;

import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@EnableJms
@Component
public class TestIxnMessageConsumer {

	@JmsListener(destination = "test-out")
	public void receiveMessage(TextMessage textMessage) throws JMSException {
		System.out.println("timestamp:    " + textMessage.getJMSTimestamp());
		System.out.println("deliveryTime: " + textMessage.getJMSDeliveryTime());
		long now = System.currentTimeMillis();
		System.out.println("now         : " + now);
		System.out.println("latency : " + (now - textMessage.getJMSDeliveryTime()));
		System.out.println("where: " + textMessage.getStringProperty("where") );
	}

}
