package no.vegvesen.ixn.federation.model;


import javax.persistence.*;

@Entity
@Table(name = "subscriptions")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_seq")
	private Integer id;

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus subscriptionStatus;

	@Column(columnDefinition="TEXT")
	private String selector;

	private String path;

	private int numberOfPolls = 0;

	private String brokerUrl;

	private String queue;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public Subscription(Integer id, SubscriptionStatus subscriptionStatus, String selector) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
	}

	public Subscription(int id, SubscriptionStatus subscriptionStatus, String selector, String path) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
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
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

	public String getBrokerUrl() { return brokerUrl; }

	public void setQueue(String queue) { this.queue = queue; }

	public String getQueue() { return queue; }

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
				"id=" + id +
				", subscriptionStatus=" + subscriptionStatus +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				", numberOfPolls=" + numberOfPolls +
				", brokerUrl='" + brokerUrl + '\'' +
				", queue='" + queue + '\'' +
				'}';
	}

	public String bindKey() {
		return "" + selector.hashCode();
	}
}
