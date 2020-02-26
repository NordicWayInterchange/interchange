package no.vegvesen.ixn.federation.messagecollector;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.docker.DockerBaseIT;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class MessageCollectorListenerIT extends DockerBaseIT {

	private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	private static Logger logger = LoggerFactory.getLogger(MessageCollectorListenerIT.class);

	@Rule
	public GenericContainer localContainer = getQpidContainer("docker/localhost", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Rule
	public GenericContainer remoteContainer = getQpidContainer("docker/remote", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	private String remoteAmqpsUrl;
	private CollectorCreator collectorCreator;

	@Before
	public void setup() {
		Integer localAmqpsPort = localContainer.getMappedPort(AMQPS_PORT);
		Integer remoteAmqpsPort = remoteContainer.getMappedPort(AMQPS_PORT);
		remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteAmqpsPort);

		collectorCreator = new CollectorCreator(SSL_CONTEXT, "localhost", localAmqpsPort.toString(), "fedEx");
	}

	@Test
	public void testStopLocalContainerTriggersConnectionExceptionListener() throws JMSException, NamingException {
		Neighbour remote = mock(Neighbour.class);
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}

	@Test
	public void testStopRemoteContainerTriggersConnectionExceptionListener() throws JMSException, NamingException {
		Neighbour remote = mock(Neighbour.class);
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageCollectorListener remoteForwardListener = collectorCreator.setupCollection(remote);

		//Stop the container to trigger the connection exception listener to run
		remoteContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}

	/*

	TODO this test is removed to be able to refactor the internal classes in CollectorCreator.
	@Test
	public void createProducerToRemote() throws NamingException, JMSException {
		Neighbour remote = mock(Neighbour.class);
		when(remote.getName()).thenReturn("remote");
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);

		MessageProducer producerToRemote = collectorCreator.createProducerToLocal();

		assertThat(producerToRemote.getDestination()).isNotNull();
	}

	*/
}