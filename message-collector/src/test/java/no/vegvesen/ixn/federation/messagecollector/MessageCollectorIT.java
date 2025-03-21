package no.vegvesen.ixn.federation.messagecollector;

import jakarta.jms.*;
import no.vegvesen.ixn.*;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.Connection;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;

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
			).withLogConsumer(new Slf4jLogConsumer(logger));

	@Container
	public QpidContainer producerContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Paths.get("docker","producer")
			);

	@Test
	public void testMessagesCollected() throws NamingException, JMSException {
        List<ListenerEndpoint> listenerEndpoints = List.of(
				new ListenerEndpoint(HOST_NAME,
						HOST_NAME,
						HOST_NAME,
						producerContainer.getAmqpsPort(),
						new Connection(),
                        "subscriptionExchange"
				)
		);

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);

        PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext,
                        e1 -> logger.error("Caught exception", e1)
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool,consumerContainer.getAmqpsUrl());
		newMessageCollector.syncListeners(listenerEndpoints);
		String containerUrl = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl,
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();

			CountDownLatch latch = new CountDownLatch(1);
			AtomicBoolean jmsExpiration = new AtomicBoolean(true);
			MessageListener listener = message -> {
				latch.countDown();
				try {
					if (message.getJMSExpiration() == 0) {
						jmsExpiration.set(false);
					}
				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
			};
			try {
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						CONSUMER_SP_NAME,
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener
				)) {
					sink.start();
					String messageText = "fishy fishy";
					JmsMessage message = createTextMessage(source, messageText);
					source.sendNonPersistentMessage(message, 8000L);

					assertThat(latch.await(2, TimeUnit.SECONDS)).withFailMessage("Message did not arrive within threshold").isTrue();
					assertThat(jmsExpiration).withFailMessage("Routed message has no expiry specified").isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		connectionPool.close();
	}


	@Test
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		SSLContext senderContext = sslServerContext(stores, HOST_NAME);

		NewMessageCollector collector = new NewMessageCollector(
				senderContext,
				new PoolingConnectionCreator(new ExceptionListeningConnectionCreator(
						senderContext,
						e -> System.out.println("Caught exception: " + e)
				)),
				consumerContainer.getAmqpsUrl()
		);
		collector.syncListeners(List.of(listenerEndpoint));

		String containerUrl1 = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl1,
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();

			//create and send 2 messages
			String messageText = "fishy fishy";
			JmsMessage message1 = createTextMessage(source, messageText);
			long shortTtl = 1000L;
			long longTtl = 10000L;
			long longEnoughForTheFirstMessageToExpire = 2000L;
			source.sendNonPersistentMessage(message1, shortTtl);
			source.sendNonPersistentMessage(message1, longTtl);
			Thread.sleep(longEnoughForTheFirstMessageToExpire); // wait for the first message to expire with extra margin, while the second is still alive

			CountDownLatch latch = new CountDownLatch(2);
			MessageListener listener = message -> latch.countDown();
			try {
				String containerUrl = consumerContainer.getAmqpsUrl();

				try (Sink sink = new Sink(
						containerUrl,
						CONSUMER_SP_NAME,
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener
				)) {
					sink.start();
					boolean gotAllMessages = latch.await(1, TimeUnit.SECONDS);
					assertThat(gotAllMessages).withFailMessage("Received message we expected to be expired").isFalse();
					//One message arrived, so latch should have a count of 1
					assertThat(latch.getCount()).isEqualTo(1);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void testDatexMessagesWithMessageCollector() throws Exception {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		String writeUrl = consumerContainer.getAmqpsUrl();
		NewMessageCollector newMessageCollector = new NewMessageCollector(
				senderContext,
				new PoolingConnectionCreator(
						new ExceptionListeningConnectionCreator(
								senderContext, exceptionListener
						)
				),
				writeUrl
		);
		newMessageCollector.syncListeners(List.of(listenerEndpoint));

        try (Source source = new Source(
                producerContainer.getAmqpsUrl(),
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String messageText = "Should work!";
			JmsMessage senderMessage = createTextMessage(source, messageText);
			source.sendNonPersistentMessage(senderMessage);

			CountDownLatch latch = new CountDownLatch(1);
			MessageListener listener = message -> latch.countDown();

			try (Sink sink = new Sink(
                    writeUrl,
					"sp_consumer",
					sslClientContext(stores, CONSUMER_SP_NAME),
					listener
			)) {
				sink.start();
				assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
			}
		}
	}

	@Test
	public void testDenmMessagesWithMessageCollector() throws NamingException, JMSException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		SSLContext senderContext = sslServerContext(stores, HOST_NAME);

		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext,
						exceptionListener
				)
		);
		String writeUrl = consumerContainer.getAmqpsUrl();
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool, writeUrl);
		newMessageCollector.syncListeners(List.of(listenerEndpoint));
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);
		System.out.printf("Producer URL: %s%n",producerContainer.getHttpUrl());
		System.out.printf("Consumer URL: %s%n",consumerContainer.getHttpUrl());
		String containerUrl = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl,
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

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

	@Test
	public void testAddingConnectionFromEmptyState() throws NamingException, JMSException {
		String writeExchange = "subscriptionExchange";
		List<ListenerEndpoint> listenerEndpoints = List.of(
				new ListenerEndpoint(HOST_NAME,
						HOST_NAME,
						HOST_NAME,
						producerContainer.getAmqpsPort(),
						new Connection(),
						writeExchange
				)
		);

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);


		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext, exceptionListener
				)
		);

		List<ListenerEndpoint> emptyEndpoins = List.of();
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool,consumerContainer.getAmqpsUrl());
		newMessageCollector.syncListeners(emptyEndpoins);
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(0);
		newMessageCollector.syncListeners(listenerEndpoints);
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);

		System.out.printf("Producer URL: %s%n",producerContainer.getHttpUrl());
		System.out.printf("Consumer URL: %s%n",consumerContainer.getHttpUrl());
		String containerUrl = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl,
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			CountDownLatch latch = new CountDownLatch(1);
			MessageListener listener = message1 -> latch.countDown();
			try {
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

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

	@Test
	public void testRemovingConnectionFromAListOfOne() throws NamingException, JMSException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext, exceptionListener
				)
		);
		String writeUrl1 = consumerContainer.getAmqpsUrl();
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool,writeUrl1);
		newMessageCollector.syncListeners(List.of(listenerEndpoint));
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);

		System.out.printf("Producer URL: %s%n",producerContainer.getHttpUrl());
		System.out.printf("Consumer URL: %s%n",consumerContainer.getHttpUrl());
		String containerUrl = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl,
				HOST_NAME,
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		newMessageCollector.syncListeners(List.of());
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(0);
	}


	@Test
	public void removeConnectionFromAListOfTwo() throws NamingException, JMSException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME,"localhost", HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint(HOST_NAME, "localhost1", HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext, exceptionListener
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool,consumerContainer.getAmqpsUrl());
        newMessageCollector.syncListeners(List.of(listenerEndpoint,listenerEndpoint2));
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(2);

		System.out.printf("Producer URL: %s%n",producerContainer.getHttpUrl());
		System.out.printf("Consumer URL: %s%n",consumerContainer.getHttpUrl());
		String containerUrl = producerContainer.getAmqpsUrl();
		try (Source source = new Source(
				containerUrl,
				"localhost",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		try (Source source = new Source(
				containerUrl,
				"localhost1",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		newMessageCollector.syncListeners(List.of(listenerEndpoint));
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);
		try (Source source = new Source(
				containerUrl,
				"localhost1",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();

			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void addListenerToAListOfOne() throws Exception {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME,"localhost", HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		PoolingConnectionCreator connectionPool = new PoolingConnectionCreator(
				new ExceptionListeningConnectionCreator(
						senderContext, exceptionListener
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool,consumerContainer.getAmqpsUrl());
		newMessageCollector.syncListeners(List.of(listenerEndpoint));
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);
		try (Source source = new Source(
				producerContainer.getAmqpsUrl(),
				"localhost",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		ListenerEndpoint listenerEndpoint2 = new ListenerEndpoint(HOST_NAME, "localhost1", HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		newMessageCollector.syncListeners(List.of(listenerEndpoint,listenerEndpoint2));
		try (Source source = new Source(
				producerContainer.getAmqpsUrl(),
				"localhost",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);

			CountDownLatch latch = new CountDownLatch(1);
			MessageListener listener = message1 -> latch.countDown();
			try (Sink sink = new Sink(
					consumerContainer.getAmqpsUrl(),
					"sp_consumer",
					sslClientContext(stores, CONSUMER_SP_NAME),
					listener

			)) {
				sink.start();
				source.sendNonPersistentMessage(senderMessage);
				assertThat(latch.await(1,TimeUnit.SECONDS)).isTrue();
			}
		}
		try (Source source = new Source(
				producerContainer.getAmqpsUrl(),
				"localhost1",
				sslClientContext(stores, PRODUCER_SP_NAME)
		)) {
			source.start();
			String message = "Should work!";
			JmsMessage senderMessage = createBinaryMessage(message, source);
			try {

				CountDownLatch latch = new CountDownLatch(1);
				MessageListener listener = message1 -> latch.countDown();
				try (Sink sink = new Sink(
						consumerContainer.getAmqpsUrl(),
						"sp_consumer",
						sslClientContext(stores, CONSUMER_SP_NAME),
						listener

				)) {
					sink.start();
					source.sendNonPersistentMessage(senderMessage);
					assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}


	private static JmsMessage createTextMessage(Source source, String messageText) throws JMSException {
		return source.createMessageBuilder()
				.textMessage(messageText)
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
				.build();
	}

	private static JmsMessage createBinaryMessage(String message, Source source) throws JMSException {
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
		return senderMessage;
	}
}
