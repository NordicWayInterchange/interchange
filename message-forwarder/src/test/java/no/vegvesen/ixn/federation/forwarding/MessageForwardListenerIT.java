package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MessageForwardListenerIT extends DockerBaseIT {

	private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	private static Logger logger = LoggerFactory.getLogger(MessageForwardListenerIT.class);

	@Rule
	public GenericContainer localContainer = getQpidContainer("docker/localhost", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	@Rule
	public GenericContainer remoteContainer = getQpidContainer("docker/remote", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

	private String remoteAmqpsUrl;
	private ForwardingCreator forwardingCreator;

	@Before
	public void setup() {
		Integer localAmqpsPort = localContainer.getMappedPort(AMQPS_PORT);
		String localAmqpsUrl = String.format("amqps://localhost:%s", localAmqpsPort);
		Integer remoteAmqpsPort = remoteContainer.getMappedPort(AMQPS_PORT);
		remoteAmqpsUrl = String.format("amqps://localhost:%s", remoteAmqpsPort);

		ForwarderProperties forwarderProperties = new ForwarderProperties();
		forwarderProperties.setLocalIxnDomainName("localhost");
		forwarderProperties.setLocalIxnFederationPort("" + localAmqpsPort);
		forwarderProperties.setRemoteWritequeue("fedEx");
		forwardingCreator = new ForwardingCreator(forwarderProperties, SSL_CONTEXT);
	}

	@Test
	public void testStopContainerTriggersConnectionExceptionListener() throws JMSException, NamingException {
		Neighbour remote = mock(Neighbour.class);
		when(remote.getName()).thenReturn("remote");
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);
		MessageForwardListener remoteForwardListener = forwardingCreator.setupForwarding(remote);

		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(remoteForwardListener.isRunning()).isFalse();
	}

	@Test
	public void createProducerToRemote() throws NamingException, JMSException {
		Neighbour remote = mock(Neighbour.class);
		when(remote.getName()).thenReturn("remote");
		when(remote.getMessageChannelUrl()).thenReturn(remoteAmqpsUrl);

		MessageProducer producerToRemote = forwardingCreator.createProducerToRemote(remote);

		assertThat(producerToRemote.getDestination()).isNotNull();
	}
}