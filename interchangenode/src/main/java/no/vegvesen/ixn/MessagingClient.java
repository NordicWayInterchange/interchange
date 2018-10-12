package no.vegvesen.ixn;

import no.vegvesen.ixn.model.DispatchMessage;

public interface MessagingClient {
	DispatchMessage receive();
	void close();
	void send(DispatchMessage dispatchMessage);
}
