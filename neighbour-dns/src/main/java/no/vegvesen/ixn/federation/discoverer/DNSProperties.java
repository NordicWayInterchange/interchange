package no.vegvesen.ixn.federation.discoverer;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="dns")
public class DNSProperties {

	private String type = "prod";
	private String controlChannelPort = "443";
	private String domainName;


	public DNSProperties() {
	}

	public DNSProperties(String type, String controlChannelPort, String domainName) {
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
