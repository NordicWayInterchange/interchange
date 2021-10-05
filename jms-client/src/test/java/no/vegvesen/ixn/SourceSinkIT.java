package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.properties.MessageProperty;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
			"localhost").dependsOn(keysContainer);

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
		JmsMessage fisk = kingHaraldTestQueueSource.createMessageBuilder().textMessage("fisk").build();
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
		JmsMessage fisk = kingHaraldTestQueueSource.createMessageBuilder().textMessage("fisk").build();
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
		if (null != null && !((String) null).startsWith(",")) {
			throw new IllegalArgumentException("when quad tree is specified it must start with comma \",\"");
		}
		JmsMessage message = kingHaraldTestQueueSource.createMessageBuilder()
				.textMessage("fisk")
				.userId("localhost")
				.messageType(Constants.DATEX_2)
				.publicationType("Obstruction")
				.protocolVersion("DATEX2;2.3")
				.latitude(60.352374)
				.longitude(13.334253)
				.originatingCountry("SE")
				.quadTreeTiles(null)
				.timestamp(System.currentTimeMillis())
				.build();
		kingHaraldTestQueueSource.sendNonPersistentMessage(message);

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
		byte[] bytemessage = "FIIIIIISK!".getBytes(StandardCharsets.UTF_8);
		source.sendNonPersistentMessage(source.createMessageBuilder()
				.bytesMessage(bytemessage)
				.userId("localhost")
				.messageType("DENM")
				.publisherId("NO-12345")
				.protocolVersion("DENM:1.2.2")
				.originatingCountry("NO")
				.quadTreeTiles("")
				.serviceType("some-denm-service-type")
				.causeCode( "3")
				.subCauseCode("6")
				.timestamp(System.currentTimeMillis())
				.build());

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

		byte[] bytemessage = "FIIIIIISK!".getBytes(StandardCharsets.UTF_8);
		JmsMessage message = source.createMessageBuilder()
				.bytesMessage(bytemessage)
				.userId("localhost")
				.messageType("IVI")
				.publisherId("NO-12345")
				.protocolVersion("IVI:1.2")
				.originatingCountry("NO")
				.quadTreeTiles("")
				.serviceType("some-ivi-service-type")
				.timestamp(System.currentTimeMillis())
				.stringProperty(MessageProperty.IVI_TYPE.getName(), "128")
				.stringProperty(MessageProperty.PICTOGRAM_CATEGORY_CODE.getName(), "557")
				.build();

		source.sendNonPersistentMessage(message);

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
