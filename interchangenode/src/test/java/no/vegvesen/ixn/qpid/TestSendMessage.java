package no.vegvesen.ixn.qpid;

import no.vegvesen.ixn.MessagingClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jms.JMSException;
import javax.naming.NamingException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestSendMessage {

	private MessagingClient messagingClient;

	@BeforeEach
	void setUp() throws Exception {
		System.setProperty("USER", "guest");
		System.setProperty("PASSWORD", "guest");
		messagingClient = new QpidMessagingClient();
	}


	@Test
	void sendFiskToQueueOnrampAndWaitForReception() throws NamingException, JMSException {
		messagingClient.send("onramp", "fisk");
		assertEquals("fisk", messagingClient.receive("onramp"));
	}

}
