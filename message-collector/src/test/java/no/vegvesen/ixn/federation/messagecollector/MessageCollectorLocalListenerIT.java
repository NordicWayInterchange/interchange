package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorLocalListenerIT extends DockerBaseIT {
    private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	private static Logger logger = LoggerFactory.getLogger(MessageCollectorLocalListenerIT.class);

	@SuppressWarnings("rawtypes")
	@Container
	public GenericContainer localContainer = getQpidContainer("docker/consumer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@SuppressWarnings("rawtypes")
	@Container
	public GenericContainer remoteContainer = getQpidContainer("docker/producer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Test
	public void stoppingLocalContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		CollectorCreator collectorCreator = new CollectorCreator(SSL_CONTEXT, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
		Neighbour remote = mock(Neighbour.class);
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}
}
