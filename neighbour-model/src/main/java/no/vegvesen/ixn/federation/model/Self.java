package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Self {

	private static Logger logger = LoggerFactory.getLogger(Self.class);

	private String name;

	private static final String DEFAULT_MESSAGE_CHANNEL_PORT = "5671";

	private Set<Capability> localCapabilities = new HashSet<>();

	private Set<LocalSubscription> localSubscriptions = new HashSet<>();

	private LocalDateTime lastUpdatedLocalCapabilities;
	private LocalDateTime lastUpdatedLocalSubscriptions;

	private String messageChannelPort;

	public Self(){}

	public Self(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Capability> getLocalCapabilities() {
		return localCapabilities;
	}


	public void setLocalCapabilities(Set<Capability> localCapabilities) {
		if (localCapabilities != null) {
			this.localCapabilities.addAll(localCapabilities);
		}
	}

	public Set<LocalSubscription> getLocalSubscriptions() {
		return localSubscriptions;
	}

	public void setLocalSubscriptions(Set<LocalSubscription> localSubscriptions) {
		if(localSubscriptions != null){
			this.localSubscriptions = localSubscriptions;
		}
	}

	public Optional<LocalDateTime> getLastUpdatedLocalCapabilities() {
		return Optional.ofNullable(lastUpdatedLocalCapabilities);
	}

	public void setLastUpdatedLocalCapabilities(LocalDateTime lastUpdatedLocalCapabilities) {
		this.lastUpdatedLocalCapabilities = lastUpdatedLocalCapabilities;
	}

	public Optional<LocalDateTime> getLastUpdatedLocalSubscriptions() {
		return Optional.ofNullable(lastUpdatedLocalSubscriptions);
	}

	public void setLastUpdatedLocalSubscriptions(LocalDateTime lastUpdatedLocalSubscriptions) {
		this.lastUpdatedLocalSubscriptions = lastUpdatedLocalSubscriptions;
	}

	public String getMessageChannelPort() { return this.messageChannelPort; }

	public void setMessageChannelPort(String messageChannelPort) {
		this.messageChannelPort = messageChannelPort;
	}

	public String getMessageChannelUrl() {
		try {
			if (this.getMessageChannelPort() == null || this.getMessageChannelPort().equals(DEFAULT_MESSAGE_CHANNEL_PORT)) {
				return String.format("amqps://%s/", name);
			} else {
				return String.format("amqps://%s:%s/", name, this.getMessageChannelPort());
			}
		} catch (NumberFormatException e) {
			logger.error("Could not create message channel url for interchange {}", this, e);
			throw new DiscoveryException(e);
		}
	}

	@Override
	public String toString() {
		return "Self{" +
				", name='" + name + '\'' +
				", localCapabilities=" + localCapabilities +
				", localSubscriptions=" + localSubscriptions +
				", lastUpdatedLocalCapabilities=" + lastUpdatedLocalCapabilities +
				", lastUpdatedLocalSubscriptions=" + lastUpdatedLocalSubscriptions +
				", messageChannelPort=" + messageChannelPort +
				'}';
	}
}
