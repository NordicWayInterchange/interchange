package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.capability.DataTypeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Self {

	private static Logger logger = LoggerFactory.getLogger(Self.class);

	private String name;

	private Set<DataType> localCapabilities = new HashSet<>();

	private Set<DataType> localSubscriptions = new HashSet<>();

	private LocalDateTime lastUpdatedLocalCapabilities;
	private LocalDateTime lastUpdatedLocalSubscriptions;

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

	public Set<DataType> getLocalCapabilities() {
		return localCapabilities;
	}


	public void setLocalCapabilities(Set<DataType> localCapabilities) {
		if (localCapabilities != null) {
			this.localCapabilities.addAll(localCapabilities);
		}
	}

	public Set<DataType> getLocalSubscriptions() {
		return localSubscriptions;
	}

	public void setLocalSubscriptions(Set<DataType> localSubscriptions) {
		if(localSubscriptions != null){
			this.localSubscriptions = localSubscriptions;
		}
	}

	public LocalDateTime getLastUpdatedLocalCapabilities() {
		return lastUpdatedLocalCapabilities;
	}

	public void setLastUpdatedLocalCapabilities(LocalDateTime lastUpdatedLocalCapabilities) {
		this.lastUpdatedLocalCapabilities = lastUpdatedLocalCapabilities;
	}

	public LocalDateTime getLastUpdatedLocalSubscriptions() {
		return lastUpdatedLocalSubscriptions;
	}

	public void setLastUpdatedLocalSubscriptions(LocalDateTime lastUpdatedLocalSubscriptions) {
		this.lastUpdatedLocalSubscriptions = lastUpdatedLocalSubscriptions;
	}

	public static Set<Subscription> calculateCustomSubscriptionForNeighbour(Neighbour neighbour, Set<DataType> localSubscriptions) {
		logger.info("Calculating custom subscription for neighbour: {}", neighbour.getName());
		Set<DataType> neighbourCapsDataTypes = neighbour.getCapabilities().getDataTypes();
		Set<Subscription> calculatedSubscriptions = DataTypeMatcher.calculateCommonInterest(localSubscriptions, neighbourCapsDataTypes)
				.stream()
				.map(DataType::toSubscription)
				.collect(Collectors.toSet());
		logger.info("Calculated custom subscription for neighbour {}: {}", neighbour.getName(), calculatedSubscriptions);
		return calculatedSubscriptions;
	}

	@Override
	public String toString() {
		return "Self{" +
				", name='" + name + '\'' +
				", localCapabilities=" + localCapabilities +
				", localSubscriptions=" + localSubscriptions +
				", lastUpdatedLocalCapabilities=" + lastUpdatedLocalCapabilities +
				", lastUpdatedLocalSubscriptions=" + lastUpdatedLocalSubscriptions +
				'}';
	}
}
