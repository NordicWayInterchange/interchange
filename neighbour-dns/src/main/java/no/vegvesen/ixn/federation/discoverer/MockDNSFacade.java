package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Component
@ConditionalOnProperty(name="dns.type", havingValue = "mock")
public class MockDNSFacade implements DNSFacadeInterface{

	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	private MockDNSProperties dnsProperties;

	@Autowired
	public MockDNSFacade(MockDNSProperties dnsProperties){
		this.dnsProperties = dnsProperties;
	}

	// Returns a list that contains only the other interchange server container.
	@Override
	public List<Interchange> getNeighbours() {

		logger.info("MockDNSProperties: {}", dnsProperties.toString());

		List<Interchange> interchanges = new ArrayList<>();
		for (String dnsMockName : dnsProperties.getMockNames()) {
			Interchange node1 = new Interchange();
			node1.setName(dnsMockName); // IMPORTANT: this name must match the name of the server.
			node1.setCapabilities(new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet()));
			node1.setSubscriptionRequest(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
			node1.setFedIn(new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, Collections.emptySet()));
			node1.setMessageChannelPort(dnsProperties.getMessageChannelPort());
			node1.setControlChannelPort(dnsProperties.getControlChannelPort());
			logger.debug("Mocking interchange {}", node1);
			interchanges.add(node1);
		}

		logger.info("Returning list of interchanges from MockDnsFacade: {}", interchanges.toString());


		return interchanges;
	}


}
