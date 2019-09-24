package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.Sink;
import no.vegvesen.ixn.Source;
import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageForwarderIT extends QpidDockerBaseIT {
	private static Logger logger = LoggerFactory.getLogger(MessageForwarderIT.class);

	private RestTemplate restTemplate() {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(localSslContext()).build();
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	@Rule
	public GenericContainer localContainer = new GenericContainer(
			new ImageFromDockerfile().withFileFromPath(".", QPID_DOCKER_PATH))
			.withClasspathResourceMapping("docker/localhost", "/config", BindMode.READ_ONLY)
			.withClasspathResourceMapping("jks", "/jks", BindMode.READ_ONLY)
			.withEnv("PASSWD_FILE", "/config/passwd")
			.withEnv("STATIC_GROUPS_FILE", "/config/groups")
			.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
			.withEnv("VHOST_FILE", "/work/default/config/default.json")
			.withEnv("GROUPS_FILE", "/work/default/config/groups")
			.withEnv("CA_CERTIFICATE_FILE", "/jks/my_ca.crt")
			.withEnv("SERVER_CERTIFICATE_FILE", "/jks/localhost.crt")
			.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/localhost.key")
			.withExposedPorts(AMQPS_PORT, HTTPS_PORT);

	@Rule
	public GenericContainer remoteContainer = new GenericContainer(
			new ImageFromDockerfile().withFileFromPath(".", QPID_DOCKER_PATH))
			.withClasspathResourceMapping("docker/remote", "/config", BindMode.READ_ONLY)
			.withClasspathResourceMapping("jks", "/jks", BindMode.READ_ONLY)
			.withEnv("PASSWD_FILE", "/config/passwd")
			.withEnv("STATIC_GROUPS_FILE", "/config/groups")
			.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
			.withEnv("VHOST_FILE", "/work/default/config/default.json")
			.withEnv("GROUPS_FILE", "/work/default/config/groups")
			.withEnv("CA_CERTIFICATE_FILE", "/jks/my_ca.crt")
			.withEnv("SERVER_CERTIFICATE_FILE", "/jks/localhost.crt")
			.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/localhost.key")
			.withExposedPorts(AMQPS_PORT, HTTPS_PORT);

	private SSLContext localSslContext() {
		return TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}

	@Test
	@Ignore("Ignored to await solution for MessageForwardListener discovers that the queue is gone")
	public void reCreationOfForwardQueueWillForwardMessagesWhenNewQueueGetsAvailable() throws JMSException, NamingException {
		Integer localMessagePort = localContainer.getMappedPort(AMQPS_PORT);

		Neighbour remoteNeighbour = mock(Neighbour.class);
		when(remoteNeighbour.getName()).thenReturn("remote");
		String remoteMessagePort = "" + remoteContainer.getMappedPort(AMQPS_PORT);
		String remoteControlChannelPort = "" + remoteContainer.getMappedPort(HTTPS_PORT);
		// The both the local and remote server runs on localhost, but different ports
		String remoteUrl = String.format("amqps://localhost:%s", remoteMessagePort);
		when(remoteNeighbour.getControlChannelPort()).thenReturn(remoteControlChannelPort);
		when(remoteNeighbour.getMessageChannelPort()).thenReturn(remoteMessagePort);
		when(remoteNeighbour.getMessageChannelUrl()).thenReturn(remoteUrl);
		NeighbourFetcher fetcher = mock(NeighbourFetcher.class);
		when(fetcher.listNeighbourCandidates()).thenReturn(Collections.singletonList(remoteNeighbour));
		ForwarderProperties properties = new ForwarderProperties();
		properties.setLocalIxnFederationPort("" + localMessagePort);
		properties.setLocalIxnDomainName("localhost");
		properties.setRemoteWritequeue("fedEx");
		MessageForwarder messageForwarder = new MessageForwarder(fetcher, localSslContext(), properties);
		messageForwarder.runSchedule();

		String sendUrl = String.format("amqps://localhost:%s", localMessagePort);
		Source source = new Source(sendUrl, "remote", localSslContext());
		source.start();
		source.send("fisk");

		Sink sink = new Sink(remoteUrl, "se-out", localSslContext());
		MessageConsumer sinkConsumer = sink.createConsumer();
		Message receive1 = sinkConsumer.receive(1000);
		assertThat(receive1).withFailMessage("no messages are routed").isNotNull();

		logger.debug("Removing the queue 'remote' on local node, should give error on consumer");
		QpidClient qpidClient = new QpidClient(String.format("https://localhost:%s/", localContainer.getMappedPort(HTTPS_PORT)), "localhost", restTemplate());
		qpidClient.removeQueue("remote");


		logger.debug("Recreating the message queue");
		when(remoteNeighbour.getSubscriptionRequest()).thenReturn(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
		qpidClient.setupRouting(remoteNeighbour, "nwEx");

		messageForwarder.runSchedule();

		source.start();
		source.send("mer fisk");
		Message receive2 = sinkConsumer.receive(1000);
		assertThat(receive2).withFailMessage("message sent after forwarding queue is recreated is not forwarded").isNotNull();
	}

	@Test
	public void forwarderConnectsToRemoteQpidAndForwardsMessageSent() throws JMSException, NamingException {
		Integer localMessagePort = localContainer.getMappedPort(AMQPS_PORT);

		Neighbour remoteNeighbour = mock(Neighbour.class);
		when(remoteNeighbour.getName()).thenReturn("remote");
		String remoteMessagePort = "" + remoteContainer.getMappedPort(AMQPS_PORT);
		String remoteControlChannelPort = "" + remoteContainer.getMappedPort(HTTPS_PORT);
		// The both the local and remote server runs on localhost, but different ports
		String remoteUrl = String.format("amqps://localhost:%s", remoteMessagePort);
		when(remoteNeighbour.getControlChannelPort()).thenReturn(remoteControlChannelPort);
		when(remoteNeighbour.getMessageChannelPort()).thenReturn(remoteMessagePort);
		when(remoteNeighbour.getMessageChannelUrl()).thenReturn(remoteUrl);
		NeighbourFetcher fetcher = mock(NeighbourFetcher.class);
		when(fetcher.listNeighbourCandidates()).thenReturn(Collections.singletonList(remoteNeighbour));
		ForwarderProperties properties = new ForwarderProperties();
		properties.setLocalIxnFederationPort("" + localMessagePort);
		properties.setLocalIxnDomainName("localhost");
		properties.setRemoteWritequeue("fedEx");
		MessageForwarder messageForwarder = new MessageForwarder(fetcher, localSslContext(), properties);
		messageForwarder.runSchedule();

		String sendUrl = String.format("amqps://localhost:%s", localMessagePort);
		Source source = new Source(sendUrl, "remote", localSslContext());
		source.start();
		source.send("fish");

		Sink sink = new Sink(remoteUrl, "se-out", localSslContext());
		MessageConsumer sinkConsumer = sink.createConsumer();
		Message receive1 = sinkConsumer.receive(1000);
		assertThat(receive1).withFailMessage("first messages is not routed").isNotNull();

		source.send("more fish");
		Message receive2 = sinkConsumer.receive(1000);
		assertThat(receive2).withFailMessage("first messages is not routed").isNotNull();
	}

}
