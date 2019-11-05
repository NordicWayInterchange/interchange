package no.vegvesen.ixn.federation.model;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subscription_request")
public class SubscriptionRequest {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subreq_generator")
	@SequenceGenerator(name="subreq_generator", sequenceName = "subreq_seq")
	@Column(name="subreq_id")
	private Integer subreq_id;

	public enum SubscriptionRequestStatus{REQUESTED, ESTABLISHED, NO_OVERLAP, TEAR_DOWN, EMPTY, FAILED, UNREACHABLE, REJECTED, FEDERATED_ACCESS_GRANTED}

	@Enumerated(EnumType.STRING)
	private SubscriptionRequestStatus status = SubscriptionRequestStatus.EMPTY;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id_sub", foreignKey = @ForeignKey(name="fk_sub_subreq"))
	private Set<Subscription> subscription = new HashSet<>();

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;


	public SubscriptionRequest(){

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

	public boolean subscriptionRequestRejected(){
		for(Subscription s : subscription){
			if(s.getSubscriptionStatus() == SubscriptionStatus.CREATED
				|| s.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED
				|| s.getSubscriptionStatus() == SubscriptionStatus.REQUESTED){
				return false;
			}
		}
		return true;
	}

	public boolean subscriptionRequestEstablished(){
		for(Subscription s : subscription){
			if(s.getSubscriptionStatus() == SubscriptionStatus.CREATED){
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "SubscriptionRequest{" +
				"subreq_id=" + subreq_id +
				", status=" + status +
				", subscription=" + subscription +
				'}';
	}
}
