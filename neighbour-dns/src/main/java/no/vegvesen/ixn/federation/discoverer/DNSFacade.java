package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xbill.DNS.*;

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
	public List<Neighbour> getNeighbours() {
		try {
			if (dnsProperties.getDomainName() == null || dnsProperties.getDomainName().isEmpty()) {
				throw new RuntimeException("DNS lookup with no domain");
			}
			Map<String, String> messageChannelPorts = getSrvRecords("_ixn._tcp.");
			Map<String, String> controlChannelPorts = getSrvRecords("_ixc._tcp.");

			List<Neighbour> neighbours = new LinkedList<>();
			for (String nodeName : messageChannelPorts.keySet()) {
				Neighbour neighbour = new Neighbour();
				neighbour.setName(nodeName);
				neighbour.setMessageChannelPort(messageChannelPorts.get(nodeName));
				if (controlChannelPorts.containsKey(nodeName)) {
					neighbour.setControlChannelPort(controlChannelPorts.get(nodeName));
					logger.debug("DNS server {} has message channel port {} and control channel port {} for node {}",
							getDnsServerName(),
							neighbour.getMessageChannelPort(),
							neighbour.getControlChannelPort(),
							neighbour.getName());
				}
				else {
					neighbour.setControlChannelPort(dnsProperties.getControlChannelPort());
					logger.debug("DNS server {} has only message channel port {} for node, using standard port for control channel {} for node {}",
							getDnsServerName(),
							neighbour.getMessageChannelPort(),
							neighbour.getControlChannelPort(),
							neighbour.getName());
				}
				neighbours.add(neighbour);
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(neighbour);
				logger.debug("DNS lookup in {} gave Neighbour {}", getDnsServerName(), json);
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

		Record[] records = new Lookup(srvLookupString, Type.SRV).run();

		if (records == null) {
			throw new RuntimeException(String.format("DNS lookup in %s failed. Returned SRV records for %s was null.",
					getDnsServerName(), srvLookupString));
		}

		for (Record record : records) {
			SRVRecord srv = (SRVRecord) record;
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
