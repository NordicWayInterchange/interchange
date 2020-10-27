package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorLocalListenerIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(MessageCollectorLocalListenerIT.class);
	static Path testKeysPath = generateKeys(MessageCollectorLocalListenerIT.class,"my_ca", "localhost");

	@SuppressWarnings("rawtypes")
	@Container
	public GenericContainer localContainer = getQpidTestContainer("docker/consumer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");


	@SuppressWarnings("rawtypes")
	@Container
	public GenericContainer remoteContainer = getQpidTestContainer("docker/producer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

	@Test
	public void stoppingLocalContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "localhost.p12", "truststore.jks");
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
		Neighbour remote = mock(Neighbour.class);
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}
}
