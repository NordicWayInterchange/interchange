package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.model.Interchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;


public class DNSResolvedInterchange extends Interchange {

	private static final int DEFAULT_CONTROL_CHANNEL_PORT = 443;
	private static final String DEFAULT_CONTROL_CHANNEL_PROTOCOL = "https";
	private static final int DEFAULT_MESSAGE_CHANNEL_PORT = 5671;

	private static Logger logger = LoggerFactory.getLogger(DNSResolvedInterchange.class);

	private int messageChannelPort;
	private int controlChannelPort;

	public DNSResolvedInterchange(Interchange interchange) {
		super(interchange.getName(), interchange.getCapabilities(), interchange.getSubscriptionRequest(), interchange.getFedIn());
		this.messageChannelPort = -1;
		this.controlChannelPort = -1;
	}

	public DNSResolvedInterchange(String name, String domain, int controlChannelPort, int messageChannelPort) {
		super(name, domain);
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
	}

	public DNSResolvedInterchange(Interchange neighbour, Integer controlChannelPort, Integer messageChannelPort) {
		super(neighbour.getName(), neighbour.getDomainName());
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
	}

	Integer getMessageChannelPort() {
		return messageChannelPort;
	}

	Integer getControlChannelPort() {
		return controlChannelPort;
	}

	@Override
	public String toString() {
		return "ResolvedInterchange{" +
				", name='" + this.getName() + '\'' +
				", domainName='" + this.getDomainName() + '\'' +
				", messageChannelPort='" + messageChannelPort + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				'}';
	}


	private String getFullDomainName() {
		StringBuilder fullDomainName = new StringBuilder(this.getName());
		if (this.getDomainName() != null && this.getDomainName().length() > 0) {
			fullDomainName.append(".").append(this.getDomainName());
		}
		return fullDomainName.toString();
	}

	String getControlChannelUrl(String file) {
		if (!file.startsWith("/")) {
			throw new DiscoveryException("Path to discover other node must start with \"/\"");
		}
		try {
			if (this.getControlChannelPort() == -1 || this.getControlChannelPort() == DEFAULT_CONTROL_CHANNEL_PORT) {
				return new URL(DEFAULT_CONTROL_CHANNEL_PROTOCOL, this.getFullDomainName(), file)
						.toExternalForm();
			} else {
				return new URL(DEFAULT_CONTROL_CHANNEL_PROTOCOL, this.getFullDomainName(), this.getControlChannelPort(), file)
						.toExternalForm();
			}
		} catch (NumberFormatException | MalformedURLException e) {
			logger.error("Could not create control channel url for interchange {}", this, e);
			throw new DiscoveryException(e);
		}
	}

	String getMessageChannelUrl() {
		try {
			if (this.getMessageChannelPort() == -1 || this.getMessageChannelPort() == DEFAULT_MESSAGE_CHANNEL_PORT) {
				return String.format("amqps://%s/", this.getFullDomainName());
			} else {
				return String.format("amqps://%s:%s/", this.getFullDomainName(), this.getMessageChannelPort());
			}
		} catch (NumberFormatException e) {
			logger.error("Could not create message channel url for interchange {}", this, e);
			throw new DiscoveryException(e);
		}
	}
}
