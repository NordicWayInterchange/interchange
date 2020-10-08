package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Entity
@Table(name = "neighbours", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_neighbour_name"))
public class Neighbour {

	private static final String DEFAULT_CONTROL_CHANNEL_PORT = "443";
	private static final String DEFAULT_CONTROL_CHANNEL_PROTOCOL = "https";
	private static final String DEFAULT_MESSAGE_CHANNEL_PORT = "5671";

	private static Logger logger = LoggerFactory.getLogger(Neighbour.class);

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "neighbour_seq")
	@Column(name = "id")
	private Integer neighbour_id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_neighbour_cap"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "sub_out", foreignKey = @ForeignKey(name = "fk_neighbour_subreq_sub_out"))
	private SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "fed_in", foreignKey = @ForeignKey(name = "fk_neighbour_subreq_fed_in"))
	private SubscriptionRequest fedIn = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "mes_back", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_message_connection_backoff"))
	private ConnectionBackoff messageConnectionBackoff = new ConnectionBackoff();

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "con_back", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_control_connection_backoff"))
	private ConnectionBackoff controlConnectionBackoff = new ConnectionBackoff();

	@UpdateTimestamp
	private LocalDateTime lastUpdated;
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

	public Neighbour(String name, Capabilities capabilities, SubscriptionRequest subscriptions, SubscriptionRequest fedIn, ConnectionBackoff messageConnectionBackoff, ConnectionBackoff controlConnectionBackoff) {
		this.setName(name);
		this.capabilities = capabilities;
		this.subscriptionRequest = subscriptions;
		this.fedIn = fedIn;
		this.messageConnectionBackoff = messageConnectionBackoff;
		this.controlConnectionBackoff = controlConnectionBackoff;
	}

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

	public SubscriptionRequest getSubscriptionRequest() {
		return subscriptionRequest;
	}

	public void setSubscriptionRequest(SubscriptionRequest subscriptionRequest) {
		this.subscriptionRequest = subscriptionRequest;
	}

	public void setSubscriptionRequestStatus(SubscriptionRequestStatus subscriptionRequestStatus) {
		this.subscriptionRequest.setStatus(subscriptionRequestStatus);
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

	public Set<Subscription> getSubscriptionsForPolling() {
		return getFedIn().getSubscriptions().stream()
				.filter(s -> SubscriptionStatus.REQUESTED.equals(s.getSubscriptionStatus()) ||
						SubscriptionStatus.ACCEPTED.equals(s.getSubscriptionStatus()) ||
						SubscriptionStatus.FAILED.equals(s.getSubscriptionStatus()))
				.collect(Collectors.toSet());
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
				", connectionBackoff=" + messageConnectionBackoff +
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

	public boolean hasEstablishedSubscriptions() {
		return getSubscriptionRequest() != null && getSubscriptionRequest().getStatus() == SubscriptionRequestStatus.ESTABLISHED;
	}

	public boolean hasCapabilities() {
		return getCapabilities() != null && getCapabilities().getStatus() == Capabilities.CapabilitiesStatus.KNOWN;
	}

	public Set<String> getUnwantedBindKeys(Set<String> existingBindKeys) {
		Set<String> wantedBindKeys = this.wantedBindings();
		Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
	}

	Set<String> wantedBindings() {
		Set<String> wantedBindings = new HashSet<>();
		for (Subscription subscription : getSubscriptionRequest().getAcceptedSubscriptions()) {
			wantedBindings.add(subscription.bindKey());
		}
		return wantedBindings;
	}

	public void setDnsProperties(Neighbour dnsNeighbour) {
		assert dnsNeighbour.getName().equals(this.getName());
		logger.debug("Found neighbour {} in DNS, populating with port values from DNS: control {}, message {}",
				this.getName(),
				dnsNeighbour.getControlChannelPort(),
				dnsNeighbour.getMessageChannelPort());
		this.setControlChannelPort(dnsNeighbour.getControlChannelPort());
		this.setMessageChannelPort(dnsNeighbour.getMessageChannelPort());

	}

	/**
	 * The basis of the subscription calculations are Self.local subscriptions and capabilities of the neighbour.
	 * Should calculate new subscriptions if the localSubscriptionsUpdated time or the lastCapabilityExchange for
	 * this neighbour is after the last time we calculated subscriptions and sent request to the neighbour.
	 * <p>
	 * Only neighbours with known capabilities are candidates for subscription request.
	 *
	 * @param localSubscriptionsUpdated when are the last local subscriptions updated, optionally
	 * @return true if the localSubscriptionsUpdated time or the getLastCapabilityExchange is after the last time we
	 * calculated and sent subscription request to the neighbour
	 */
	public boolean shouldCheckSubscriptionRequestsForUpdates(Optional<LocalDateTime> localSubscriptionsUpdated) {
		if (!localSubscriptionsUpdated.isPresent()) {
			logger.debug("Local subscriptions never established for any service provider, should not check for subscription requests for any neighbour");
			return false;
		}
		LocalDateTime localSubscriptionsUpdatedTime = localSubscriptionsUpdated.get();

		if (hasCapabilities() && getCapabilities().getLastCapabilityExchange() == null) {
			logger.warn("Should not check subscription for neighbour with no capabilities exchange");
			return false;
		}
		LocalDateTime capabilityPostDate = this.getCapabilities().getLastCapabilityExchange();

		if (this.getFedIn() == null || !this.getFedIn().getSuccessfulRequest().isPresent()) {
			logger.debug("Should check subscription for neighbour with no previous subscription request");
			return true;
		}

		LocalDateTime neighbourSubscriptionRequestTime = this.getFedIn().getSuccessfulRequest().get();

		boolean shouldCheckSubscriptionRequestForUpdates = neighbourSubscriptionRequestTime.isBefore(localSubscriptionsUpdatedTime)
				|| neighbourSubscriptionRequestTime.isBefore(capabilityPostDate);
		logger.debug("shouldCheckSubscriptionRequestForUpdates {}: localSubscriptionsUpdated {}, successfulRequest {}, lastCapabilityExchange {}",
				shouldCheckSubscriptionRequestForUpdates,
				localSubscriptionsUpdatedTime,
				neighbourSubscriptionRequestTime,
				capabilityPostDate);
		return shouldCheckSubscriptionRequestForUpdates;
	}

	public boolean needsOurUpdatedCapabilities(Optional<LocalDateTime> localCapabilitiesUpdated) {
		if (localCapabilitiesUpdated.isPresent()) {
			logger.debug("Local capabilities updated {}, last neighbour capability exchange {}", localCapabilitiesUpdated, this.getCapabilities().getLastCapabilityExchange());
			return this.capabilitiesNeverSeen()
					|| this.getCapabilities().getLastCapabilityExchange().isBefore(localCapabilitiesUpdated.get());
		} else {
			return this.capabilitiesNeverSeen();
		}
	}

	private boolean capabilitiesNeverSeen() {
		return this.getCapabilities() == null || this.getCapabilities().getLastCapabilityExchange() == null;
	}

	/**
	 * Evaluates if the other object is the same neighbour, not that all member attributes are equal.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Neighbour)) return false;

		Neighbour neighbour = (Neighbour) o;

		return name.equals(neighbour.name);
	}

	/**
	 * Neighbour with same name will give the same result. Not using all member attributes.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public void setNeighbour_id(Integer neighbour_id) {
		this.neighbour_id = neighbour_id;
	}

	public ConnectionBackoff getMessageConnectionBackoff() {
		return messageConnectionBackoff;
	}

	public void setMessageConnectionBackoff(ConnectionBackoff connectionBackoff) {
		this.messageConnectionBackoff = connectionBackoff;
	}

	public ConnectionBackoff getControlConnectionBackoff() { return controlConnectionBackoff; }

	public void setControlConnectionBackoff(ConnectionBackoff controlConnectionBackoff) {
		this.controlConnectionBackoff = controlConnectionBackoff;
	}
}
