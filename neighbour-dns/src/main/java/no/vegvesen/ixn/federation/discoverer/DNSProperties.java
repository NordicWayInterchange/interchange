package no.vegvesen.ixn.federation.discoverer;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix ="dns")
public class DNSProperties {

	private String type;
	private String controlChannelPort;
	private String domainName;


	public DNSProperties() {
	}

	public DNSProperties(String type, String controlChannelPort, String domainName, List<String> mockNames) {
		this.type = type;
		this.controlChannelPort = controlChannelPort;
		this.domainName = domainName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
				"type='" + type + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				", domainName='" + domainName + '\'' +
				'}';
	}
}
