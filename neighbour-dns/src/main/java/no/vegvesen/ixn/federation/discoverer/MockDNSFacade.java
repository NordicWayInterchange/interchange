package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.DNSResolvedInterchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@ConditionalOnProperty(name="dns.type", havingValue = "mock")
public class MockDNSFacade implements DNSFacadeInterface{

	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	@Value("${dns.mock-names}")
	String [] dnsMockNames;

	@Value("${dns.lookup.domain.name}")
	String domainName;

	// Returns a list that contains only the other interchange server container.
	@Override
	public List<DNSResolvedInterchange> getNeighbours() {

		List<DNSResolvedInterchange> interchanges = new ArrayList<>();
		for (String dnsMockName : dnsMockNames) {
			DNSResolvedInterchange node1 = new DNSResolvedInterchange(dnsMockName, domainName, 8090, 5671);
			logger.debug("Mocking interchange {}", node1);
			interchanges.add(node1);
		}
		return interchanges;
	}


}
