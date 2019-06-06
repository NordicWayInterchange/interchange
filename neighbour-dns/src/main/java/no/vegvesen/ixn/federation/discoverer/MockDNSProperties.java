package no.vegvesen.ixn.federation.discoverer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix ="dns.mock")
public class MockDNSProperties {

	private String controlChannelPort = "8090";
	private String messageChannelPort = "5671";
	private String domainName = null;
	private List<String> mockNames;

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
