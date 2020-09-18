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
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
	static Path path = getFolderPath("message-collector/target/testKeys-MessageCollectorRemoteListenerIT");
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

	@SuppressWarnings("rawtypes")
	@Container
	public static GenericContainer keyContainer = getKeyContainer(path,"my_ca", "localhost");

	@SuppressWarnings("rawtypes")
	@Container
    public GenericContainer localContainer = getQpidContainer("docker/consumer", path, "localhost.p12","password","truststore.jks",	"password");

	@SuppressWarnings("rawtypes")
	@Container
    public GenericContainer remoteContainer = getQpidContainer("docker/producer",path, "localhost.p12","password","truststore.jks", "password");

	@Test
    public void stoppingRemoteContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		SSLContext sslContext = TestKeystoreHelper.sslContext(path, "localhost.p12", "truststore.jks");
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
        Neighbour remote = mock(Neighbour.class);
        when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
