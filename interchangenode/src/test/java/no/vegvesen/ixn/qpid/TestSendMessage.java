package no.vegvesen.ixn.qpid;

import no.vegvesen.ixn.MessagingClient;
import no.vegvesen.ixn.model.DispatchMessage;
import org.junit.Before;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.naming.NamingException;
import java.util.HashMap;
import java.util.Map;

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
		Map<String,String> properties = new HashMap<>();
		properties.put("lat", "10");
		properties.put("lon", "60");
		String body = "fisk";
		DispatchMessage dispatchMessage = new DispatchMessage(properties, body);
		messagingClient.send(dispatchMessage);
		TextMessage received = messagingClient.receive();
		assertEquals("fisk", received.getText());
	}

}
