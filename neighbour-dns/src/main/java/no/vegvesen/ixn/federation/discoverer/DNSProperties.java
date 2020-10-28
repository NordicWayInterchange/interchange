package no.vegvesen.ixn.federation.discoverer;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="dns")
public class DNSProperties {

	/**
	 * The domain name where to look for neighbours in the DNS.
	 */
	private String domainName;


	public DNSProperties() {
	}

	public DNSProperties(String domainName) {
		this.domainName = domainName;
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
				//", controlChannelPort='" + controlChannelPort + '\'' +
				", domainName='" + domainName + '\'' +
				'}';
	}
}
