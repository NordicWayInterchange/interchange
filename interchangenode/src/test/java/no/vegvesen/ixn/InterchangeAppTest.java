package no.vegvesen.ixn;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.naming.NamingException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

	@Mock
	MessagingClient messagingClient;
	private InterchangeApp app;

	@Before
	public void setUp() {
		app = new InterchangeApp(messagingClient);
	}

	@Test
	public void handleOneMessage() throws JMSException, NamingException {
		when(messagingClient.receive("onramp")).thenReturn("fisk");
		app.handleOneMessage();
		verify(messagingClient, times(1)).send("test-out", "fisk");
	}
}