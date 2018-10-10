package no.vegvesen.ixn.qpid;

import no.vegvesen.ixn.MessagingClient;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

public class TestSendMessage {

	private MessagingClient messagingClient;

	@Before
	public void setUp() throws Exception {
		System.setProperty("USER", "guest");
		System.setProperty("PASSWORD", "guest");
		messagingClient = new QpidMessagingClient();
	}


	@Test
	public void sendFiskToQueueOnrampAndWaitForReception() throws NamingException, JMSException {
		messagingClient.send("onramp", "fisk");
		assertEquals("fisk", messagingClient.receive("onramp"));
	}

}
