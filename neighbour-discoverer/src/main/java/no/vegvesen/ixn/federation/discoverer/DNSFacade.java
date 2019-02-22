package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Model.Capability;
import no.vegvesen.ixn.federation.model.Model.DataType;
import no.vegvesen.ixn.federation.model.Model.Interchange;
import no.vegvesen.ixn.federation.model.Model.Subscription;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DNSFacade {

	// Return a list of interchanges  (Neighbours) with the properties hostname and port nr set.
	// Preparation for DNS neighbour discovery.

	public List<Interchange> getNeighbours(){

		Interchange ixnC = new Interchange();
		ixnC.setHostname("http://localhost");
		ixnC.setPortNr("8090");

		DataType dataTypeCapability = new DataType("datex2", "1.0", Collections.singleton("works"));
		Capability capability = new Capability("NO", dataTypeCapability);
		ixnC.setCapabilities(Collections.singleton(capability));

		DataType dataTypeSubscription = new DataType("datex2", "1.0", Collections.singleton("conditions"));
		Subscription subscription = new Subscription("SE", dataTypeSubscription, "", "");
		ixnC.setSubscriptions(Collections.singleton(subscription));

		ixnC.setName("ixn-c");

		return Collections.singletonList(ixnC);
	}

}
