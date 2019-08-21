package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

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

		List<Neighbour> neighbours = new ArrayList<>();

		// TODO: get control channel port nr from separate SRV lookup.

		try {
			if (dnsProperties.getDomainName() == null || dnsProperties.getDomainName().isEmpty()) {
				throw new RuntimeException("DNS lookup with no domain");
			}
			// SRV record lookup for message chanel port on each sub domain
			String srvLookupString = "_ixn._tcp." + dnsProperties.getDomainName();

			Record[] records = new Lookup(srvLookupString, Type.SRV).run();

			if (records == null) {
				throw new RuntimeException("DNS lookup failed. Returned SRV records for " + srvLookupString + " was null.");
			}

			for (Record record : records) {

				SRVRecord srv = (SRVRecord) record;
				String domainName = srv.getTarget().toString();
				String messageChannelPort = String.valueOf(srv.getPort());

				Neighbour neighbour = new Neighbour();
				neighbour.setName(domainName.substring(0, domainName.length()-1));
				neighbour.setMessageChannelPort(messageChannelPort);
				neighbour.setControlChannelPort(dnsProperties.getControlChannelPort());

				neighbours.add(neighbour);
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(neighbour);
				logger.debug("DNS lookup gave Neighbour: \n" + json);
			}

		} catch (Exception e) {
			logger.error("Error in DNSFacade", e);
		}

		return neighbours;
	}

}
