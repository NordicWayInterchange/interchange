package no.vegvesen.ixn;

import no.vegvesen.ixn.docker.DockerBaseIT;
import org.apache.qpid.jms.exceptions.JmsConnectionFailedException;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("rawtypes")
public class IxnContextTest extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(IxnContextTest.class);


	@ClassRule
	public static GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key");

	private String getQpidURI() {
		String url = "amqps://localhost:" + localContainer.getMappedPort(AMQPS_PORT);
		logger.info("connection string to local message broker {}", url);
		return url;
	}


	/**
	 * This test triggers a DoS recognition on the server to avoid too many open connections.
	 * 2020-03-12 10:22:56,500 WARN  [IO-/10.166.15.234:58076] (o.a.q.s.t.AbstractAMQPConnection) - Connection has taken more than 10000 ms to establish.  Closing as possible DoS.
	 * 2020-03-12 10:22:56,500 INFO  [IO-/10.166.15.234:58076] (q.m.c.idle_close) - [Broker] CON-1003 : Closed due to inactivity : Protocol connection is not established within timeout period
	 * <p>
	 * The situation we are trying to reproduce is the one from MultiVersionProtocolEngine.
	 * 2020-03-12 07:22:36,813 WARN  [IO-/10.36.3.1:33884] (o.a.q.s.t.MultiVersionProtocolEngine) - Connection has taken more than 2000 ms to send complete protocol header.  Closing as possible DoS.
	 * 2020-03-12 07:22:36,814 INFO  [IO-/10.36.3.1:33884] (q.m.c.idle_close) - [Broker] CON-1003 : Closed due to inactivity : Protocol header not received within timeout period
	 * DoS is thrown in MultiVersionProtocolEngine#tick().
	 * I believe we have to simulate bad network traffic to achieve this.
	 */
	@Test
	public void createConnectionFailsAfterWaitingDoSLimit() throws JMSException, NamingException, InterruptedException {
		ToStringConsumer serverLogConsumer = new ToStringConsumer();
		localContainer.followOutput(serverLogConsumer, OutputFrame.OutputType.STDOUT);

		SSLContext guestSSLContext = TestKeystoreHelper.sslContext("jks/guest.p12", "jks/truststore.jks");
		IxnContext ixnContext = new IxnContext(getQpidURI(), "onramp", null);
		Connection questConnection = ixnContext.createConnection(guestSSLContext);
		Thread.sleep(11000); //10 seconds is the connection creation to start limit
		try {
			questConnection.start();
			fail("one of the connections should have been terminated on the server due to DoS");
		} catch (Exception e) {
			assertTrue(e instanceof JmsConnectionFailedException);
			String serverLog = serverLogConsumer.toUtf8String();
			assertTrue(serverLog.contains("Closing as possible DoS."));
			e.printStackTrace();
		}
	}
}