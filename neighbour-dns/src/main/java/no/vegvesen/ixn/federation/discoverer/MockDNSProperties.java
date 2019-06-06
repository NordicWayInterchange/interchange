package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix ="dns.mock")
public class MockDNSProperties {

	private String controlChannelPort;
	private String messageChannelPort;
	private String domainName = null;
	private List<String> mockNames;
/*
	public MockDNSProperties() {
	}

	public MockDNSProperties(String controlChannelPort, String messageChannelPort, String domainName, List<String> mockNames) {
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
		this.domainName = domainName;
		this.mockNames = mockNames;
	}*/

	public String getControlChannelPort() {
		return controlChannelPort;
	}

	public void setControlChannelPort(String controlChannelPort) {
		this.controlChannelPort = controlChannelPort;
	}

	public String getMessageChannelPort() {
		return messageChannelPort;
	}

	public void setMessageChannelPort(String messageChannelPort) {
		this.messageChannelPort = messageChannelPort;
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
		return "MockDNSProperties{" +
				"controlChannelPort='" + controlChannelPort + '\'' +
				", messageChannelPort='" + messageChannelPort + '\'' +
				", domainName='" + domainName + '\'' +
				", mockNames=" + mockNames +
				'}';
	}
}
