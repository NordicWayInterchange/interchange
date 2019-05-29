package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.DNSResolvedInterchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name ="dns.type", havingValue = "prod", matchIfMissing = true)
public class DNSFacade implements DNSFacadeInterface {

	private String domain;
	private int controlChannelPortnr;
	private int messageChannelPortnr;
	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	public DNSFacade(@Value("${dns.lookup.domain.name}") String domain,
					 @Value("${control.channel.portnr}") int controlChannelPortnr,
					 @Value("${message.channel.portnr}") int messageChannelPortnr){

		if(!domain.startsWith(".")){
			this.domain = "."+domain;
		}else {
			this.domain = domain;
		}

		this.controlChannelPortnr = controlChannelPortnr;
		this.messageChannelPortnr = messageChannelPortnr;
	}

	// Returns a list of interchanges discovered through DNS lookup.
	@Override
	public List<DNSResolvedInterchange> getNeighbours() {

		List<DNSResolvedInterchange> interchanges = new ArrayList<>();

		try {
			// SRV record lookup on each sub domain
			Record[] records = new Lookup("_ixn._tcp" + domain, Type.SRV).run();
			for (Record record : records) {

				SRVRecord srv = (SRVRecord) record;
				logger.debug("Record: " + srv.toString());

				String target = srv.getTarget().toString();

				int lengthDomain = target.indexOf(domain);
				DNSResolvedInterchange interchange = new DNSResolvedInterchange(target.substring(0, lengthDomain),domain, controlChannelPortnr, messageChannelPortnr);

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
