package no.vegvesen.ixn.federation.forwarding;

import no.vegvesen.ixn.IxnContext;
import no.vegvesen.ixn.TestKeystoreHelper;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

import javax.jms.*;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MessageForwardListenerIT extends DockerBaseIT {

	private static final SSLContext SSL_CONTEXT = TestKeystoreHelper.sslContext("jks/localhost.p12", "jks/truststore.jks");
	private static Logger logger = LoggerFactory.getLogger(MessageForwardListenerIT.class);

	@Rule
	public GenericContainer localContainer = getQpidContainer("docker/localhost", "jks", "my_ca.crt", "localhost.crt", "localhost.key");

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