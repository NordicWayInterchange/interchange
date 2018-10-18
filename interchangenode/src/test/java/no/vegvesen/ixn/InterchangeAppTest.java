package no.vegvesen.ixn;

import no.vegvesen.ixn.messaging.IxnMessageProducer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InterchangeAppTest {

	@Mock
	IxnMessageProducer producer;
	private InterchangeApp app;

	@Before
	public void setUp() {
		app = new InterchangeApp(producer);
	}

	@Test
	public void handleOneMessage() throws JMSException {
		TextMessage textMessage = mock(TextMessage.class);
		when(textMessage.getText()).thenReturn("fisk");
		app.receiveMessage(textMessage);
		verify(producer, times(1)).sendMessage(any(), any());
	}

	@Test
	public void  messageWithoutBodyIsDropped() throws JMSException {
		TextMessage textMessage = mock(TextMessage.class);
		when(textMessage.getText()).thenReturn(null);
		app.receiveMessage(textMessage);
		verify(producer, times(0)).sendMessage(any(), any());
	}

}