package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.Type;

import java.util.Collections;
import java.util.List;

@Component
public class DNSFacade {

	// Return a list of interchanges  (Neighbours) with the properties hostname and port nr set.
	// Preparation for DNS neighbour discovery.

	private Logger logger = LoggerFactory.getLogger(DNSFacade.class);

	public List<Interchange> mockGetNeighbours(){
		Interchange ixnC = new Interchange();
		ixnC.setHostname("http://localhost");
		ixnC.setPortNr("8090");

		DataType capability = new DataType("datex2", "1.0", "works");
		ixnC.setCapabilities(Collections.singleton(capability));

		Subscription subscription = new Subscription("SE", "where1 LIKE 'NO'", "", "");
		ixnC.setSubscriptions(Collections.singleton(subscription));

		ixnC.setName("ixn-c");

		return Collections.singletonList(ixnC);

	}

	public List<Interchange> getNeighbours(){

		// TODO: lookups on real addresses.

		try{

			// 1. NAPTR lookup on itsinterchange.eu
			// example naptr address to lookup: 4.4.2.2.3.3.5.6.8.1.4.4.e164.arpa
			// Record[] records = new Lookup("4.4.2.2.3.3.5.6.8.1.4.4.e164.arpa", Type.NAPTR).run();
			// NAPTRRecord srv = (NAPTRRecord) record;

			// 2. SRV record lookup on each sub domain
			Record[] records = new Lookup("_mqtt._tcp.mosquitto.org", Type.SRV).run();

			for (Record record : records) {

				SRVRecord srv = (SRVRecord) record;

				System.out.println("Record: " + srv.toString());
				System.out.println("Port nr: " + srv.getPort());
				System.out.println("Target: " + srv.getTarget().toString());

				Record[] arecords = new Lookup(srv.getTarget().toString(), Type.A).run();

				for(Record rec : arecords){
					System.out.println("IP address: " + rec.rdataToString());

					Interchange interchange = new Interchange();
					interchange.setName(srv.getTarget().toString());
					interchange.setPortNr(""+srv.getPort());
					interchange.setHostname(rec.rdataToString());

					ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = ow.writeValueAsString(interchange);
					System.out.println(json);
				}

			}
			// TODO: return list of interchanges instead of empty list.
			return Collections.emptyList();

		}catch(Exception e){
			logger.info(e.getClass().getName());
			return Collections.emptyList();
		}
	}

}
