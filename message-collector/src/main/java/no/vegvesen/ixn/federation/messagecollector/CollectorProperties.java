package no.vegvesen.ixn.federation.messagecollector;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="collector")
public class CollectorProperties {

	/**
	 * Amqp message port on the local node.
	 */
	private String localIxnFederationPort = "5671";

	/**
	 * Time, in milliseconds, how often the message collector service will look for new or existing neighbours to connect or reconnect to.<br/>
	 * Is used in scheduling settings, and must be provided as an application property, no default value.
	 */
	private String fixeddelay;

	public CollectorProperties() {
	}

	public String getLocalIxnFederationPort() {
		return localIxnFederationPort;
	}

	public void setLocalIxnFederationPort(String localIxnFederationPort) {
		this.localIxnFederationPort = localIxnFederationPort;
	}

	public String getFixeddelay() {
		return fixeddelay;
	}

	public void setFixeddelay(String fixeddelay) {
		this.fixeddelay = fixeddelay;
	}

	@Override
	public String toString() {
		return "CollectorProperties{" +
				", localIxnFederationPort='" + localIxnFederationPort + '\'' +
				", fixeddelay='" + fixeddelay + '\'' +
				'}';
	}
}
