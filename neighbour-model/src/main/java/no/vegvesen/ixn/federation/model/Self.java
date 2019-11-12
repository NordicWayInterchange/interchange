package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.exceptions.InvalidSelectorException;
import no.vegvesen.ixn.federation.exceptions.SelectorAlwaysTrueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name="self", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_self_name"))
public class Self {

	private static Logger logger = LoggerFactory.getLogger(Self.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "self_generator")
	@SequenceGenerator(name = "self_generator", sequenceName = "self_seq")
	@Column(name = "self_id")
	private Integer self_id;
	
	private String name;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "self_id_dat", foreignKey = @ForeignKey(name="fk_dat_self"))
	private Set<DataType> localCapabilities = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "self_id_sub", foreignKey = @ForeignKey(name="fk_sub_self"))
	private Set<Subscription> localSubscriptions = new HashSet<>();

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

	public Set<Subscription> getLocalSubscriptions() {
		return localSubscriptions;
	}

	public void setLocalSubscriptions(Set<Subscription> localSubscriptions) {
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

	public Set<Subscription> calculateCustomSubscriptionForNeighbour(Neighbour neighbour) {
		logger.info("Calculating custom subscription for neighbour: {}", neighbour.getName());
		Set<DataType> neighbourCapsDataTypes = neighbour.getCapabilities().getDataTypes();
		Set<Subscription> localSubscriptions = getLocalSubscriptions();
		Set<Subscription> calculatedSubscriptions = calculateCommonInterest(neighbourCapsDataTypes, localSubscriptions);
		logger.info("Calculated custom subscription for neighbour {}: {}", neighbour.getName(), calculatedSubscriptions);
		return calculatedSubscriptions;

	}

	public Set<Subscription> calculateCommonInterest(Set<DataType> neighbourCapsDataTypes, Set<Subscription> localSubscriptions) {
		Set<Subscription> calculatedSubscriptions = new HashSet<>();
		for (DataType neighbourDataType : neighbourCapsDataTypes) {
			for (Subscription localSubscription : localSubscriptions) {
				try {
					// Trows InvalidSelectorException if selector is invalid or SelectorAlwaysTrueException if selector is always true
					String selector = localSubscription.getSelector();
					logger.info("Matching local subscription {}",selector);
					if (CapabilityMatcher.matches(neighbourDataType, selector)) {

						// Subscription to be returned only has selector set.
						Subscription matchingSubscription = new Subscription();
						matchingSubscription.setSelector(selector);

						calculatedSubscriptions.add(matchingSubscription);
					}
                } catch (InvalidSelectorException | SelectorAlwaysTrueException e) {
					logger.error("Error matching neighbour data type with local subscription. Returning empty set of subscriptions.", e);
                }
			}
		}
		return calculatedSubscriptions;
	}

	public static Set<Subscription> calculateCommonInterestSubscriptions(Set<DataType> neighbourCapsDataTypes, Set<String> localSelectors) {
		Set<Subscription> calculatedSubscriptions = new HashSet<>();
		for (DataType neighbourDataType : neighbourCapsDataTypes) {
			for (String selector : localSelectors) {
				try {
					// Trows InvalidSelectorException if selector is invalid or SelectorAlwaysTrueException if selector is always true
					logger.info("Matching local subscription {}",selector);
					if (CapabilityMatcher.matches(neighbourDataType, selector)) {

						// Subscription to be returned only has selector set.
						Subscription matchingSubscription = new Subscription();
						matchingSubscription.setSelector(selector);

						calculatedSubscriptions.add(matchingSubscription);
					}
                } catch (InvalidSelectorException | SelectorAlwaysTrueException e) {
					logger.error("Error matching neighbour data type with local subscription. Skipping selector {}",selector, e);
                }
			}
		}
		return calculatedSubscriptions;
	}


	@Override
	public String toString() {
		return "Self{" +
				"self_id=" + self_id +
				", name='" + name + '\'' +
				", localCapabilities=" + localCapabilities +
				", localSubscriptions=" + localSubscriptions +
				", lastUpdatedLocalCapabilities=" + lastUpdatedLocalCapabilities +
				", lastUpdatedLocalSubscriptions=" + lastUpdatedLocalSubscriptions +
				'}';
	}
}
