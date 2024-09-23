package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
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
import javax.naming.NamingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
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
	public static final String HOST_NAME = getDockerHost();
	static CaStores stores = generateStores(getTargetFolderPathForTestClass(MessageCollectorIT.class),"my_ca", HOST_NAME,PRODUCER_SP_NAME,CONSUMER_SP_NAME);

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer consumerContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Paths.get("docker","consumer")
			);

	@Container
	//Container is not static and is not reused between tests
	public QpidContainer producerContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Paths.get("docker","producer")
			).withLogConsumer(new Slf4jLogConsumer(logger));


	public Source createSource(String containerUrl, String queue, CaStores stores, String spName) {
		return new Source(
				containerUrl,
				queue,
				sslClientContext(stores,spName)
		);
	}

	@Test
	public void testMessagesCollected() throws NamingException, JMSException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector collector = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		collector.runSchedule();
		assertThat(collector.getListeners()).hasSize(1);

        try (Source source = createSource(producerContainer.getAmqpsUrl(), HOST_NAME, stores, PRODUCER_SP_NAME)) {
            source.start();

			CountDownLatch latch = new CountDownLatch(1);
			AtomicBoolean jmsExpiration = new AtomicBoolean(true);
			try {
				try (Sink sink = new Sink(
                        consumerContainer.getAmqpsUrl(),
						CONSUMER_SP_NAME,
						sslClientContext(stores, CONSUMER_SP_NAME),
						message -> {
							latch.countDown();
                            try {
                                if (message.getJMSExpiration() == 0) {
                                    jmsExpiration.set(false);
                                }
                            } catch (JMSException e) {
                                throw new RuntimeException(e);
                            }
                        }
				)) {
					sink.start();
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

                    assertThat(latch.await(2, TimeUnit.SECONDS)).withFailMessage("Message did not arrive within threshold").isTrue();
					assertThat(jmsExpiration).withFailMessage("Routed message has no expiry specified").isTrue();
				}
			} catch (Exception e) {
                throw new RuntimeException(e);
            }


        }

	}

	@Test
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {

		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		//TODO this should use different contexts for each side of the collector, see comment in CollectorCreator constructor
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort,
				"subscriptionExchange");

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

			CountDownLatch latch = new CountDownLatch(1);
            try {
				String containerUrl = consumerContainer.getAmqpsUrl();

				try (Sink sink = new Sink(
						containerUrl,
						CONSUMER_SP_NAME,
						sslClientContext(stores, CONSUMER_SP_NAME),
						message -> latch.countDown()
				)) {
					sink.start();
					assertThat(latch.await(1,TimeUnit.SECONDS)).withFailMessage("Received message we expected to be expired").isFalse();
				}
			} catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


	}

	@Test
	public void testDatexMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort,
				"subscriptionExchange");

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

            try {
				CountDownLatch latch = new CountDownLatch(1);
				String containerUrl = consumerContainer.getAmqpsUrl();

				try (Sink sink = new Sink(
						containerUrl,
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						message -> latch.countDown()
				)) {
					sink.start();
					assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

	}

	@Test
	public void testDenmMessagesWithMessageCollector() throws NamingException, JMSException {
		GracefulBackoffProperties backoffProperties = new GracefulBackoffProperties();
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		ListenerEndpointRepository listenerEndpointRepository = mock(ListenerEndpointRepository.class);
		when(listenerEndpointRepository.findAll()).thenReturn(List.of(listenerEndpoint));

		String localIxnFederationPort = consumerContainer.getAmqpsPort().toString();
		CollectorCreator collectorCreator = new CollectorCreator(
				sslServerContext(stores,HOST_NAME),
				HOST_NAME,
				localIxnFederationPort,
				"subscriptionExchange");

		MessageCollector forwarder = new MessageCollector(listenerEndpointRepository, collectorCreator, backoffProperties);
		forwarder.runSchedule();
		System.out.printf("Producer URL: %s%n",producerContainer.getHttpUrl());
		System.out.printf("Consumer URL: %s%n",consumerContainer.getHttpUrl());
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
			try {

				CountDownLatch latch = new CountDownLatch(1);
				try (Sink sink = new Sink(
                        consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						message1 -> latch.countDown()

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
                throw new RuntimeException(e);
            }

        }

	}

}
