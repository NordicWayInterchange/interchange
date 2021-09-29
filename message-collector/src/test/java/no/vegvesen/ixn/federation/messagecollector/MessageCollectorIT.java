package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.GracefulBackoffProperties;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.apache.qpid.jms.message.JmsMessage;
import org.apache.qpid.jms.message.JmsTextMessage;
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
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorIT extends QpidDockerBaseIT {
	static Path testKeysPath = generateKeys(MessageCollectorIT.class,"my_ca", "localhost", "sp_producer", "sp_consumer");

	@SuppressWarnings("rawtypes")
	@Container
	//Container is not static and is not reused between tests
	public GenericContainer consumerContainer = getQpidTestContainer("docker/consumer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

	@SuppressWarnings("rawtypes")
	@Container
	//Container is not static and is not reused between tests
	public GenericContainer producerContainer = getQpidTestContainer("docker/producer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

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

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "amqps://localhost:" + producerPort.toString(), "localhost", new Connection());

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"incomingExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		JmsMessage senderMessage = source.getTextMessage("fishy fishy", "SE");
		source.sendNonPersistentMessage(senderMessage, 8000L);

		Message message = consumer.receive(2000);
		assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
		assertThat(message.getJMSExpiration()).withFailMessage("Routed message has noe expiry specified").isNotEqualTo(0L);
	}

	@Test
	@Order(2)
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {
		Integer producerPort = producerContainer.getMappedPort(AMQPS_PORT);

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "amqps://localhost:" + producerPort.toString(), "localhost", new Connection());

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"incomingExchange");
		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();
		JmsMessage senderMessage = source.getJmsTextMessage("fishy fishy", "SE");
		source.sendNonPersistentMessage(senderMessage, 1000L);

		Thread.sleep(2000); // wait for the message to expire with extra margin

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();
		Message message = consumer.receive(1000);
		assertThat(message).withFailMessage("Expected message is not routed").isNull();
	}

}
