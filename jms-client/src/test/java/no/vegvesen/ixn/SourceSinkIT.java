package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class SourceSinkIT extends QpidDockerBaseIT {

	static Path testKeysPath = generateKeys(SourceSinkIT.class,"my_ca", "localhost", "king_harald");

	@SuppressWarnings("rawtypes")
	@Container
	public final GenericContainer qpidContainer = getQpidContainerGeneratedKeys("qpid", testKeysPath, "localhost.p12", "password", "truststore.jks", "password");
	private String URL;
	private SSLContext KING_HARALD_SSL_CONTEXT;

	@BeforeEach
	public void setUp() {
		Integer mappedPort = qpidContainer.getMappedPort(5671);
		URL = String.format("amqps://localhost:%s/", mappedPort);
		KING_HARALD_SSL_CONTEXT = TestKeystoreHelper.sslContext(testKeysPath,"king_harald.p12", "truststore.jks");
	}

	@Test
	public void explicitExpiryIsReceived() throws JMSException, NamingException {
		Source kingHaraldTestQueueSource = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		JmsTextMessage fisk = kingHaraldTestQueueSource.createTextMessage("fisk");
		kingHaraldTestQueueSource.sendTextMessage(fisk, 2000);

		Sink kingHaraldTestQueueSink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNotNull();
		assertThat(receive.getJMSExpiration()).isNotNull().isGreaterThan(0);
	}

	@Test
	public void expiredMessageIsNotDelivered() throws JMSException, NamingException, InterruptedException {
		Source kingHaraldTestQueueSource = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		JmsTextMessage fisk = kingHaraldTestQueueSource.createTextMessage("fisk");
		kingHaraldTestQueueSource.sendTextMessage(fisk, 200);

		Thread.sleep(1000);

		Sink kingHaraldTestQueueSink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNull();
	}

	@Test
	public void queueMaxTtlIsRespected() throws JMSException, NamingException, InterruptedException {
		Source kingHaraldTestQueueSource = new Source(URL, "expiry-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		kingHaraldTestQueueSource.send("fisk"); //send with default expiry (0)

		Thread.sleep(2000); // let the message expire on the queue with queue declaration "maximumMessageTtl": 1000

		Sink kingHaraldTestQueueSink = new Sink(URL, "expiry-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testQueueConsumer = kingHaraldTestQueueSink.createConsumer();
		Message receive = testQueueConsumer.receive(1000);
		assertThat(receive).isNull();
	}

	@Test
	public void sourceCloseIsClosed() throws JMSException, NamingException {
		Source source = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		source.start();
		assertThat(source.isConnected()).isTrue();
		source.close();
		assertThat(source.isConnected()).isFalse();
	}
}
