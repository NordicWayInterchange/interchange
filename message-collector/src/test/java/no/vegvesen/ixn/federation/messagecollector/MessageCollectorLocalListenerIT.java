package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.docker.QpidContainer;
import no.vegvesen.ixn.docker.QpidDockerBaseIT;
import no.vegvesen.ixn.federation.model.ListenerEndpoint;
import no.vegvesen.ixn.keys.generator.ClusterKeyGenerator.CaStores;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.net.ssl.SSLContext;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers
public class MessageCollectorLocalListenerIT extends QpidDockerBaseIT {


	public static final String HOST_NAME = getDockerHost();
	static final CaStores stores = generateStores(getTargetFolderPathForTestClass(MessageCollectorLocalListenerIT.class),"my_ca", HOST_NAME);

	@Container
	public QpidContainer localContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("docker","consumer")
			);




	@Container
	public QpidContainer remoteContainer = getQpidTestContainer(
			stores,
			HOST_NAME,
			HOST_NAME,
			Path.of("docker","producer")
			);


	@Test
	public void stoppingLocalContainerStopsListener() {
		SSLContext sslContext = sslServerContext(stores,HOST_NAME);
		CollectorCreator collectorCreator = new CollectorCreator(sslContext, HOST_NAME, localContainer.getAmqpsPort().toString(), "subscriptionExchange");
		ListenerEndpoint remote = mock(ListenerEndpoint.class);
		when(remote.getTarget()).thenReturn("subscriptionExchange");
		when(remote.getHost()).thenReturn(HOST_NAME);
		when(remote.getPort()).thenReturn(remoteContainer.getAmqpsPort());
		when(remote.getSource()).thenReturn(HOST_NAME);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}
}
