package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.api.v1_0.Constants;
import no.vegvesen.ixn.model.IllegalMessageException;
import org.apache.qpid.jms.message.JmsMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.jms.JMSException;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
public class SourceSinkIT extends QpidDockerBaseIT {

	private static final Logger logger = LoggerFactory.getLogger(SourceSinkIT.class);

	private static final String SP_NAME = "king_harald";

	final static CaStores stores = generateStores(getTargetFolderPathForTestClass(SourceSinkIT.class),"my_ca","localhost", SP_NAME);

	@Container
	public final QpidContainer qpidContainer = getQpidTestContainer(
			stores,
		"localhost",
			"localhost", Paths.get("qpid"))
		.withLogConsumer(new Slf4jLogConsumer(logger));


	private String Url;
	private SSLContext kingHaraldSSlContext;

	@BeforeEach
	public void setUp() {
		Url = qpidContainer.getAmqpsUrl();
		kingHaraldSSlContext = sslClientContext(stores, SP_NAME);
	}

	@Test
	public void invalidDatexMessageThrowsError() throws JMSException, NamingException{
        try (Source kingHaraldTestQueueSource = new Source(Url, "test-queue", kingHaraldSSlContext)) {
            kingHaraldTestQueueSource.start();
            JmsMessage fisk = kingHaraldTestQueueSource.createMessageBuilder()
                    .textMessage("fisk")
                    .userId("localhost")
                    .messageType(Constants.DATEX_2)
                    .publisherId("king_harald")
                    .publicationId("NO00001")
                    .publicationType("Obstruction")
                    .publisherName("publishername")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .quadTreeTiles(",120002,")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build();
            kingHaraldTestQueueSource.sendNonPersistentMessage(fisk, 2000);
        }

		CountDownLatch latch = new CountDownLatch(1);
		boolean success;
        try (Sink kingHaraldTestQueueSink = new Sink(Url, "test-queue", kingHaraldSSlContext,message -> latch.countDown())) {
			kingHaraldTestQueueSink.start();
			success = latch.await(1,TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(success).isTrue();

	}

	@Test
	public void explicitExpiryIsReceived() throws JMSException, NamingException {
        try (Source kingHaraldTestQueueSource = new Source(Url, "test-queue", kingHaraldSSlContext)) {
            kingHaraldTestQueueSource.start();
            assertThrows(IllegalMessageException.class, () -> kingHaraldTestQueueSource.createMessageBuilder()
                    .textMessage("fisk")
                    .userId("localhost")
                    .messageType(Constants.DATEX_2)
                    .publisherId("king_harald")
                    .publicationId("NO00001")
                    .publicationType("Obstruction")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .quadTreeTiles(",120002,")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build());
        }

    }

	@Test
	public void expiredMessageIsNotDelivered() throws JMSException, NamingException, InterruptedException {
        try (Source kingHaraldTestQueueSource = new Source(Url, "test-queue", kingHaraldSSlContext)) {
            kingHaraldTestQueueSource.start();
            JmsMessage fisk = kingHaraldTestQueueSource.createMessageBuilder()
                    .textMessage("fisk")
                    .userId("localhost")
                    .messageType(Constants.DATEX_2)
                    .publisherId("king_harald")
                    .publicationId("NO00001")
                    .publicationType("Obstruction")
                    .publisherName("publishername")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .quadTreeTiles(",120002,")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build();
            kingHaraldTestQueueSource.sendNonPersistentMessage(fisk, 200);
        }

        Thread.sleep(1000);

		CountDownLatch latch = new CountDownLatch(1);
		boolean success;
		try (Sink kingHaraldTestQueueSink = new Sink(Url, "test-queue", kingHaraldSSlContext, message -> latch.countDown())) {
			kingHaraldTestQueueSink.start();
            success = latch.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		assertThat(success).isFalse();
	}

	@Test
	public void queueMaxTtlIsRespected() throws JMSException, NamingException, InterruptedException {
        try (Source kingHaraldTestQueueSource = new Source(Url, "expiry-queue", kingHaraldSSlContext)) {
            kingHaraldTestQueueSource.start();
            JmsMessage message = kingHaraldTestQueueSource.createMessageBuilder()
                    .textMessage("fisk")
                    .userId("localhost")
                    .messageType(Constants.DATEX_2)
                    .publisherId("king_harald")
                    .publicationId("NO00001")
                    .publicationType("Obstruction")
                    .publisherName("publishername")
                    .protocolVersion("DATEX2;2.3")
                    .latitude(60.352374)
                    .longitude(13.334253)
                    .originatingCountry("SE")
                    .quadTreeTiles(",120002,")
                    .shardId(1)
                    .shardCount(1)
                    .timestamp(System.currentTimeMillis())
                    .build();
            kingHaraldTestQueueSource.sendNonPersistentMessage(message);
        }

        Thread.sleep(2000); // let the message expire on the queue with queue declaration "maximumMessageTtl": 1000

		CountDownLatch latch = new CountDownLatch(1);
		boolean success;
        try (Sink kingHaraldTestQueueSink = new Sink(Url, "expiry-queue", kingHaraldSSlContext, m -> latch.countDown())) {
			kingHaraldTestQueueSink.start();
			success = latch.await(1,TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
		assertThat(success).isFalse();
	}

	@Test
	public void sourceCloseIsClosed() throws JMSException, NamingException {
		Source source = new Source(Url, "test-queue", kingHaraldSSlContext);
		source.start();
		assertThat(source.isConnected()).isTrue();
		source.close();
		assertThat(source.isConnected()).isFalse();
	}

	@Test
	public void sendNonPersistentDenmByteMessage() throws JMSException, NamingException {
        try (Source source = new Source(Url, "test-queue", kingHaraldSSlContext)) {
            source.start();
            byte[] bytemessage = "FIIIIIISK!".getBytes(StandardCharsets.UTF_8);
            source.sendNonPersistentMessage(source.createMessageBuilder()
                    .bytesMessage(bytemessage)
                    .userId("localhost")
                    .messageType("DENM")
                    .publisherId("NO-12345")
                    .publicationId("pub-1")
                    .protocolVersion("DENM:1.2.2")
                    .originatingCountry("NO")
                    .quadTreeTiles("0122")
                    .serviceType("some-denm-service-type")
                    .causeCode(3)
                    .subCauseCode(6)
                    .timestamp(System.currentTimeMillis())
                    .build());
        }

		CountDownLatch latch = new CountDownLatch(1);
		boolean hasCountedDown;
        try (Sink sink = new Sink(Url, "test-queue", kingHaraldSSlContext, m -> latch.countDown())) {
			sink.start();
			hasCountedDown = latch.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(hasCountedDown).isTrue();
	}

	@Test
	public void sendNonPersistentIviByteMessage() throws JMSException, NamingException, InterruptedException {
        try (Source source = new Source(Url, "test-queue", kingHaraldSSlContext)) {
            source.start();

            byte[] bytemessage = "FIIIIIISK!".getBytes(StandardCharsets.UTF_8);
            JmsMessage message = source.createMessageBuilder()
                    .bytesMessage(bytemessage)
                    .userId("localhost")
                    .messageType("IVIM")
                    .publisherId("NO-12345")
                    .publicationId("pub-1")
                    .protocolVersion("IVI:1.2")
                    .originatingCountry("NO")
                    .quadTreeTiles("0122")
                    .serviceType("some-ivi-service-type")
                    .timestamp(System.currentTimeMillis())
                    .iviType("128")
                    .pictogramCategoryCode("557")
                    .build();

            source.sendNonPersistentMessage(message);
        }

		CountDownLatch latch = new CountDownLatch(1);
		boolean await;
		try (Sink sink = new Sink(Url, "test-queue", kingHaraldSSlContext,m -> latch.countDown())) {
			sink.start();
            await = latch.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
		assertThat(await).isTrue();
	}


	@Test
	public void sendNonPersistentBytesMessageWithImage() throws JMSException, NamingException, IOException, InterruptedException {
        try (ImageSource source = new ImageSource(Url, "test-queue", kingHaraldSSlContext)) {
            source.start();
            source.sendNonPersistentByteMessageWithImage("NO", "", "src/images/cabin_view.jpg");
        }

        CountDownLatch latch = new CountDownLatch(1);
		boolean await;
		try (Sink sink = new Sink(Url, "test-queue", kingHaraldSSlContext, m -> latch.countDown())) {
			sink.start();
            await = latch.await(1, TimeUnit.SECONDS);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(await).isTrue();
	}
}
