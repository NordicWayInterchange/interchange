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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
    private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

    @SuppressWarnings("rawtypes")
	@Container
    public GenericContainer localContainer = getQpidContainer("docker/consumer","jks", "localhost.p12","password","truststore.jks",	"password");

	@SuppressWarnings("rawtypes")
	@Container
    public GenericContainer remoteContainer = getQpidContainer("docker/producer","jks", "localhost.p12","password","truststore.jks", "password");

	@Test
    public void stoppingRemoteContainerStopsListener() {
		String remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		CollectorCreator collectorCreator = new CollectorCreator(SSL_CONTEXT, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
        Neighbour remote = mock(Neighbour.class);
        when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
