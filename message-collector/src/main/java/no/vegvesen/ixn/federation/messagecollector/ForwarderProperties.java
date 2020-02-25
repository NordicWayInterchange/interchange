package no.vegvesen.ixn.federation.messagecollector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//TODO: generate additional-spring-configuration-metadata.json
//TODO: set default values
@Component
@ConfigurationProperties(prefix ="forwarder")
public class ForwarderProperties {

	private String localIxnDomainName;
	private String localIxnFederationPort;

	public ForwarderProperties() {
	}

	public String getLocalIxnDomainName() {
		return localIxnDomainName;
	}

	public void setLocalIxnDomainName(String localIxnDomainName) {
		this.localIxnDomainName = localIxnDomainName;
	}

	public String getLocalIxnFederationPort() {
		return localIxnFederationPort;
	}

	public void setLocalIxnFederationPort(String localIxnFederationPort) {
		this.localIxnFederationPort = localIxnFederationPort;
	}

	@Override
	public String toString() {
		return "ForwarderProperties{" +
				"localIxnDomainName='" + localIxnDomainName + '\'' +
				", localIxnFederationPort='" + localIxnFederationPort + '\'' +
				'}';
	}
}
