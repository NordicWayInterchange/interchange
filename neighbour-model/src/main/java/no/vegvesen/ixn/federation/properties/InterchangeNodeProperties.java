package no.vegvesen.ixn.federation.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix ="interchange.node-provider")
public class InterchangeNodeProperties {

	public InterchangeNodeProperties() {
	}

	public InterchangeNodeProperties(String name, String messageChannelPort) {
		this.name = name;
	}

	/**
	 * The fully qualified domain name of the interchange node
	 */
	String name;

	String brokerExternalName;

	/**
	 * The message channel port for amqps
	 */
	String messageChannelPort = "5671";

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBrokerExternalName() {
		return brokerExternalName;
	}

	public void setBrokerExternalName(String brokerExternalName) {
		this.brokerExternalName = brokerExternalName;
	}

	public String getMessageChannelPort() { return messageChannelPort; }

	public void setMessageChannelPort(String messageChannelPort) { this.messageChannelPort = messageChannelPort; }
}
