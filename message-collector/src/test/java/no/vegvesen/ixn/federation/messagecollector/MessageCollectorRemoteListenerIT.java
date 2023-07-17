package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.KeysContainer;
import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.Connection;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class MessageCollectorRemoteListenerIT extends QpidDockerBaseIT {
    private static Logger logger = LoggerFactory.getLogger(MessageCollectorRemoteListenerIT.class);

    @Container
	static KeysContainer keysContainer = getKeyContainer(MessageCollectorRemoteListenerIT.class,"my_ca", "localhost");

	@Container
    public QpidContainer localContainer = getQpidTestContainer("docker/consumer", keysContainer.getKeyFolderOnHost(), "localhost.p12","password","truststore.jks",	"password","localhost")
            .dependsOn(keysContainer);

	@Container
    public QpidContainer remoteContainer = getQpidTestContainer("docker/producer", keysContainer.getKeyFolderOnHost(), "localhost.p12","password","truststore.jks", "password","localhost")
            .dependsOn(keysContainer);

	@Test
    public void stoppingRemoteContainerStopsListener() {
		SSLContext sslContext = TestKeystoreHelper.sslContext(keysContainer.getKeyFolderOnHost(), "localhost.p12", "truststore.jks");
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, "localhost", localContainer.getAmqpsPort().toString(), "subscriptionExchange");
        ListenerEndpoint remote = new ListenerEndpoint(
                "Remote",
                "localhost",
                "localhost",
                remoteContainer.getAmqpsPort(),
                new Connection(),
                "subscriptionExchange"
        );
        MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);
        remoteContainer.stop();

        assertThat(remoteForwardListener.isRunning()).isFalse();
    }

}
