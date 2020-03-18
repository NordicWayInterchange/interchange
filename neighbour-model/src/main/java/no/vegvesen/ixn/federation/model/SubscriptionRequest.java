package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "subscription_request")
public class SubscriptionRequest {

	private static Logger logger = LoggerFactory.getLogger(SubscriptionRequest.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subreq_generator")
	@SequenceGenerator(name = "subreq_generator", sequenceName = "subreq_seq")
	@Column(name = "subreq_id")
	private Integer subreq_id;

	@Enumerated(EnumType.STRING)
	private SubscriptionRequestStatus status = SubscriptionRequestStatus.EMPTY;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id_sub", foreignKey = @ForeignKey(name = "fk_sub_subreq"))
	private Set<Subscription> subscription = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;


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

	public boolean subscriptionRequestRejected() {
		return subscription.stream().noneMatch(
				s -> s.getSubscriptionStatus() == SubscriptionStatus.CREATED
						|| s.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED
						|| s.getSubscriptionStatus() == SubscriptionStatus.REQUESTED);
	}

	public boolean subscriptionRequestEstablished() {
		return subscription.stream().anyMatch(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.CREATED));
	}

	@Override
	public String toString() {
		return "SubscriptionRequest{" +
				"subreq_id=" + subreq_id +
				", status=" + status +
				", subscription=" + subscription +
				'}';
	}

	public void	setStatusFromSubscriptionStatus() {
		if (getSubscriptions().stream().anyMatch(s -> s.getSubscriptionStatus().equals(SubscriptionStatus.CREATED))) {
			logger.info("At least one subscription in fedIn has status CREATED. Setting status of fedIn to ESTABLISHED");
			this.status = SubscriptionRequestStatus.ESTABLISHED;
		}
		else if (getSubscriptions().stream().noneMatch(s -> s.getSubscriptionStatus() == SubscriptionStatus.CREATED
				|| s.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED
				|| s.getSubscriptionStatus() == SubscriptionStatus.REQUESTED)) {
			logger.info("All subscriptions in neighbour fedIn were rejected. Setting status of fedIn to REJECTED");
			this.status = SubscriptionRequestStatus.REJECTED;
		} else {
			logger.info("Some subscriptions in neighbour fedIn do not have a final status or have not been rejected. Keeping status of fedIn REQUESTED");
			this.status = SubscriptionRequestStatus.REQUESTED;
		}
	}
}
