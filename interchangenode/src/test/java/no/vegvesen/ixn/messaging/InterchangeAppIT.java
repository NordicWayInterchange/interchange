package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("62")
public class InterchangeAppIT {

	@Autowired
	TestIxnMessageConsumer consumer;

	@Autowired
	IxnMessageProducer producer;

	private static final String USER_KEYSTORE = "jks/king_harald.p12";
	private static final String TRUSTSTORE = "jks/truststore.jks";


	@BeforeClass
	public static void setUp() {
		TestKeystoreHelper.useTestKeystore(USER_KEYSTORE, TRUSTSTORE);
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