package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.Interchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "dns.type", havingValue = "prod", matchIfMissing = true)
public class DNSFacade implements DNSFacadeInterface {

	private DNSProperties dnsProperties;
	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	@Autowired
	public DNSFacade(DNSProperties dnsProperties) {
		this.dnsProperties = dnsProperties;
	}

	// Returns a list of interchanges discovered through DNS lookup.
	@Override
	public List<Interchange> getNeighbours() {

		List<Interchange> interchanges = new ArrayList<>();

		// TODO: get control channel port nr from separate SRV lookup.

		try {
			// SRV record lookup for message chanel port on each sub domain
			String srvLookupString = "_ixn._tcp" + dnsProperties.getDomainName();

			Record[] records = new Lookup(srvLookupString, Type.SRV).run();

			if (records == null) {
				throw new RuntimeException("DNS lookup failed. Returned SRV records for " + srvLookupString + " was null.");
			}

			for (Record record : records) {

				SRVRecord srv = (SRVRecord) record;
				String target = srv.getTarget().toString();
				String messageChannelPort = String.valueOf(srv.getPort());
				int lengthDomain = target.indexOf(dnsProperties.getDomainName());

				Interchange interchange = new Interchange();
				interchange.setName(target.substring(0, lengthDomain));
				interchange.setMessageChannelPort(messageChannelPort);
				interchange.setControlChannelPort(dnsProperties.getControlChannelPort());
				interchange.setDomainName(dnsProperties.getDomainName());

				interchanges.add(interchange);
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(interchange);
				logger.debug("DNS lookup gave interchange: \n" + json);
			}

		} catch (Exception e) {
			logger.error("Error in DNSFacade", e);
		}

		return interchanges;
	}

}
