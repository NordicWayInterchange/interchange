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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorLocalListenerIT extends QpidDockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(MessageCollectorLocalListenerIT.class);

	//@Container
	//static KeysContainer keysContainer = getKeyContainer(MessageCollectorLocalListenerIT.class,"my_ca", "localhost");
	static KeysStructure keysStructure = generateKeys(MessageCollectorLocalListenerIT.class,"my_ca","localhost");

	@Container
	public QpidContainer localContainer = getQpidTestContainer("docker/consumer",
			keysStructure.getKeysOutputPath(),
			keysStructure.getServerKeystoreName(),
			keysStructure.getKeystorePassword(),
			keysStructure.getTruststoreName(),
			keysStructure.getTruststorePassword(),
			"localhost");


	@Container
	public QpidContainer remoteContainer = getQpidTestContainer("docker/producer",
			keysStructure.getKeysOutputPath(),
			keysStructure.getServerKeystoreName(),
			keysStructure.getKeystorePassword(),
			keysStructure.getTruststoreName(),
			keysStructure.getTruststorePassword(),
			"localhost");

	@Test
	public void stoppingLocalContainerStopsListener() {
		SSLContext sslContext = TestKeystoreHelper.sslContext(keysStructure.getKeysOutputPath(), keysStructure.getServerKeystoreName(), keysStructure.getTruststoreName());
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
