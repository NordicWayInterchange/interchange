package no.vegvesen.ixn.federation.model;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

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



	@Column(columnDefinition = "boolean default false")
	private boolean createNewQueue = false;

	private String queueConsumerUser;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "brok_id", foreignKey = @ForeignKey(name = "fk_brok_sub"))
	private Set<Broker> brokers = new HashSet<>();

	private long lastUpdatedTimestamp;

	private String eTag;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
	}

	public Subscription(int id, SubscriptionStatus subscriptionStatus, String selector, String path, boolean createNewQueue, String queueConsumerUser) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.createNewQueue = createNewQueue;
		this.queueConsumerUser = queueConsumerUser;
	}

	public Subscription(String selector, SubscriptionStatus subscriptionStatus, boolean createNewQueue, String queueConsumerUser) {
		this.selector = selector;
		this.subscriptionStatus = subscriptionStatus;
		this.createNewQueue = createNewQueue;
		this.queueConsumerUser = queueConsumerUser;
	}

	public Subscription(int id, SubscriptionStatus subscriptionStatus, String selector, String path, String brokerUrl, String queue, boolean createNewQueue, String queueConsumerUser) {
		this.id = id;
		this.subscriptionStatus = subscriptionStatus;
		this.selector = selector;
		this.path = path;
		this.createNewQueue = createNewQueue;
		this.queueConsumerUser = queueConsumerUser;
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

	//public void setBrokerUrl(String brokerUrl) { this.brokerUrl = brokerUrl; }

	//public String getBrokerUrl() { return brokerUrl; }

	//public void setQueue(String queue) { this.queue = queue; }

	//public String getQueue() { return queue; }

	public boolean isCreateNewQueue() {
		return createNewQueue;
	}

	public void setCreateNewQueue(boolean createNewQueue) {
		this.createNewQueue = createNewQueue;
	}

	public String getQueueConsumerUser() {
		return queueConsumerUser;
	}

	public void setQueueConsumerUser(String queueConsumerUser) {
		this.queueConsumerUser = queueConsumerUser;
	}

	public long getLastUpdatedTimestamp() {
		return lastUpdatedTimestamp;
	}

	public void setLastUpdatedTimestamp(long lastUpdatedTimestamp) {
		this.lastUpdatedTimestamp = lastUpdatedTimestamp;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public Set<Broker> getBrokers() {
		return brokers;
	}

	public void setBrokers(Set<Broker> newBrokers) {
		this.brokers.clear();
		if (newBrokers != null) {
			this.brokers.addAll(newBrokers);
		}
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
				"id=" + id +
				", subscriptionStatus=" + subscriptionStatus +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				", numberOfPolls=" + numberOfPolls +
				//", brokerUrl='" + brokerUrl + '\'' +
				//", queue='" + queue + '\'' +
				", createNewQueue='" + createNewQueue + '\'' +
				", queueConsumerUser='" + queueConsumerUser + '\'' +
				", lastUpdatedTimestamp=" + lastUpdatedTimestamp +
				", eTag='" + eTag + '\'' +
				", brokers=" + brokers +
				'}';
	}

	public String bindKey() {
		return "" + selector.hashCode();
	}
}
