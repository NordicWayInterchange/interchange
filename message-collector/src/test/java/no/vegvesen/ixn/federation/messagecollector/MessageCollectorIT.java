package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorIT extends QpidDockerBaseIT {

	static Path testKeysPath = getFolderPath("message-collector/target/test-keys");

	@SuppressWarnings("rawtypes")
	@Container
	//Container is not static and is not reused between tests
	public GenericContainer consumerContainer = getQpidContainer("docker/consumer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password");

	@SuppressWarnings("rawtypes")
	@Container
	//Container is not static and is not reused between tests
	public GenericContainer producerContainer = getQpidContainer("docker/producer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks", "password");

	public Sink createSink(Integer containerPort, String queueName, String keyStore) {
		return new Sink("amqps://localhost:" + containerPort,
				queueName,
				TestKeystoreHelper.sslContext(testKeysPath, keyStore, "truststore.jks"));
	}

	public Source createSource(Integer containerPort, String queue, String keystore) {
		return new Source("amqps://localhost:" + containerPort,
				queue,
				TestKeystoreHelper.sslContext(testKeysPath, keystore, "truststore.jks"));
	}

	@Test
	@Order(1)
	public void testMessagesCollected() throws NamingException, JMSException {
		Integer producerPort = producerContainer.getMappedPort(AMQPS_PORT);

		Neighbour neighbour = new Neighbour();
		neighbour.setName("localhost");
		neighbour.setMessageChannelPort(producerPort.toString());

		NeighbourService neighbourService = mock(NeighbourService.class);
		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Lists.list(neighbour));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"fedEx");
		MessageCollector forwarder = new MessageCollector(neighbourService, collectorCreator);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		source.send("fishy fishy", "SE", 8000L);

		Message message = consumer.receive(2000);
		assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
		assertThat(message.getJMSExpiration()).withFailMessage("Routed message has noe expiry specified").isNotEqualTo(0L);
	}

	@Test
	@Order(2)
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {
		Integer producerPort = producerContainer.getMappedPort(AMQPS_PORT);

		Neighbour neighbour = new Neighbour();
		neighbour.setName("localhost");
		neighbour.setMessageChannelPort(producerPort.toString());

		NeighbourService neighbourService = mock(NeighbourService.class);
		when(neighbourService.listNeighboursToConsumeMessagesFrom()).thenReturn(Lists.list(neighbour));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"fedEx");
		MessageCollector forwarder = new MessageCollector(neighbourService, collectorCreator);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();
		source.send("fishy fishy", "SE", 1000L);

		Thread.sleep(2000); // wait for the message to expire with extra margin

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();
		Message message = consumer.receive(1000);
		assertThat(message).withFailMessage("Expected message is not routed").isNull();
	}

}
