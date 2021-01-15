package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
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

	@Enumerated(EnumType.STRING)
	private SubscriptionRequestStatus status = SubscriptionRequestStatus.EMPTY;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id", foreignKey = @ForeignKey(name = "fk_sub_subreq"))
	private Set<Subscription> subscription = new HashSet<>();

	private LocalDateTime successfulRequest;

	public SubscriptionRequest() {
	}

	public SubscriptionRequest(SubscriptionRequestStatus status, Set<Subscription> subscription) {
		this.status = status;
		this.subscription = subscription;
	}

	public SubscriptionRequestStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionRequestStatus status) {
		this.status = status;
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

	public void deleteSubscription (Integer subscriptionId) {
		Subscription subscriptionToRemove = getSubscriptionById(subscriptionId);
		subscription.remove(subscriptionToRemove);
	}

	public void addNewSubscriptions (Set<Subscription> newSubscriptions) {
		subscription.addAll(newSubscriptions);
	}

	public Subscription getSubscriptionById(Integer id) throws SubscriptionNotFoundException {

		for (Subscription subscription : subscription) {
			if (subscription.getId().equals(id)) {
				return subscription;
			}
		}

		throw new SubscriptionNotFoundException("Could not find subscription with id " + id);
	}

	@Override
	public String toString() {
		return "SubscriptionRequest{" +
				"subreq_id=" + subreq_id +
				", status=" + status +
				", subscription=" + subscription +
				'}';
	}

	public Set<Subscription> getAcceptedSubscriptions() {
		return getSubscriptions().stream()
				.filter(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.ACCEPTED))
				.collect(Collectors.toSet());
	}

	public Set<Subscription> getCreatedSubscriptions() {
		return getSubscriptions().stream()
				.filter(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.CREATED))
				.collect(Collectors.toSet());
	}

	public Optional<LocalDateTime> getSuccessfulRequest() {
		return Optional.ofNullable(successfulRequest);
	}

	public void setSuccessfulRequest(LocalDateTime successfulRequest) {
		this.successfulRequest = successfulRequest;
	}

	public Set<String> wantedBindings() {
		Set<String> wantedBindings = new HashSet<>();
		for (Subscription subscription : getAcceptedSubscriptions()) {
			wantedBindings.add(subscription.bindKey());
		}
		for (Subscription subscription : getCreatedSubscriptions()) {
			wantedBindings.add(subscription.bindKey());
		}
		return wantedBindings;
	}

	public Set<String> getUnwantedBindKeys(Set<String> existingBindKeys) {
		Set<String> wantedBindKeys = this.wantedBindings();
		Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
	}
}
