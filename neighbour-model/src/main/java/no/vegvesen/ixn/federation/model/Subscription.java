package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name="sub_generator", sequenceName = "sub_seq", allocationSize=50)
	@Column(name="sub_id")
	private Integer sub_id;

	public enum SubscriptionStatus {REQUESTED, CREATED, REJECTED}

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscriptionStatus;

	@Column
	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	private String selector;

	public Subscription(){}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public SubscriptionStatus getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@JsonIgnore
	public Integer getId() {
		return sub_id;
	}


}
