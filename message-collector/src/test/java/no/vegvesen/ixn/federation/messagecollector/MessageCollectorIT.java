package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.docker.keygen.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorIT extends QpidDockerBaseIT {
	//TODO this is difficult to test the way it is now, since both containers is in effect sharing the same name and PKI
	//Try to make it so that we can test the internal/external messaging using custom networks
	private static final Logger logger = LoggerFactory.getLogger(MessageCollectorIT.class);

	private static final String PRODUCER_SP_NAME = "sp_producer";
	public static final String CONSUMER_SP_NAME = "sp_consumer";
	public static final String HOST_NAME = "localhost";
	static CaStores stores = generateStores(getTargetFolderPathForTestClass(MessageCollectorIT.class),"my_ca", HOST_NAME,PRODUCER_SP_NAME,CONSUMER_SP_NAME);

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer consumerContainer = getQpidTestContainer(
			Paths.get("docker","consumer"),
			stores,
			HOST_NAME,
			HOST_NAME
	);

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer producerContainer = getQpidTestContainer(
			Paths.get("docker","producer"),
			stores,
			HOST_NAME,
			HOST_NAME
	).withLogConsumer(new Slf4jLogConsumer(logger));



	public Sink createSink(String containerUrl, String queueName, CaStores stores, String spName) {
		return new Sink(
				containerUrl,
				queueName,
				sslClientContext(stores,spName)
		);

	}

	public Source createSource(String containerUrl, String queue, CaStores stores, String spName) {
		return new Source(
				containerUrl,
				queue,
				sslClientContext(stores,spName)
		);
	}

	@Test
	@Order(1)
	public void testMessagesCollected() throws NamingException, JMSException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort
		);

		MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		collector.runSchedule();
		assertThat(collector.getListeners()).hasSize(1);

        try (Source source = createSource(producerContainer.getAmqpsUrl(), HOST_NAME, stores, PRODUCER_SP_NAME)) {
            source.start();

            try (Sink sink = createSink(consumerContainer.getAmqpsUrl(), CONSUMER_SP_NAME, stores, CONSUMER_SP_NAME)) {
                source.sendNonPersistentMessage(source.createMessageBuilder()
						.textMessage("fishy fishy")
						.userId(HOST_NAME)
						.messageType(Constants.DATEX_2)
						.publicationType("Obstruction")
						.publisherName("publishername")
						.protocolVersion("DATEX2;2.3")
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

				Message message = sink.createConsumer().receive(2000);
				assertThat(message).withFailMessage("Expected message is not routed").isNotNull();
				assertThat(message.getJMSExpiration()).withFailMessage("Routed message has no expiry specified").isNotEqualTo(0L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        }

	}

	@Test
	@Order(2)
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		//TODO this should use different contexts for each side of the collector, see comment in CollectorCreator constructor
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores, HOST_NAME),
				HOST_NAME,
				localIxnFederationPort
		);

		MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		collector.runSchedule();
		assertThat(collector.getListeners()).hasSize(1);
		System.out.println(consumerContainer.getHttpUrl());

        try (Source source = createSource(producerContainer.getAmqpsUrl(),
				HOST_NAME,
                stores,
                PRODUCER_SP_NAME)) {
            source.start();
            JmsMessage message1 = source.createMessageBuilder()
                    .textMessage("fishy fishy")
                    .userId(HOST_NAME)
                    .messageType(Constants.DATEX_2)
                    .publisherId("Test")
                    .publicationId("pub-1")
                    .quadTreeTiles(",3232,")
                    .publicationType("Obstruction")
                    .publisherName("publishername")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build();

            source.sendNonPersistentMessage(message1,1000L);
			Thread.sleep(2000L); // wait for the message to expire with extra margin

            try (Sink sink = createSink(consumerContainer.getAmqpsUrl(), CONSUMER_SP_NAME, stores, CONSUMER_SP_NAME)) {
                Message message = sink.createConsumer().receive(1000L);
				assertThat(message).withFailMessage("Received message we expected to be expired").isNull();
			} catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


	}

	@Test
	@Order(3)
	public void testDatexMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort
		);

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

        try (Source source = createSource(producerContainer.getAmqpsUrl(), HOST_NAME, stores, PRODUCER_SP_NAME)) {
            source.start();
            JmsMessage senderMessage = source.createMessageBuilder()
                    .textMessage("Should work!")
                    .userId(HOST_NAME)
                    .messageType(Constants.DATEX_2)
                    .publisherId("Test")
                    .publicationId("pub-1")
                    .quadTreeTiles(",3232,")
                    .publicationType("Obstruction")
                    .publisherName("publishername")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build();
            source.sendNonPersistentMessage(senderMessage);

            try (Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer", stores, CONSUMER_SP_NAME)) {
                MessageConsumer consumer = sink.createConsumer();
				Message message = consumer.receive(1000);

				assertThat(message).isNotNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

	}

	@Test
	@Order(4)
	public void testDenmMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

        CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
                consumerContainer.getAmqpsPort().toString()
		);

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();

        try (Source source = createSource(producerContainer.getAmqpsUrl(), HOST_NAME, stores, PRODUCER_SP_NAME)) {
            source.start();
            String message = "Should work!";
            byte[] bytemessage = message.getBytes(StandardCharsets.UTF_8);
            JmsMessage senderMessage = source.createMessageBuilder()
                    .bytesMessage(bytemessage)
                    .userId(HOST_NAME)
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
            source.sendNonPersistentMessage(senderMessage);

            try (Sink sink = createSink(consumerContainer.getAmqpsUrl(), "sp_consumer", stores, CONSUMER_SP_NAME)) {
                MessageConsumer consumer = sink.createConsumer();
				Message receiveMessage = consumer.receive(1000);

				assertThat(receiveMessage).isNotNull();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

	}

}
