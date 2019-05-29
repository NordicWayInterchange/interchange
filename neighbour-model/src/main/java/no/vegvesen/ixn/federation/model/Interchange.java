package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.hibernate.annotations.UpdateTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "interchanges", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_ixn_name"))
public class Interchange {

	static Logger logger = LoggerFactory.getLogger(Interchange.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ixn_generator")
	@SequenceGenerator(name = "ixn_generator", sequenceName = "ixn_seq")
	@Column(name = "ixn_id")
	private Integer ixn_id;

	private String name; // common name from the certificate

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_cap", referencedColumnName = "cap_id", foreignKey = @ForeignKey(name = "fk_cap_ixn"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_sub_out", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_subreq_ixn_sub_out"))
	private SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_fed_in", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_subreq_ixn_fed_in"))
	private SubscriptionRequest fedIn = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@UpdateTimestamp
	private LocalDateTime lastUpdated;
	private LocalDateTime lastSeen;
	private LocalDateTime backoffStart;
	private int backoffAttempts = 0;
	private String domainName;
	private String messageChannelPort;
	private String controlChannelPort;

	public Interchange() {
	}

	public Interchange(String name, Capabilities capabilities, SubscriptionRequest subscriptions, SubscriptionRequest fedIn) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptionRequest = subscriptions;
		this.fedIn = fedIn;
	}

	public Interchange(String name, String domain) {
		this.name = name;
		this.domainName = domain;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SubscriptionRequest getSubscriptionRequest() {
		return subscriptionRequest;
	}

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

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		if (domainName.startsWith(".")) {
			throw new DiscoveryException("Domain name shall not start with \".\"");
		}
		this.domainName = domainName;
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
		return "Interchange{" +
				"ixn_id=" + ixn_id +
				", name='" + name + '\'' +
				", capabilities=" + capabilities +
				", subscriptionRequest=" + subscriptionRequest +
				", fedIn=" + fedIn +
				", lastUpdated=" + lastUpdated +
				", lastSeen=" + lastSeen +
				", backoffStart=" + backoffStart +
				", backoffAttempts=" + backoffAttempts +
				", domainName='" + domainName + '\'' +
				", messageChannelPort='" + messageChannelPort + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				'}';
	}
}
