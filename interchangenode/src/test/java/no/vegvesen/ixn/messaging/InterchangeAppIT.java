package no.vegvesen.ixn.messaging;

import no.vegvesen.ixn.federation.forwarding.DockerBaseIT;
import no.vegvesen.ixn.model.IxnMessage;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;

import java.util.Collections;

/**
 * Verifies that it is possible to send messages to the amqp-server.
 * It reuses the spring wiring of jms resources from the interchange app to send and receive messages in the tests.
 *
 * @see no.vegvesen.ixn.AccessControlIT uses separate user client connections.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {InterchangeAppIT.Initializer.class})
public class InterchangeAppIT extends DockerBaseIT {

	@ClassRule
	public static GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	@ClassRule
	public static GenericContainer postgisContainer = getPostgisContainer("interchangenode/src/test/docker/postgis");

	static class Initializer
			implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
			TestPropertyValues.of(
					"amqphub.amqp10jms.remote-url=amqp://localhost:" + localContainer.getMappedPort(AMQP_PORT),
					"amqphub.amqp10jms.username=interchange",
					"amqphub.amqp10jms.password=12345678",
					"spring.datasource.url: jdbc:postgresql://localhost:" + postgisContainer.getMappedPort(JDBC_PORT) + "/geolookup",
					"spring.datasource.username: geolookup",
					"spring.datasource.password: geolookup",
					"spring.datasource.driver-class-name: org.postgresql.Driver"
			).applyTo(configurableApplicationContext.getEnvironment());
		}
	}

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