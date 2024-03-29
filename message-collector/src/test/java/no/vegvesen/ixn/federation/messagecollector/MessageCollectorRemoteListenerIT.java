package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Testcontainers
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

    static KeysStructure keysStructure = generateKeys(MessageCollectorRemoteListenerIT.class,"my_ca","localhost");

	@Container
    public QpidContainer localContainer = getQpidTestContainer("docker/consumer",
            keysStructure,
            "localhost");

	@Container
    public QpidContainer remoteContainer = getQpidTestContainer("docker/producer",
            keysStructure,
            "localhost");

	@Test
    public void stoppingRemoteContainerStopsListener() {
		SSLContext sslContext = sslServerContext(keysStructure);
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getAmqpsPort().toString(), "subscriptionExchange");
        ListenerEndpoint remote = mock(ListenerEndpoint.class);
        when(remote.getTarget()).thenReturn("subscriptionExchange");
        when(remote.getHost()).thenReturn("localhost");
        when(remote.getPort()).thenReturn(remoteContainer.getAmqpsPort());
        when(remote.getSource()).thenReturn("localhost");
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
