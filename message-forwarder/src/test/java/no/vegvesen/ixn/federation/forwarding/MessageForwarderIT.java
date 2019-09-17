package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.TestKeystoreHelper;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageForwarderIT {


	private static Logger logger = LoggerFactory.getLogger(MessageForwarderIT.class);

	private static final Path QPID_DOCKER_PATH = getQpidDockerPath();

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

	private static final int AMQPS_PORT = 5671;

	//@Rule
	private GenericContainer localContainer = new GenericContainer(
			new ImageFromDockerfile().withFileFromPath(".", QPID_DOCKER_PATH))
			.withClasspathResourceMapping("docker/localhost", "/config", BindMode.READ_ONLY)
			.withClasspathResourceMapping("jks", "/jks", BindMode.READ_ONLY)
			.withEnv("PASSWD_FILE", "/config/passwd")
			.withEnv("STATIC_GROUPS_FILE", "/config/groups")
			.withEnv("STATIC_VHOST_FILE", "/config/vhost.json")
			.withEnv("VHOST_FILE", "/work/default/config/default.json")
			.withEnv("GROUPS_FILE", "/work/default/config/groups")
			.withEnv("CA_CERTIFICATE_FILE", "/jks/localhost.crt")
			.withEnv("SERVER_CERTIFICATE_FILE", "/jks/localhost.crt")
			.withEnv("SERVER_PRIVATE_KEY_FILE", "/jks/localhost.key")
			.withExposedPorts(AMQPS_PORT);

	@Test
	public void connectToLocalQpidAndListenForMessagesToRemote() throws JMSException, NamingException {
		System.out.println("startup attempts" + localContainer.getStartupAttempts());
		System.out.println("ci: " + localContainer.getContainerInfo());
		ForwarderProperties properties = new ForwarderProperties();
		properties.setLocalIxnFederationPort("" + localContainer.getMappedPort(AMQPS_PORT));
		properties.setLocalIxnDomainName("localhost");
//		properties.setRemoteWritequeue("fedEx");

		SSLContext bouvetSslContext = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");

		Neighbour remoteNeighbour = mock(Neighbour.class);
		when(remoteNeighbour.getName()).thenReturn("remote");
		NeighbourFetcher fetcher = mock(NeighbourFetcher.class);
//		when(fetcher.listNeighbourCandidates()).thenReturn(Collections.singletonList(remoteNeighbour));
		MessageForwarder messageForwarder = new MessageForwarder(fetcher, bouvetSslContext, properties);
		messageForwarder.createConsumerFromLocal(remoteNeighbour);


	}
}
