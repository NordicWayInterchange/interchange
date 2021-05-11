package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import org.apache.qpid.jms.message.JmsTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Testcontainers
public class SourceSinkIT extends QpidDockerBaseIT {



	@Container
	static KeysContainer keysContainer = getKeyContainer(SourceSinkIT.class,"my_ca","localhost","king_harald");

	@SuppressWarnings("rawtypes")
	@Container
	public final QpidContainer qpidContainer = getQpidTestContainer("qpid",
			keysContainer.getKeyFolderOnHost(),
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");
	private String URL;
	private SSLContext KING_HARALD_SSL_CONTEXT;

	@BeforeEach
	public void setUp() {
		URL = qpidContainer.getAmqpsUrl();
		KING_HARALD_SSL_CONTEXT = TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(),"king_harald.p12", "truststore.jks");
	}

	@Test
	public void explicitExpiryIsReceived() throws JMSException, NamingException {
		Source kingHaraldTestQueueSource = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		kingHaraldTestQueueSource.start();
		JmsTextMessage fisk = kingHaraldTestQueueSource.createTextMessage("fisk");
		kingHaraldTestQueueSource.sendNonPersistentMessage(fisk, 2000);

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
		kingHaraldTestQueueSource.sendNonPersistentMessage(fisk, 200);

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
		kingHaraldTestQueueSource.sendNonPersistent("fisk"); //send with default expiry (0)

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

	@Test
	public void sendNonPersistentDenmByteMessage() throws JMSException, NamingException {
		Source source = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		source.start();
		source.sendNonPersistentDenmByteMessage("FIIIIIISK!", "NO", "");

		Sink sink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testConsumer = sink.createConsumer();
		Message receive = testConsumer.receive(1000);
		sink.onMessage(receive);
		assertThat(receive).isNotNull();
	}

	@Test
	public void sendNonPersistentIviByteMessage() throws JMSException, NamingException {
		Source source = new Source(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		source.start();
		source.sendNonPersistentIviByteMessage("FIIIIIISK!", "NO", "");

		Sink sink = new Sink(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		MessageConsumer testConsumer = sink.createConsumer();
		Message receive = testConsumer.receive(1000);
		sink.onMessage(receive);
		assertThat(receive).isNotNull();
	}


	//TODO how about doing this from different threads?
	@Test
	public void sendNonPersistentBytesMessageWithImage() throws JMSException, NamingException, IOException {
		ImageSource source = new ImageSource(URL, "test-queue", KING_HARALD_SSL_CONTEXT);
		source.start();
		source.sendNonPersistentByteMessageWithImage("NO", "", "src/images/cabin_view.jpg");

		try (ImageSink sink = new ImageSink(URL, "test-queue", KING_HARALD_SSL_CONTEXT)) {
			MessageConsumer testConsumer = sink.createConsumer();
			Message receive = testConsumer.receive(1000);
			sink.onMessage(receive);
			assertThat(receive).isNotNull();
		} catch (Exception e) {
			fail("unexpected exception",e);
		}
	}
}
