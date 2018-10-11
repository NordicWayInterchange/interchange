package no.vegvesen.ixn.qpid;

import no.vegvesen.ixn.MessagingClient;
import no.vegvesen.ixn.model.DispatchMessage;
import org.junit.Before;
import org.junit.Test;

public class TestOnrampMessages {
	private MessagingClient messagingClient;

	@Before
	public void setUp() throws Exception {
		System.setProperty("USER", "guest");
		System.setProperty("PASSWORD", "guest");
		messagingClient = new QpidMessagingClient();
	}

	@Test
	public void one() throws Exception {
		messagingClient.send(new DispatchMessage(null, "fisk"));
	}

	@Test
	public void hundred() throws Exception {
		for (int i=0; i< 100; i++) {
			messagingClient.send(new DispatchMessage(null, "fisk-" + i));
		}
	}

	@Test
	public void thousand() throws Exception{
		for (int i=0; i< 1000; i++) {
			messagingClient.send(new DispatchMessage(null, "fisk-" + i));
		}
	}

	@Test
	public void million() throws Exception{
		for (int i=0; i< 1000000; i++) {
			messagingClient.send(new DispatchMessage(null, "fisk-" + i));
		}
	}

}
