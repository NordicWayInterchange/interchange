package no.vegvesen.ixn.federation.discoverer;

/*-
 * #%L
 * neighbour-dns
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
			System.setProperty("dns.search", dnsProperties.getDomainName());
			Map<String, String> messageChannelPorts = getSrvRecords("_ixn._tcp.");
			Map<String, String> controlChannelPorts = new HashMap<>();
			try {
				controlChannelPorts = getSrvRecords("_ixc._tcp.");
			} catch (RuntimeException e) {
				logger.warn("No control channel ports found in DNS", e);
			}

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
				} else {
					neighbour.setControlChannelPort(dnsProperties.getControlChannelPort());
					logger.debug("DNS server {} has only message channel port {} for node, using standard port for control channel {} for node {}",
							getDnsServerName(),
							neighbour.getMessageChannelPort(),
							neighbour.getControlChannelPort(),
							neighbour.getName());
				}
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
