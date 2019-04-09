package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
@ConditionalOnProperty(name="dns.type", havingValue = "mock")
public class MockDNSFacade implements DNSFacadeInterface{

	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	// Returns a list that contains only the other interchange server container.
	@Override
	public List<Interchange> getNeighbours() {

		List<Interchange> interchanges = new ArrayList<>();

		Interchange node1 = new Interchange();
		node1.setName("localhost"); // IMPORTANT: this name must match the name of the server.
		node1.setCapabilities(Collections.emptySet());
		node1.setSubscriptions(Collections.emptySet());
		node1.setFedIn(Collections.emptySet());

		node1.setDomainName("");
		node1.setMessageChannelPort("5672");
		node1.setControlChannelPort("8090");

		interchanges.add(node1);

		return interchanges;
	}


}
