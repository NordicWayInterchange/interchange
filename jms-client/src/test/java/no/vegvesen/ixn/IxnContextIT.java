package no.vegvesen.ixn;

import eu.rekawek.toxiproxy.model.ToxicDirection;
import no.vegvesen.ixn.docker.DockerBaseIT;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.ToStringConsumer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("rawtypes")
public class IxnContextIT extends DockerBaseIT {

	private static Logger logger = LoggerFactory.getLogger(IxnContextIT.class);

	@ClassRule
	public static Network network = Network.newNetwork();

	@ClassRule
	public static GenericContainer localContainer = getQpidContainer("qpid", "jks", "localhost.crt", "localhost.crt", "localhost.key")
			.withNetwork(network);

	@ClassRule
	public static ToxiproxyContainer toxiproxy = new ToxiproxyContainer()
			.withNetwork(network);
	final ToxiproxyContainer.ContainerProxy proxy = toxiproxy.getProxy(localContainer, AMQPS_PORT);


	private String getQpidURI() {
		final String ipAddressViaToxiproxy = proxy.getContainerIpAddress();
		final int portViaToxiproxy = proxy.getProxyPort();
		String url = String.format("amqps://%s:%d", ipAddressViaToxiproxy, portViaToxiproxy);
		logger.info("connection string to local message broker {}", url);
		return url;
	}

	/**
	 * This test triggers a DoS recognition on the server to avoid too many open connections.
	 * 2020-03-12 10:22:56,500 WARN  [IO-/10.166.15.234:58076] (o.a.q.s.t.AbstractAMQPConnection) - Connection has taken more than 10000 ms to establish.  Closing as possible DoS.
	 * 2020-03-12 10:22:56,500 INFO  [IO-/10.166.15.234:58076] (q.m.c.idle_close) - [Broker] CON-1003 : Closed due to inactivity : Protocol connection is not established within timeout period
	 */
	@Test
	public void createConnectionFailsAfterWaitingDoSLimit() throws JMSException, NamingException, InterruptedException {
		ToStringConsumer serverLogConsumer = new ToStringConsumer();
		localContainer.followOutput(serverLogConsumer, OutputFrame.OutputType.STDOUT);

		SSLContext guestSSLContext = TestKeystoreHelper.sslContext("jks/guest.p12", "jks/truststore.jks");
		IxnContext ixnContext = new IxnContext(getQpidURI(), "onramp", null);
		Connection guestConnection = ixnContext.createConnection(guestSSLContext);
		Thread.sleep(11000); //DoS limit between create and start of connection interval 10 seconds
		try {
			guestConnection.start();
			logger.error(serverLogConsumer.toUtf8String());
			fail("the connections should have been terminated on the server due to DoS");
		} catch (Exception e) {
			String serverLog = serverLogConsumer.toUtf8String();
			logger.debug(serverLog);
			assertTrue(serverLog.contains("Closing as possible DoS."));
			assertTrue(serverLog.contains("AbstractAMQPConnection"));
			logger.debug("The test experienced the connection problem as expected", e);
		}
	}

	/**
	 * This test triggers a DoS recognition on the server to avoid too many open connections.
	 * The situation we are trying to reproduce is the one from MultiVersionProtocolEngine.
	 * We have to simulate bad network traffic to achieve this.
	 * 2020-03-12 07:22:36,813 WARN  [IO-/10.36.3.1:33884] (o.a.q.s.t.MultiVersionProtocolEngine) - Connection has taken more than 2000 ms to send complete protocol header.  Closing as possible DoS.
	 * 2020-03-12 07:22:36,814 INFO  [IO-/10.36.3.1:33884] (q.m.c.idle_close) - [Broker] CON-1003 : Closed due to inactivity : Protocol header not received within timeout period
	 * DoS is thrown in MultiVersionProtocolEngine#tick().
	 */
	@Test
	public void createConnectionFailsWithDoSRecognitionDueToBadNetworkTraffic() throws NamingException, IOException {
		ToStringConsumer serverLogConsumer = new ToStringConsumer();
		localContainer.followOutput(serverLogConsumer, OutputFrame.OutputType.STDOUT);

		SSLContext guestSSLContext = TestKeystoreHelper.sslContext("jks/guest.p12", "jks/truststore.jks");
		IxnContext ixnContext = new IxnContext(getQpidURI(), "onramp", null);
		proxy.toxics().latency("down", ToxicDirection.DOWNSTREAM, 1999); // push the DoS limit on 2000
		proxy.toxics().latency("up", ToxicDirection.UPSTREAM, 1999);
		logger.info(serverLogConsumer.toUtf8String());
		try {
			Set<Connection> guestConnections = new HashSet<>();
			for (int i = 0; i < 4; i++) {
				Connection guestConnection = ixnContext.createConnection(guestSSLContext);
				guestConnection.start();
				guestConnections.add(guestConnection);
			}
			logger.error(serverLogConsumer.toUtf8String());
			fail("the connections should have been terminated on the server due to DoS");
		} catch (Exception e) {
			String serverLog = serverLogConsumer.toUtf8String();
			logger.debug(serverLog);
			assertTrue(serverLog.contains("Closing as possible DoS."));
			assertTrue(serverLog.contains("MultiVersionProtocolEngine"));
			logger.debug("The test experienced the connection problem as expected", e);
		}
	}
}