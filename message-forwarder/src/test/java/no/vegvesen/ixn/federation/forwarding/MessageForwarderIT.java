package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageForwarderIT {
	private static Logger logger = LoggerFactory.getLogger(MessageForwarderIT.class);
	private static final int HTTPS_PORT = 443;
	private static final int AMQPS_PORT = 5671;
	private static final Path QPID_DOCKER_PATH = getQpidDockerPath();

	RestTemplate restTemplate() {
		CloseableHttpClient httpClient = HttpClients.custom().setSSLContext(localSslContext()).build();
		return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
	}

	private static Path getQpidDockerPath() {
		Path run = Paths.get(".").toAbsolutePath();
		logger.debug("Resolving qpid path from run path: " + run.toAbsolutePath().toString());
		Path projectRoot = null;
		while (projectRoot == null && run.getParent() != null) {
			if (run.endsWith("interchange")) {
				projectRoot = run;
			}
			run = run.getParent();
		}
		if (projectRoot != null) {
			Path qpid = projectRoot.resolve("qpid");
			logger.debug("Resolved qpid path {}", qpid.toAbsolutePath().toString());
			return qpid;
		}
		throw new RuntimeException("Could not resolve path to qpid docker folder in parent folder of " + run.toString());
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
			.withEnv("SERVER_CERTIFICATE_FILE", "/jks/remote.crt")
			.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/remote.key")
			.withExposedPorts(AMQPS_PORT, HTTPS_PORT);

	SSLContext localSslContext() {
		return TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	}

	@Test
	public void connectToLocalQpidAndListenForMessagesToRemote() throws JMSException, NamingException {
		ForwarderProperties properties = new ForwarderProperties();
		properties.setLocalIxnFederationPort("" + localContainer.getMappedPort(AMQPS_PORT));
		properties.setLocalIxnDomainName("localhost");
		properties.setRemoteWritequeue("fedEx");

		Neighbour remoteNeighbour = mock(Neighbour.class);
		when(remoteNeighbour.getName()).thenReturn("remote");
		String remoteMessagePort = "" + remoteContainer.getMappedPort(AMQPS_PORT);
		String remoteControlChannelPort = "" + remoteContainer.getMappedPort(HTTPS_PORT);
		String remoteUrl = String.format("amqps://%s:%s",
				remoteNeighbour.getName(),
				remoteMessagePort);
		when(remoteNeighbour.getControlChannelPort()).thenReturn(remoteControlChannelPort);
		when(remoteNeighbour.getMessageChannelPort()).thenReturn(remoteMessagePort);
		when(remoteNeighbour.getMessageChannelUrl()).thenReturn(remoteUrl);
		NeighbourFetcher fetcher = mock(NeighbourFetcher.class);
		when(fetcher.listNeighbourCandidates()).thenReturn(Collections.singletonList(remoteNeighbour));
		MessageForwarder messageForwarder = new MessageForwarder(fetcher, localSslContext(), properties);
		messageForwarder.runSchedule();

/*
		QpidClient qpidClient = new QpidClient(String.format("https://localhost:%s/", localContainer.getMappedPort(HTTPS_PORT)), "localhost", restTemplate());
		qpidClient.removeQueue("remote");
*/


	}
}
