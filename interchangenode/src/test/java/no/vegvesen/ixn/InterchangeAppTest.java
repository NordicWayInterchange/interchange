package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
	public void handleOneMessage(){
		DispatchMessage textMessage = new DispatchMessage(headerId(), "fisk");
		when(messagingClient.receive()).thenReturn(textMessage);
		app.handleOneMessage();
		verify(messagingClient, times(1)).send(any());
	}

	private Map<String, String> headerId() {
		HashMap<String, String> headersWithId = new HashMap<>();
		headersWithId.put("msgguid", UUID.randomUUID().toString());
		return headersWithId;
	}


	@Test
	public void  messageWithoutBodyIsDropped() {
		DispatchMessage textMessage = new DispatchMessage(headerId(), null);
		when(messagingClient.receive()).thenReturn(textMessage);
		app.handleOneMessage();
		verify(messagingClient, times(0)).send(any());
	}

}