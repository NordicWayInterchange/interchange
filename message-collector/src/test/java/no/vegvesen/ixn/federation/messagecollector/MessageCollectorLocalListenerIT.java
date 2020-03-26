package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageCollectorLocalListenerIT extends DockerBaseIT {
    private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	private static Logger logger = LoggerFactory.getLogger(MessageCollectorLocalListenerIT.class);

	@Rule
	public GenericContainer localContainer = getQpidContainer("docker/consumer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Rule
	public GenericContainer remoteContainer = getQpidContainer("docker/producer", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	private String remoteAmqpsUrl;
	private CollectorCreator collectorCreator;


	@Test
	public void stoppingLocalContainerStopsListener() throws NamingException, JMSException {
		remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteContainer.getMappedPort(AMQPS_PORT));
		collectorCreator = new CollectorCreator(SSL_CONTEXT, "localhost", localContainer.getMappedPort(AMQPS_PORT).toString(), "fedEx");
		Neighbour remote = mock(Neighbour.class);
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();


	}
}
