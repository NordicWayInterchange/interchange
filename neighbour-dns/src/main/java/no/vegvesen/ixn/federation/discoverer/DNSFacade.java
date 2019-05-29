package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.Interchange;
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
	private String controlChannelPortnr;
	private String messageChannelPortnr;
	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	public DNSFacade(@Value("${dns.lookup.domain.name}") String domain,
					 @Value("${control.channel.portnr}") String controlChannelPortnr,
					 @Value("${message.channel.portnr}") String messageChannelPortnr){

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
	public List<Interchange> getNeighbours() {

		List<Interchange> interchanges = new ArrayList<>();

		try {
			// SRV record lookup on each sub domain
			Record[] records = new Lookup("_ixn._tcp" + domain, Type.SRV).run();
			for (Record record : records) {

				SRVRecord srv = (SRVRecord) record;
				logger.debug("Record: " + srv.toString());

				Interchange interchange = new Interchange();
				String target = srv.getTarget().toString();

				int lengthDomain = target.indexOf(domain);
				interchange.setName(target.substring(0, lengthDomain));
				interchange.setControlChannelPort(controlChannelPortnr);
				interchange.setMessageChannelPort(messageChannelPortnr);
				interchange.setDomainName(domain);

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
