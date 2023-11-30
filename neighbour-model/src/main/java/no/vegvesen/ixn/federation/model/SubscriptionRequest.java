package no.vegvesen.ixn.federation.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "subscription_request")
public class SubscriptionRequest {

	private static final Logger logger = LoggerFactory.getLogger(SubscriptionRequest.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subreq_seq")
	@Column(name = "id")
	private Integer subreq_id;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id", foreignKey = @ForeignKey(name = "fk_sub_subreq"))
	private Set<Subscription> subscription = new HashSet<>();

	private LocalDateTime successfulRequest;

	public SubscriptionRequest() {
	}

	public SubscriptionRequest(Set<Subscription> subscription) {
		this.subscription = subscription;
	}

	public Set<Subscription> getSubscriptions() {
		return subscription;
	}

	public void setSubscriptions(Set<Subscription> newSubscription) {
		this.subscription.clear();
		if (newSubscription != null) {
			this.subscription.addAll(newSubscription);
		}
	}

	public void deleteSubscriptions(Set<Subscription> subscriptionsToDelete) {
		subscription.removeAll(subscriptionsToDelete);
	}

	public void addNewSubscriptions (Set<Subscription> newSubscriptions) {
		subscription.addAll(newSubscriptions);
	}

	@Override
	public String toString() {
		return "SubscriptionRequest{" +
				"subreq_id=" + subreq_id +
				", subscription=" + subscription +
				'}';
	}

	public Set<Subscription> getSubscriptionsByStatus(SubscriptionStatus status) {
		return getSubscriptions().stream()
				.filter(s -> s.getSubscriptionStatus().equals(status))
				.collect(Collectors.toSet());
	}

	public Optional<LocalDateTime> getSuccessfulRequest() {
		return Optional.ofNullable(successfulRequest);
	}

	public void setSuccessfulRequest(LocalDateTime successfulRequest) {
		this.successfulRequest = successfulRequest;
	}

}
