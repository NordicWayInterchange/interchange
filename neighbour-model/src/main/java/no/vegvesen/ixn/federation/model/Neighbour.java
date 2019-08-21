package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "neighbours", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_neighbour_name"))
public class Neighbour implements Subscriber {

	private static final String DEFAULT_CONTROL_CHANNEL_PORT = "443";
	private static final String DEFAULT_CONTROL_CHANNEL_PROTOCOL = "https";
	private static final String DEFAULT_MESSAGE_CHANNEL_PORT = "5671";

	private static Logger logger = LoggerFactory.getLogger(Neighbour.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neighbour_generator")
	@SequenceGenerator(name = "neighbour_generator", sequenceName = "neighbour_seq")
	@Column(name = "neighbour_id")
	private Integer neighbour_id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "neighbour_id_cap", referencedColumnName = "cap_id", foreignKey = @ForeignKey(name = "fk_cap_neighbour"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "neighbour_id_sub_out", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_subreq_neighbour_sub_out"))
	private SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "neighbour_id_fed_in", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_subreq_neighbour_fed_in"))
	private SubscriptionRequest fedIn = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@UpdateTimestamp
	private LocalDateTime lastUpdated;
	private LocalDateTime lastSeen;
	private LocalDateTime backoffStart;
	private int backoffAttempts = 0;
	private String messageChannelPort;
	private String controlChannelPort;

	public Neighbour() {
	}

	public Neighbour(String name, Capabilities capabilities, SubscriptionRequest subscriptions, SubscriptionRequest fedIn) {
		this.setName(name);
		this.capabilities = capabilities;
		this.subscriptionRequest = subscriptions;
		this.fedIn = fedIn;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name.endsWith(".")) {
			throw new DiscoveryException(String.format("Server name '%s' shall not end with \".\"", name));
		}
		this.name = name;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	@Override
	public SubscriptionRequest getSubscriptionRequest() {
		return subscriptionRequest;
	}

	@Override
	public void setSubscriptionRequest(SubscriptionRequest subscriptionRequest) {
		this.subscriptionRequest = subscriptionRequest;
	}

	public SubscriptionRequest getFedIn() {
		return fedIn;
	}

	public Subscription getSubscriptionById(Integer id) throws SubscriptionNotFoundException {

		for (Subscription subscription : subscriptionRequest.getSubscriptions()) {
			if (subscription.getId().equals(id)) {
				return subscription;
			}
		}

		throw new SubscriptionNotFoundException("Could not find subscription with id " + id + " on interchange " + name);
	}

	public void setFedIn(SubscriptionRequest fedIn) {
		this.fedIn = fedIn;
	}

	public LocalDateTime getLastSeen() {
		return lastSeen;
	}

	@PreUpdate
	@PrePersist
	public void setLastSeen() {
		this.lastSeen = LocalDateTime.now();
	}

	public String getMessageChannelPort() {
		return messageChannelPort;
	}

	public void setMessageChannelPort(String messageChannelPort) {
		this.messageChannelPort = messageChannelPort;
	}

	public String getControlChannelPort() {
		return controlChannelPort;
	}

	public void setControlChannelPort(String controlChannelPort) {
		this.controlChannelPort = controlChannelPort;
	}

	public LocalDateTime getBackoffStartTime() {
		return backoffStart;
	}

	public void setBackoffStart(LocalDateTime backoffStart) {
		this.backoffStart = backoffStart;
	}

	public int getBackoffAttempts() {
		return backoffAttempts;
	}

	public void setBackoffAttempts(int backoffAttempts) {
		this.backoffAttempts = backoffAttempts;
	}

	public Set<Subscription> getSubscriptionsForPolling() {
		Set<Subscription> subscriptionsForPolling = new HashSet<>();

		for (Subscription subscription : this.getFedIn().getSubscriptions()) {
			if (subscription.getSubscriptionStatus().equals(Subscription.SubscriptionStatus.REQUESTED) || subscription.getSubscriptionStatus().equals(Subscription.SubscriptionStatus.ACCEPTED)) {
				subscriptionsForPolling.add(subscription);
			}
		}

		return subscriptionsForPolling;
	}

	public Set<Subscription> getFailedFedInSubscriptions() {
		Set<Subscription> subscriptionsWithStatusFailed = new HashSet<>();

		for (Subscription subscription : this.getFedIn().getSubscriptions()) {
			if (subscription.getSubscriptionStatus().equals(Subscription.SubscriptionStatus.FAILED)) {
				subscriptionsWithStatusFailed.add(subscription);
			}
		}

		return subscriptionsWithStatusFailed;
	}

	@Override
	public String toString() {
		return "Neighbour{" +
				"neighbour_id=" + neighbour_id +
				", name='" + name +
				", capabilities=" + capabilities +
				", subscriptionRequest=" + subscriptionRequest +
				", fedIn=" + fedIn +
				", lastUpdated=" + lastUpdated +
				", lastSeen=" + lastSeen +
				", backoffStart=" + backoffStart +
				", backoffAttempts=" + backoffAttempts +
				", messageChannelPort='" + messageChannelPort +
				", controlChannelPort='" + controlChannelPort +
				'}';
	}

	public String getControlChannelUrl(String file) {
		try {
			if (this.getControlChannelPort() == null || this.getControlChannelPort().equals(DEFAULT_CONTROL_CHANNEL_PORT)) {
				return new URL(DEFAULT_CONTROL_CHANNEL_PROTOCOL, name, file)
						.toExternalForm();
			} else {
				return new URL(DEFAULT_CONTROL_CHANNEL_PROTOCOL, name, Integer.parseInt(this.getControlChannelPort()), file)
						.toExternalForm();
			}
		} catch (NumberFormatException | MalformedURLException e) {
			logger.error("Could not create control channel url for interchange {}", this, e);
			throw new DiscoveryException(e);
		}
	}

	public String getMessageChannelUrl() {
		try {
			if (this.getMessageChannelPort() == null || this.getMessageChannelPort().equals(DEFAULT_MESSAGE_CHANNEL_PORT)) {
				return String.format("amqps://%s/", name);
			} else {
				return String.format("amqps://%s:%s/", name, this.getMessageChannelPort());
			}
		} catch (NumberFormatException e) {
			logger.error("Could not create message channel url for interchange {}", this, e);
			throw new DiscoveryException(e);
		}
	}
}
