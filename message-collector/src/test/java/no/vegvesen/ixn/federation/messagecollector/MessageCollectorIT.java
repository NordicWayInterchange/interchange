package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.apache.qpid.jms.message.JmsBytesMessage;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorIT extends QpidDockerBaseIT {

	private static final String PRODUCER_SP_NAME = "sp_producer";
	public static final String CONSUMER_SP_NAME = "sp_consumer";
	static KeysStructure keysStructure = generateKeys(MessageCollectorIT.class,"my_ca", "localhost", PRODUCER_SP_NAME, CONSUMER_SP_NAME);

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer consumerContainer = getQpidTestContainer("docker/consumer",
			keysStructure,
			"localhost");

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer producerContainer = getQpidTestContainer("docker/producer",
			keysStructure,
			"localhost");



	public Sink createSink(String containerUrl, String queueName, KeysStructure keysStructure, String spName) {
		return new Sink(
				containerUrl,
				queueName,
				sslClientContext(keysStructure,spName)
		);

	}

	public Source createSource(String containerUrl, String queue, KeysStructure keysStructure, String spName) {
		return new Source(
				containerUrl,
				queue,
				sslClientContext(keysStructure,spName)
		);
	}

	@Test
	@Order(1)
	public void testMessagesCollected() throws NamingException, JMSException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(keysStructure),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(),"localhost",keysStructure,PRODUCER_SP_NAME);
		source.start();

		Sink sink = createSink(consumerContainer.getAmqpsUrl(), CONSUMER_SP_NAME,keysStructure,CONSUMER_SP_NAME);
		MessageConsumer consumer = sink.createConsumer();

		source.sendNonPersistentMessage(source.createMessageBuilder()
				.textMessage("fishy fishy")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion( "DATEX2;2.3")
				.publisherId("SE-123")
				.publicationId("pub-1")
				.quadTreeTiles(",232,")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.shardId(1)
				.shardCount(1)
				.timestamp(System.currentTimeMillis())
				.build(), 8000L);

		Message message = consumer.receive(2000);
		assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
		assertThat(message.getJMSExpiration()).withFailMessage("Routed message has noe expiry specified").isNotEqualTo(0L);
	}

	@Test
	@Order(2)
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", 5671, new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(keysStructure),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(),
				"localhost",
				keysStructure,
				PRODUCER_SP_NAME);
		source.start();
		JmsMessage message1 = source.createMessageBuilder()
				.textMessage("fishy fishy")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publisherId("Test")
				.publicationId("pub-1")
				.quadTreeTiles(",3232,")
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.shardId(1)
				.shardCount(1)
				.timestamp(System.currentTimeMillis())
				.build();
		JmsMessage senderMessage = message1;
		source.sendNonPersistentMessage(senderMessage);

		Thread.sleep(2000); // wait for the message to expire with extra margin

		Sink sink = createSink(consumerContainer.getAmqpsUrl(),CONSUMER_SP_NAME,keysStructure,CONSUMER_SP_NAME);
		MessageConsumer consumer = sink.createConsumer();
		Message message = consumer.receive(1000);
		assertThat(message).withFailMessage("Expected message is not routed").isNull();
	}

	@Test
	@Order(3)
	@Disabled
	public void testDatexMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", 5671, new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(keysStructure),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(), "localhost",keysStructure, "sp_producer.p12");
		source.start();
		JmsMessage message1 = source.createMessageBuilder()
				.textMessage("Should work!")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publisherId("Test")
				.publicationId("pub-1")
				.quadTreeTiles(",3232,")
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.shardId(1)
				.shardCount(1)
				.timestamp(System.currentTimeMillis())
				.build();
		JmsMessage senderMessage = message1;
		source.sendNonPersistentMessage(senderMessage);

		Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer",keysStructure, "sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		Message message = consumer.receive(1000);

		assertThat(message).isNotNull();
	}

	@Test
	@Order(4)
	@Disabled
	public void testDenmMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint("localhost", "localhost", "localhost", 5671, new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(Arrays.asList(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(keysStructure),
				"localhost",
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

		Source source = createSource(producerContainer.getAmqpsUrl(), "localhost",keysStructure, "sp_producer.p12");
		source.start();
		String message = "Should work!";
		byte[] bytemessage = message.getBytes(StandardCharsets.UTF_8);
		JmsMessage message1 = source.createMessageBuilder()
				.bytesMessage(bytemessage)
				.userId("localhost")
				.messageType(Constants.DENM)
				.publisherId("Test")
				.publicationId("pub-1")
				.quadTreeTiles(",3232,")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.causeCode(1)
				.subCauseCode(1)
				.shardId(1)
				.shardCount(1)
				.timestamp(System.currentTimeMillis())
				.build();
		JmsMessage senderMessage = message1;
		source.send(senderMessage);

		Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer", keysStructure,"sp_consumer.p12");
		MessageConsumer consumer = sink.createConsumer();

		Message receiveMessage = consumer.receive(1000);

		assertThat(receiveMessage).isNotNull();
	}

}
