package no.vegvesen.ixn.federation.model;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Self {

	private static Logger logger = LoggerFactory.getLogger(Self.class);

	private Set<Capability> localCapabilities = new HashSet<>();

	private Set<LocalSubscription> localSubscriptions = new HashSet<>();

	private LocalDateTime lastUpdatedLocalCapabilities;
	private LocalDateTime lastUpdatedLocalSubscriptions;

	public Self(){}

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

	@Override
	public String toString() {
		return "Self{" +
				", localCapabilities=" + localCapabilities +
				", localSubscriptions=" + localSubscriptions +
				", lastUpdatedLocalCapabilities=" + lastUpdatedLocalCapabilities +
				", lastUpdatedLocalSubscriptions=" + lastUpdatedLocalSubscriptions +
				'}';
	}
}
