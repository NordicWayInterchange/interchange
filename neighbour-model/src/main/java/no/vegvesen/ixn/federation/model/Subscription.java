package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "subscriptions")
public class Subscription {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name = "sub_generator", sequenceName = "sub_seq")
	@Column(name = "sub_id")
	private Integer sub_id;

	public enum SubscriptionStatus {REQUESTED, ACCEPTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, GIVE_UP, FAILED, UNREACHABLE, REJECTED}

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscriptionStatus;

	private String selector;

	private String path;

	@JsonIgnore
	private int numberOfPolls = 0;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public SubscriptionStatus getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(SubscriptionStatus status) {
		this.subscriptionStatus = status;
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

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonIgnore
	public int getNumberOfPolls() {
		return numberOfPolls;
	}

	public void setNumberOfPolls(int numberOfPolls) {
		this.numberOfPolls = numberOfPolls;
	}

	@Override
	public String toString() {
		return "Subscription{" +
				"sub_id=" + sub_id +
				", subscriptionStatus=" + subscriptionStatus +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				'}';
	}
}
