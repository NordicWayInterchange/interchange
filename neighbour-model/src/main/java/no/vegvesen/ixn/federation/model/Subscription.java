package no.vegvesen.ixn.federation.model;


import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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

	@Column(columnDefinition="TEXT")
	private String consumerCommonName;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "end_id", foreignKey = @ForeignKey(name = "fk_end_sub"))
	private Set<Endpoint> endpoints = new HashSet<>();

	private long lastUpdatedTimestamp;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus, String consumerCommonName) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
		this.consumerCommonName = consumerCommonName;
	}

	public Subscription(int id, SubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.consumerCommonName = consumerCommonName;
	}

	public Subscription(int id, SubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName, Set<Endpoint> endpoints) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.consumerCommonName = consumerCommonName;
		this.endpoints.addAll(endpoints);
	}

	public Subscription(SubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName, Set<Endpoint> endpoints) {
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.consumerCommonName = consumerCommonName;
		this.endpoints.addAll(endpoints);
	}

	public Subscription(SubscriptionStatus subscriptionStatus, String selector, String path, String consumerCommonName) {
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.consumerCommonName = consumerCommonName;
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

	public String getConsumerCommonName() {
		return consumerCommonName;
	}

	public void setConsumerCommonName(String consumerCommonName) {
		this.consumerCommonName = consumerCommonName;
	}

	public long getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

	public Set<Endpoint> getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(Set<Endpoint> newEndpoints) {
		if (newEndpoints != null) {
			endpoints.retainAll(newEndpoints);
			endpoints.addAll(newEndpoints);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Subscription)) return false;
		Subscription that = (Subscription) o;
		return selector.equals(that.selector) &&
				consumerCommonName.equals(that.consumerCommonName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(selector, consumerCommonName);
	}

	@Override
	public String toString() {
		return "Subscription{" +
				"id=" + id +
				", subscriptionStatus=" + subscriptionStatus +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				", numberOfPolls=" + numberOfPolls +
				", consumerCommonName='" + consumerCommonName + '\'' +
				", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
				", endpoints=" + endpoints +
				'}';
	}

	public String bindKey() {
		return "" + selector.hashCode();
	}

	public void incrementNumberOfPolls() {
		numberOfPolls++;
	}
}
