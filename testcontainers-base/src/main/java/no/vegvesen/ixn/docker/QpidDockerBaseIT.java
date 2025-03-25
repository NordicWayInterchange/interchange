package no.vegvesen.ixn.docker;

import jakarta.jms.*;
import no.vegvesen.ixn.keys.generator.*;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStore;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.ClientStore;
import no.vegvesen.ixn.ssl.KeystoreDetails;
import no.vegvesen.ixn.ssl.KeystoreType;
import no.vegvesen.ixn.ssl.SSLContextFactory;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;

public class QpidDockerBaseIT extends DockerBaseIT {

	private static final Logger logger = LoggerFactory.getLogger(QpidDockerBaseIT.class);

	public static QpidContainer getQpidTestContainer(CaStores stores, String vhostName, String hostname, Path configPath) {
		Path imageLocation = getFolderPath("qpid-test");
		logger.debug("Creating container qpid-it-memory, from Docker file from {} and config from {}",
				imageLocation, configPath);
		Stream<HostStore> stream = stores.hostStores().stream();
		HostStore hostStore = getHostStore(hostname, stream);
		CaStore caStore = stores.trustStore();
		String keystoreName = hostStore.path().getFileName().toString();
		String keystorePassword = hostStore.password();
		String truststoreName = caStore.path().getFileName().toString();
		String truststorePassword = caStore.password();
		return new QpidContainer("qpid-it-memory",
				imageLocation,
				configPath,
				caStore.path().getParent(),
				keystoreName,
				keystorePassword,
				truststoreName,
				truststorePassword,
				vhostName);
	}

	public static CaStores generateStores(Path outputPath, String ca, String server, String ... serviceProviders) {
        try {
            Files.createDirectories(outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        List<ClientRequest> clientRequests = new ArrayList<>();
		for (String serviceProvider : serviceProviders) {
			clientRequests.add(new ClientRequest(serviceProvider,"NO", serviceProvider + "@" + server));
		}
		CARequest request = new CARequest(
				ca,
				"NO",
				List.of(),
				List.of(new HostRequest(
						server
				)),
				clientRequests
		);
		CaResponse response;
		try {
            response = generate(request);
        } catch (CertificateException | NoSuchAlgorithmException | SignatureException | OperatorCreationException |
				 InvalidKeyException | NoSuchProviderException | CertIOException e) {
            throw new RuntimeException(e);
        }
        CaStores stores;
		try {
            stores = store(response,outputPath, () -> "password");
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
		return stores;
    }


	public static SSLContext sslClientContext(CaStores stores, String serviceProviderName) {
        ClientStore clientStore = getClientStore(serviceProviderName, stores.clientStores().stream());
		CaStore caStore = stores.trustStore();
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(
						clientStore.path().toString(),
						clientStore.password(),
						KeystoreType.PKCS12
				),
				new KeystoreDetails(
						caStore.path().toString(),
						caStore.password(),
						KeystoreType.JKS
				)
		);
	}

	public static String getTrustStorePath(CaStores stores) {
		return stores.trustStore().path().toString();
	}

	public static String getClientStorePath(String clientName, List<ClientStore> clientStores) {
		return getClientStore(clientName,clientStores.stream()).path().toString();

	}

	public static SSLContext sslServerContext(CaStores stores, String hostName) {
		HostStore hostStore = getHostStore(hostName, stores.hostStores().stream());
		CaStore trustStore = stores.trustStore();
		return SSLContextFactory.sslContextFromKeyAndTrustStores(
				new KeystoreDetails(
						hostStore.path().toString(),
						hostStore.password(),
						KeystoreType.PKCS12
				),
				new KeystoreDetails(
						trustStore.path().toString(),
						trustStore.password(),
						KeystoreType.JKS
				)
		);
	}

	/**
	 * A message listener that waits for n messages within a specified amount of time,
	 * and returns as soon as n is received
	 */
	protected static class CountNumberOfMessagesInTimeframe implements WaitingMessageListener {
		private final CountDownLatch latch;

		public CountNumberOfMessagesInTimeframe(int count) {
			latch = new CountDownLatch(count);
		}

		@Override
		public void onMessage(Message message) {
			latch.countDown();
		}

		@Override
		public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
			return latch.await(timeout, unit);
		}
	}

	/**
	 * A message Listener that waits for a certain amount of time, and registers the number of messages
	 * received in that time.
	 */
	protected static class WaitForTimeFrameAndCountMessages implements WaitingMessageListener {
		private final AtomicInteger numMessages;
		private final CountDownLatch latch;
        private final int expectedCount;

        public WaitForTimeFrameAndCountMessages(int expectedCount) {
            this.expectedCount = expectedCount;
            this.numMessages = new AtomicInteger();
			this.latch = new CountDownLatch(1);
		}

		@Override
		public void onMessage(Message message) {
			numMessages.incrementAndGet();
		}

		public void releaseLock() {
			latch.countDown();
		}

		public void releaseLockAfter(long timeout, TimeUnit unit) throws InterruptedException {
			unit.sleep(timeout);
			releaseLock();
		}

		public boolean success() {
			return numMessages.get() == expectedCount;
		}

		@Override
		public boolean waitFor(long timeOut, TimeUnit timeUnit) throws InterruptedException {
			releaseLockAfter(timeOut, timeUnit);
			return success();
		}
	}

	public static class WaitForMessage {

        private final SSLContext sslContext;
		private final String url;
        private final String queueName;
        private final WaitingMessageListener listener;


		public WaitForMessage(SSLContext sslContext, String url, String queueName, int count) {
			this.sslContext = sslContext;
			this.url = url;
            this.queueName = queueName;
            listener = new CountNumberOfMessagesInTimeframe(count);
        }

		public WaitForMessage(SSLContext sslContext, String url, String queueName, WaitingMessageListener listener) {
			this.sslContext = sslContext;
			this.url = url;
			this.queueName = queueName;
            this.listener = listener;
		}

		public boolean await(int timeOut, TimeUnit timeUnit) throws InterruptedException, JMSException {
			boolean success;
			JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(this.url);
			jmsConnectionFactory.setSslContext(sslContext);
			try (Connection connection = jmsConnectionFactory.createConnection()) {
				connection.start();
				try (Session session = connection.createSession(Session.AUTO_ACKNOWLEDGE)) {
					Destination destination = session.createQueue(this.queueName);
					try (MessageConsumer consumer = session.createConsumer(destination)) {
						consumer.setMessageListener(listener);
						success = listener.waitFor(timeOut, timeUnit);
					}
				}
			}
			return success;
		}

	}

	public interface WaitingMessageListener extends MessageListener {
		/**
		 * A messageListener that can report success after/within a certain amount of time
		 * @param timeOut number of timeunits for timeout
		 * @param timeUnit timeunits for timeout
		 * @return true if the condition is held, false otherwise
		 * @throws InterruptedException on interruptions
		 */
		boolean waitFor(long timeOut, TimeUnit timeUnit) throws InterruptedException;
	}
}