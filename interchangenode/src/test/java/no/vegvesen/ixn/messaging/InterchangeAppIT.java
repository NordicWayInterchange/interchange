package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InterchangeAppIT {

	@Autowired
	TestIxnMessageConsumer consumer;

	@Autowired
	IxnMessageProducer producer;


	@BeforeClass
	public static void setUp() {
		TestKeystoreHelper.useTestKeystore();
	}

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