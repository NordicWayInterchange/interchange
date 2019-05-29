package no.vegvesen.ixn.federation.discoverer;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix ="dns")
public class DNSProperties {

	private String lookupInterval;
	private String initialDelay;
	private String type;
	private String controlChannelPort;
	private String domainName;

	private List<String> mockNames;

	public DNSProperties() {
	}

	public DNSProperties(String lookupInterval, String initialDelay, String type, String controlChannelPort, String domainName, List<String> mockNames) {
		this.lookupInterval = lookupInterval;
		this.initialDelay = initialDelay;
		this.type = type;
		this.controlChannelPort = controlChannelPort;
		this.domainName = domainName;
		this.mockNames = mockNames;
	}

	public String getLookupInterval() {
		return lookupInterval;
	}

	public void setLookupInterval(String lookupInterval) {
		this.lookupInterval = lookupInterval;
	}

	public String getInitialDelay() {
		return initialDelay;
	}

	public void setInitialDelay(String initialDelay) {
		this.initialDelay = initialDelay;
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

	public List<String> getMockNames() {
		return mockNames;
	}

	public void setMockNames(List<String> mockNames) {
		this.mockNames = mockNames;
	}

	@Override
	public String toString() {
		return "DNSProperties{" +
				"lookupInterval='" + lookupInterval + '\'' +
				", initialDelay='" + initialDelay + '\'' +
				", type='" + type + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				", domainName='" + domainName + '\'' +
				", mockNames=" + mockNames +
				'}';
	}
}
