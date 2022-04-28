package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsMessage;
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
import static org.mockito.ArgumentMatchers.any;
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
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", producerPort, new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		MatchRepository matchRepository = mock(MatchRepository.class);
		MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
		when(matchDiscoveryService.findMatchesByExchangeName(any(String.class))).thenReturn(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		source.sendNonPersistentMessage(source.createMessageBuilder()
				.textMessage("fishy fishy")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion( "DATEX2;2.3")
				.publisherId("SE-123")
				.quadTreeTiles(",232,")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.timestamp(System.currentTimeMillis())
				.build(), 8000L);

		Message message = consumer.receive(2000);
		assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
		assertThat(message.getJMSExpiration()).withFailMessage("Routed message has noe expiry specified").isNotEqualTo(0L);
	}

	@Test
	@Order(2)
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {
		Integer producerPort = producerContainer.getMappedPort(AMQPS_PORT);

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", 5671, new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		MatchRepository matchRepository = mock(MatchRepository.class);
		MatchDiscoveryService matchDiscoveryService = new MatchDiscoveryService(matchRepository);
		when(matchDiscoveryService.findMatchesByExchangeName(any(String.class))).thenReturn(new Match(new LocalSubscription(), new Subscription(), MatchStatus.SETUP_ENDPOINT));

		String localIxnFederationPort = consumerContainer.getMappedPort(AMQPS_PORT).toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				TestKeystoreHelper.sslContext(testKeysPath,"localhost.p12", "truststore.jks"),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties, matchDiscoveryService);
		forwarder.runSchedule();

		Source source = createSource(producerPort, "localhost", "sp_producer.p12");
		source.start();
		JmsMessage message1 = source.createMessageBuilder()
				.textMessage("fishy fishy")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publisherId("Test")
				.quadTreeTiles(",3232,")
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.timestamp(System.currentTimeMillis())
				.build();
		JmsMessage senderMessage = message1;
		source.sendNonPersistentMessage(senderMessage, 1000L);

		Thread.sleep(2000); // wait for the message to expire with extra margin

		Sink sink = createSink(consumerContainer.getMappedPort(AMQPS_PORT), "sp_consumer", "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();
		Message message = consumer.receive(1000);
		assertThat(message).withFailMessage("Expected message is not routed").isNull();
	}

}
