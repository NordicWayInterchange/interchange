package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Interchange;
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

	public MockDNSFacade(@Value("${dns.mock-names}") 			String[] dnsMockNames,
						 @Value("${dns.lookup.domain.name}")  	String domainName,
						 @Value("${control.channel.portnr}") 	Integer controlChannelPort,
						 @Value("${message.channel.portnr}") 	Integer messageChannelPort) {
		this.dnsMockNames = dnsMockNames;
		this.domainName = domainName;
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
	}

	@Value("${dns.mock-names}")
	private String [] dnsMockNames;

	@Value("${dns.lookup.domain.name}")
	private String domainName;

	@Value("${control.channel.portnr}")
	private Integer controlChannelPort;

	@Value("${message.channel.portnr}")
	private Integer messageChannelPort;


	// Returns a list that contains only the other interchange server container.
	@Override
	public List<DNSResolvedInterchange> getNeighbours() {

		List<DNSResolvedInterchange> interchanges = new ArrayList<>();
		for (String dnsMockName : dnsMockNames) {
			DNSResolvedInterchange node1 = new DNSResolvedInterchange(dnsMockName, this.domainName, this.controlChannelPort, this.messageChannelPort);
			logger.debug("Mocking interchange {}", node1);
			interchanges.add(node1);
		}
		return interchanges;
	}

	@Override
	public DNSResolvedInterchange resolveInterchange(Interchange neighbour) {
		return new DNSResolvedInterchange(neighbour.getName(), this.domainName, this.controlChannelPort, this.messageChannelPort);
	}


}
