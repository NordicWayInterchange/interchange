package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.model.IxnMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

/**
 * Verifies that it is possible to send messages to the amqp-server.
 * It reuses the spring wiring of jms resources from the interchange app to send and receive messages in the tests.
 * This test is run with profile "62" and uses ports in the 62...-series.
 * The amqp-url, username and password is specified in the application-62.properties.
 *
 * @See AccessControlIT uses separate user client connections.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("62")
public class InterchangeAppIT {

	@Autowired
	TestIxnMessageConsumer consumer;

	@Autowired
	IxnMessageProducer producer;

	@Test
	public void sendValidMessage() {
		long currentTimeMillis = System.currentTimeMillis();
		long expiration = currentTimeMillis + 1000;
		IxnMessage message = new IxnMessage(
				"The great traffic testers",
				"quest",
				expiration,
				63.0f,
				10.0f,
				Collections.singletonList("traffic jam"),
				"jam, jam - spam, spam");
		message.setCountries(Collections.singletonList("no"));
		producer.sendMessage("onramp", message);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("Ventet 2 sek");
		}
		producer.sendMessage("onramp", message);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.out.println("Ventet 2 sek");
		}
	}

	@Test
	public void sendInvalidMessage() {
		long currentTimeMillis = System.currentTimeMillis();
		long expiration = currentTimeMillis + 1000;
		IxnMessage message = new IxnMessage(
				"The great traffic testers",
				"quest",
				expiration,
				63.0f,
				10.0f,
				Collections.singletonList("traffic jam"),
				null);
		message.setCountries(Collections.singletonList("no"));
		producer.sendMessage("onramp", message);
	}

}