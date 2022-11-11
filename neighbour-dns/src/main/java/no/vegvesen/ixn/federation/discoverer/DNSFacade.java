package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.util.*;

@Component
public class DNSFacade {

	private DNSProperties dnsProperties;
	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	@Autowired
	public DNSFacade(DNSProperties dnsProperties) {
		this.dnsProperties = dnsProperties;
	}

	// Returns a list of neighbours discovered through DNS lookup.
	public List<Neighbour> lookupNeighbours() {
		try {
			if (dnsProperties.getDomainName() == null || dnsProperties.getDomainName().isEmpty()) {
				throw new RuntimeException("DNS lookup with no domain");
			}
			System.setProperty("dns.search", dnsProperties.getDomainName());
			Map<String, String> controlChannelPorts = new HashMap<>();
			try {
				controlChannelPorts = getSrvRecords("_ixc._tcp.");
			} catch (RuntimeException e) {
				logger.warn("No control channel ports found in DNS", e);
			}

			List<Neighbour> neighbours = new LinkedList<>();
			for (String nodeName : controlChannelPorts.keySet()) {
				Neighbour neighbour = new Neighbour();
				neighbour.setName(nodeName);
				neighbour.setControlChannelPort(controlChannelPorts.get(nodeName));
				logger.debug("DNS server {} has control channel port {} for node {}",
						getDnsServerName(),
						neighbour.getControlChannelPort(),
						neighbour.getName());
				neighbours.add(neighbour);
			}
			return neighbours;

		} catch (Exception e) {
			logger.error(String.format("Error in DNSFacade %s", getDnsServerName()), e);
		}
		return Collections.emptyList();
	}

	private Map<String, String> getSrvRecords(String srvRecordType) throws TextParseException {
		Map<String, String> srvPorts = new HashMap<>();
		// SRV record lookup for message chanel port on each sub domain
		String srvLookupString = srvRecordType + dnsProperties.getDomainName();
		logger.debug("Looking up {}", srvLookupString);

		Record[] records = new Lookup(srvLookupString, Type.SRV).run();

		if (records == null) {
			throw new RuntimeException(String.format("DNS lookup in %s failed. Returned SRV records for %s was null.",
					getDnsServerName(), srvLookupString));
		}

		for (Record record : records) {
			SRVRecord srv = (SRVRecord) record;
			logger.debug("Got srv record {}", record);
			String domainName = srv.getTarget().toString();

			String neighbourName = domainName.substring(0, domainName.length() - 1);
			srvPorts.put(neighbourName, String.valueOf(srv.getPort()));
		}
		return srvPorts;
	}

	public String getDnsServerName() {
		return this.dnsProperties.getDomainName();
	}

}
