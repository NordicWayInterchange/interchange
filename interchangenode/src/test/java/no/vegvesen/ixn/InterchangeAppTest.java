package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.TextMessage;
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
		TextMessage textMessage = mock(TextMessage.class);
		when(textMessage.getText()).thenReturn("fisk");
		when(messagingClient.receive("onramp")).thenReturn(textMessage);
		app.handleOneMessage();
		verify(messagingClient, times(1)).send(argThat(messageToWithBody("test-out", "fisk")));
	}

	private Matcher<DispatchMessage> messageToWithBody(final String outQueue, final String body) {
		return new TypeSafeMatcher<DispatchMessage>() {
			public boolean matchesSafely(DispatchMessage item) {
				return body.equals(item.getBody()) && outQueue.equals(item.getQueue());
			}
			public void describeTo(Description description) {
				description.appendText("a message to " + outQueue + " with body " + body);
			}
		};
	}
}