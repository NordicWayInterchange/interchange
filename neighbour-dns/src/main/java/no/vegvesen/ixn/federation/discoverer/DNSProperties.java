package no.vegvesen.ixn.federation.discoverer;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="dns")
public class DNSProperties {

	/**
	 * When discovery of neighbours only finds message port in DNS, this port is selected as the control channel port
	 */
	private String controlChannelPort = "443";

	/**
	 * The domain name where to look for neighbours in the DNS.
	 */
	private String domainName;


	public DNSProperties() {
	}

	public DNSProperties(String controlChannelPort, String domainName) {
		this.controlChannelPort = controlChannelPort;
		this.domainName = domainName;
	}

	public String getControlChannelPort() {
		return controlChannelPort;
	}

	public void setControlChannelPort(String controlChannelPort) {
		this.controlChannelPort = controlChannelPort;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}


	@Override
	public String toString() {
		return "DNSProperties{" +
				", controlChannelPort='" + controlChannelPort + '\'' +
				", domainName='" + domainName + '\'' +
				'}';
	}
}
