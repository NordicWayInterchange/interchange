package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.DNSResolvedInterchange;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DNSFacadeInterface {
	// Returns a list of interchanges discovered through DNS lookup.
	List<DNSResolvedInterchange> getNeighbours();
}
