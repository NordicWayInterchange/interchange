package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "subscription_request")
public class SubscriptionRequest {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subreq_generator")
	@SequenceGenerator(name="subreq_generator", sequenceName = "subreq_seq")
	@Column(name="subreq_id")
	private Integer subreq_id;

	public enum SubscriptionRequestStatus{REQUESTED, ESTABLISHED, NO_OVERLAP, TEAR_DOWN, EMPTY, FAILED, UNREACHABLE}

	@Enumerated(EnumType.STRING)
	private SubscriptionRequestStatus status;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "subreq_id_sub", foreignKey = @ForeignKey(name="fk_sub_subreq"))
	private Set<Subscription> subscription;

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

	public void setSubscriptions(Set<Subscription> subscription) {
		this.subscription = subscription;
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
