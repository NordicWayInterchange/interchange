package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "local_subscription_request")
public class LocalSubscriptionRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locsubreq_generator")
	@SequenceGenerator(name="locsubreq_generator", sequenceName = "locsubreq_seq")
	@Column(name="subreq_id")
	private Integer subreq_id;

	@Enumerated(EnumType.STRING)
	private SubscriptionRequestStatus status = SubscriptionRequestStatus.EMPTY;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id", foreignKey = @ForeignKey(name="fk_dat_subreq"))
	private Set<DataType> subscription = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;


	public LocalSubscriptionRequest(){

	}

	public LocalSubscriptionRequest(SubscriptionRequestStatus status, Set<DataType> subscription) {
		this.status = status;
		this.subscription = subscription;
	}

	public LocalSubscriptionRequest(SubscriptionRequestStatus status, DataType firstSubscription) {
		this.status = status;
		this.subscription = new HashSet<>();
		this.subscription.add(firstSubscription);
	}

	public SubscriptionRequestStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionRequestStatus status) {
		this.status = status;
	}

	public Set<DataType> getSubscriptions() {
		return subscription;
	}

	public void setSubscriptions(Set<DataType> newSubscription) {
		this.subscription.clear();
		if (newSubscription != null) {
			this.subscription.addAll(newSubscription);
		}
	}


	@Override
	public String toString() {
		return "SubscriptionRequest{" +
				"subreq_id=" + subreq_id +
				", status=" + status +
				", subscription=" + subscription +
				'}';
	}

	public void addLocalSubscription(DataType newLocalSubscription) {
		if(subscription.contains(newLocalSubscription)){
			throw new SubscriptionRequestException("The Service Provider subscription already exist. Nothing to add.");
		}
		subscription.add(newLocalSubscription);

	}
}
