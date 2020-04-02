package no.vegvesen.ixn.federation.model;


import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;

import javax.persistence.*;

//TODO selector til egen klasse?
@Entity
@Table(name = "subscriptions")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name = "sub_generator", sequenceName = "sub_seq")
	@Column(name = "sub_id")
	private Integer sub_id;

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscriptionStatus;

	private String selector;

	private String path;

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

	public Integer getId() {
		return sub_id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getNumberOfPolls() {
		return numberOfPolls;
	}

	public void setNumberOfPolls(int numberOfPolls) {
		this.numberOfPolls = numberOfPolls;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (this == o) return true;
		if (!(o instanceof Subscription)) return false;

		Subscription that = (Subscription) o;

		return selector.equals(that.selector);
	}

	@Override
	public int hashCode() {
		return selector.hashCode();
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

	public String bindKey() {
		return "" + selector.hashCode();
	}
}
