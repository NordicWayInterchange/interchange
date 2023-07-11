package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	@Container
	public QpidContainer localContainer = getQpidTestContainer("docker/consumer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");


	@Container
	public QpidContainer remoteContainer = getQpidTestContainer("docker/producer",
			testKeysPath,
			"localhost.p12",
			"password",
			"truststore.jks",
			"password",
			"localhost");

	@Test
	public void stoppingLocalContainerStopsListener() {
		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "localhost.p12", "truststore.jks");
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getAmqpsPort().toString(), "subscriptionExchange");
		ListenerEndpoint remote = mock(ListenerEndpoint.class);
		when(remote.getTarget()).thenReturn("subscriptionExchange");
		when(remote.getHost()).thenReturn("localhost");
		when(remote.getPort()).thenReturn(remoteContainer.getAmqpsPort());
		when(remote.getSource()).thenReturn("localhost");
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}
}
