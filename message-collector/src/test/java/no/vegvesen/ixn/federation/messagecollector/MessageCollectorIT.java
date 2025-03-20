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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	public Source createSource(String containerUrl, String queue, CaStores stores, String spName) {
		return new Source(
				containerUrl,
				queue,
				sslClientContext(stores,spName)
		);
	}

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
		NewSink readSink = new NewSink(senderContext);

		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		SinkConnectionPool connectionPool = new SinkConnectionPool(
				new ExceptionListeningConnectionCreator(
						readSink,
						exceptionListener
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool);
		newMessageCollector.syncListeners(listenerEndpoints, consumerContainer.getAmqpsUrl());
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
		connectionPool.close();
	}


	@Test
	public void testExpiredMessagesNotCollected() throws NamingException, JMSException, InterruptedException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		NewSink readSink = new NewSink(senderContext);

		NewMessageCollector collector = new NewMessageCollector(
				senderContext,
				new SinkConnectionPool(new ExceptionListeningConnectionCreator(
						readSink,
						e -> System.out.println("Caught exception: " + e)
				))
		);
		collector.syncListeners(List.of(listenerEndpoint), consumerContainer.getAmqpsUrl());

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
			source.sendNonPersistentMessage(message1,10000L);
			Thread.sleep(2000L); // wait for the message to expire with extra margin

			CountDownLatch latch = new CountDownLatch(2);
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
					assertThat(latch.getCount()).isEqualTo(1);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Test
	public void testDatexMessagesWithMessageCollector() throws NamingException, JMSException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		NewSink readSink = new NewSink(senderContext);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		NewMessageCollector newMessageCollector = new NewMessageCollector(
				senderContext,
				new SinkConnectionPool(
						new ExceptionListeningConnectionCreator(
								readSink,
								exceptionListener
						)
				)
		);
		String writeUrl = consumerContainer.getAmqpsUrl();
		newMessageCollector.syncListeners(List.of(listenerEndpoint),writeUrl);

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
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");
		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		NewSink readSink = new NewSink(senderContext);

		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		SinkConnectionPool connectionPool = new SinkConnectionPool(
				new ExceptionListeningConnectionCreator(
						readSink,
						exceptionListener
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool);
		String writeUrl = consumerContainer.getAmqpsUrl();
		newMessageCollector.syncListeners(List.of(listenerEndpoint),writeUrl);
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);
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
		NewSink readSink = new NewSink(senderContext);


		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		SinkConnectionPool connectionPool = new SinkConnectionPool(
				new ExceptionListeningConnectionCreator(
						readSink,
						exceptionListener
				)
		);
		List<ListenerEndpoint> emptyEndpoins = List.of();
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool);
		newMessageCollector.syncListeners(emptyEndpoins, consumerContainer.getAmqpsUrl());
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(0);
		newMessageCollector.syncListeners(listenerEndpoints, consumerContainer.getAmqpsUrl());
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);

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

	@Test
	public void testRemovingConnectionFromAListOfOne() throws NamingException, JMSException {
		ListenerEndpoint listenerEndpoint = new ListenerEndpoint(HOST_NAME, HOST_NAME, HOST_NAME, producerContainer.getAmqpsPort(), new Connection(), "subscriptionExchange");

		SSLContext senderContext = sslServerContext(stores, HOST_NAME);
		NewSink readSink = new NewSink(senderContext);
		ExceptionListener exceptionListener = e -> logger.error("Caught exception", e);
		SinkConnectionPool connectionPool = new SinkConnectionPool(
				new ExceptionListeningConnectionCreator(
						readSink,
						exceptionListener
				)
		);
		NewMessageCollector newMessageCollector = new NewMessageCollector(senderContext, connectionPool);
		String writeUrl1 = consumerContainer.getAmqpsUrl();
		newMessageCollector.syncListeners(List.of(listenerEndpoint), writeUrl1);
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(1);

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
		String writeUrl = consumerContainer.getAmqpsUrl();
		newMessageCollector.syncListeners(List.of(),writeUrl);
		assertThat(newMessageCollector.numberOfListeners()).isEqualTo(0);
	}

	public static class MessageForwarder implements Runnable {
		private final String writeUrl;
		private final String writeExchange;
		private final SSLContext writeContext;
		private final SinkConnectionPool connectionPool;
		private final String readUrl;
		private final String readSource;
		private final AtomicBoolean running;

		public MessageForwarder(
				String writeUrl,
				String writeExchange,
				SSLContext writeContext,
				SinkConnectionPool connectionPool,
				String readUrl,
				String readSource
		) {
			this.writeUrl = writeUrl;
			this.writeExchange = writeExchange;
			this.writeContext = writeContext;
			this.connectionPool = connectionPool;
			this.readUrl = readUrl;
			this.readSource = readSource;
			this.running = new AtomicBoolean(false);
		}

		@Override
		public void run() {
			running.set(true);
			//Broker "consumer"
			try (Source writeSource = new Source(writeUrl, writeExchange, writeContext)) {
				writeSource.start();
				try (jakarta.jms.Connection readConnection = connectionPool.createConnection(readUrl)) {
					logger.info("Connected to url {}", writeUrl);
					try (Session session = readConnection.createSession(Session.AUTO_ACKNOWLEDGE)) {
						Destination destination = session.createQueue(readSource);
						try (MessageConsumer consumer = session.createConsumer(destination)) {
							logger.info("Subscribed to destination {}", destination);
							while (running.get()) {
								try {
									Message message = consumer.receive(500); //Listener schedule, might be changed...
									if (message != null) {
										MessageForwardUtil.send(writeSource.getProducer(), message);
										logger.info("Message {} received from queue {}", message.getJMSMessageID(), readSource);
									}
								} catch (JMSException e) {
									running.set(false);
								}
							}
							logger.info("Exiting collector thread");
						}
					}
				} catch (JMSException e) {
					throw new RuntimeException(e);
				}
			} catch (NamingException | JMSException e) {
				throw new RuntimeException(e);
			}
		}

		public void stop() {
			running.set(false);
		}
	}

    public static final class NewMessageCollector {
        private final Map<ListenerEndpoint, MessageForwarder> states;
        private final SSLContext senderContext;
        private final SinkConnectionPool connectionPool;
        private final ExecutorService executorService;

		public NewMessageCollector(SSLContext senderContext, SinkConnectionPool connectionPool) {
			this.senderContext = senderContext;
			this.states = new HashMap<>();
			this.connectionPool = connectionPool;
			this.executorService = Executors.newThreadPerTaskExecutor(Executors.defaultThreadFactory());
		}

		public void syncListeners(List<ListenerEndpoint> endpoints, String localUrl) {
			addToExecution(endpoints, localUrl);
			removeSpareListeners(endpoints);
		}

		public int numberOfListeners() {
			return states.size();
		}

		public void removeSpareListeners(List<ListenerEndpoint> desiredEndpoints) {
			for (ListenerEndpoint listenerEndpoint : states.keySet()) {
				if (! desiredEndpoints.contains(listenerEndpoint)) {
					MessageForwarder messageForwarder = states.get(listenerEndpoint);
					messageForwarder.stop();
					states.remove(listenerEndpoint);
				}
			}
		}

		public void addToExecution(List<ListenerEndpoint> endpoints, String localUrl) {
			for (ListenerEndpoint endpoint : endpoints) {
				addToExecution(endpoint, localUrl);
			}
		}

		public void addToExecution(ListenerEndpoint endpoint, String localUrl) {
			if (! states.containsKey(endpoint)) {
				MessageForwarder forwarder = new MessageForwarder(
						localUrl,
						endpoint.getTarget(),
						senderContext,
						connectionPool,
						endpoint.toUrl(),
						endpoint.getSource()
				);
				executorService.execute(forwarder);
				states.put(endpoint, forwarder);
			}
		}


		@Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (NewMessageCollector) obj;
            return Objects.equals(this.states, that.states) &&
                    Objects.equals(this.senderContext, that.senderContext) &&
                    Objects.equals(this.connectionPool, that.connectionPool) &&
                    Objects.equals(this.executorService, that.executorService);
        }

        @Override
        public int hashCode() {
            return Objects.hash(states, senderContext, connectionPool, executorService);
        }

        @Override
        public String toString() {
            return "NewMessageCollector[" +
                    "states=" + states + ", " +
                    "senderContext=" + senderContext + ", " +
                    "connectionPool=" + connectionPool + ", " +
                    "executorService=" + executorService + ']';
        }
	}
}
