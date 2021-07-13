package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
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
import static org.mockito.Mockito.*;

@Testcontainers
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

	static Path testKeysPath = generateKeys(MessageCollectorRemoteListenerIT.class,"my_ca", "localhost");

	@SuppressWarnings("rawtypes")
	@Container
    public GenericContainer localContainer = getQpidTestContainer("docker/consumer", testKeysPath, "localhost.p12","password","truststore.jks",	"password","localhost");

	@SuppressWarnings("rawtypes")
	@Container
    public GenericContainer remoteContainer = getQpidTestContainer("docker/producer", testKeysPath, "localhost.p12","password","truststore.jks", "password","localhost");

	@Test
    public void stoppingRemoteContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		SSLContext sslContext = TestKeystoreHelper.sslContext(testKeysPath, "localhost.p12", "truststore.jks");
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
        ListenerEndpoint remote = mock(ListenerEndpoint.class);
        when(remote.getBrokerUrl()).thenReturn(remoteAmqpsUrl);
        when(remote.getQueue()).thenReturn("localhost");
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
