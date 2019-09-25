package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MessageForwardListenerIT extends QpidDockerBaseIT {

	private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	static Logger logger = LoggerFactory.getLogger(MessageForwardListenerIT.class);

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

	@Test
	public void testStopContainerTriggersConnectionExceptionListener() throws JMSException, NamingException {
		String localAmqpsUrl = String.format("amqps://localhost:%s", localContainer.getMappedPort(AMQPS_PORT));

		String readQueue = "remote";
		logger.debug("Creating destination for messages on queue [{}] from [{}]", readQueue, localAmqpsUrl);
		IxnContext context = new IxnContext(localAmqpsUrl, null, readQueue);
		Connection connection = context.createConnection(SSL_CONTEXT);
		connection.start();
		Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
		Destination queueR = context.getReceiveQueue();
		MessageConsumer messageConsumer = session.createConsumer(queueR);

		MessageForwardListener messageForwardListener = new MessageForwardListener(messageConsumer, mock(MessageProducer.class));
		connection.setExceptionListener(messageForwardListener);
		//Stop the container to trigger the connection exception listener to run
		localContainer.stop();

		assertThat(messageForwardListener.isRunning()).isFalse();
	}
}