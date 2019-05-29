package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.net.MalformedURLException;
import java.net.URL;


@Entity
@Table(name = "interchanges", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_ixn_name"))
public class DNSResolvedInterchange extends Interchange {

	private static final int DEFAULT_CONTROL_CHANNEL_PORT = 443;
	private static final String DEFAULT_CONTROL_CHANNEL_PROTOCOL = "https";
	private static final int DEFAULT_MESSAGE_CHANNEL_PORT = 5671;

	static Logger logger = LoggerFactory.getLogger(DNSResolvedInterchange.class);

	private String domainName;
	private int messageChannelPort;
	private int controlChannelPort;

	public DNSResolvedInterchange(Interchange interchange) {
		super(interchange.getName(), interchange.getCapabilities(), interchange.getSubscriptionRequest(), interchange.getFedIn());
		this.messageChannelPort = -1;
		this.controlChannelPort = -1;
	}

	public DNSResolvedInterchange(Interchange interchange, int messageChannelPort, int controlChannelPort) {
		super(interchange.getName(), interchange.getCapabilities(), interchange.getSubscriptionRequest(), interchange.getFedIn());
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
	}

	public DNSResolvedInterchange(String name, String domain, int controlChannelPort, int messageChannelPort) {
		super(name, domain);
		this.controlChannelPort = controlChannelPort;
		this.messageChannelPort = messageChannelPort;
	}

	public int getMessageChannelPort() {
		return messageChannelPort;
	}

	public void setMessageChannelPort(int messageChannelPort) {
		this.messageChannelPort = messageChannelPort;
	}

	public int getControlChannelPort() {
		return controlChannelPort;
	}

	public void setControlChannelPort(int controlChannelPort) {
		this.controlChannelPort = controlChannelPort;
	}

	@Override
	public String toString() {
		return "ResolvedInterchange{" +
				", name='" + this.getName() + '\'' +
				", domainName='" + domainName + '\'' +
				", messageChannelPort='" + messageChannelPort + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				'}';
	}


	private String getFullDomainName() {
		StringBuilder fullDomainName = new StringBuilder(this.getName());
		if (this.getDomainName() != null) {
			fullDomainName.append(".").append(this.getDomainName());
		}
		return fullDomainName.toString();
	}

	public String getControlChannelUrl(String file) {
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

	public String getMessageChannelUrl() {
		URL url = null;
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
