package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
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
	@JoinColumn(name = "neighbour_requested_subs", foreignKey = @ForeignKey(name = "fk_neighbour_subreq_neigh_requested"))
	private NeighbourSubscriptionRequest neighbourRequestedSubscriptions = new NeighbourSubscriptionRequest(NeighbourSubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "our_requested_subs", foreignKey = @ForeignKey(name = "fk_neighbour_subreq_our_requested"))
	private SubscriptionRequest ourRequestedSubscriptions = new SubscriptionRequest(new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "con_con", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_neighbour_control_connection"))
	private Connection controlConnection = new Connection();

	@UpdateTimestamp
	private LocalDateTime lastUpdated;
	private String controlChannelPort;

	public Neighbour() {
	}

	public Neighbour(String name, Capabilities capabilities, NeighbourSubscriptionRequest subscriptions, SubscriptionRequest ourRequestedSubscriptions) {
		this.setName(name);
		this.capabilities = capabilities;
		this.neighbourRequestedSubscriptions = subscriptions;
		this.ourRequestedSubscriptions = ourRequestedSubscriptions;
	}

	public Neighbour(String name, Capabilities capabilities, NeighbourSubscriptionRequest subscriptions, SubscriptionRequest ourRequestedSubscriptions, Connection controlConnection) {
		this.setName(name);
		this.capabilities = capabilities;
		this.neighbourRequestedSubscriptions = subscriptions;
		this.ourRequestedSubscriptions = ourRequestedSubscriptions;
		this.controlConnection = controlConnection;
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

	public NeighbourSubscriptionRequest getNeighbourRequestedSubscriptions() {
		return neighbourRequestedSubscriptions;
	}

	public void setNeighbourRequestedSubscriptions(NeighbourSubscriptionRequest subscriptionRequest) {
		this.neighbourRequestedSubscriptions = subscriptionRequest;
	}

	public void setSubscriptionRequestStatus(NeighbourSubscriptionRequestStatus subscriptionRequestStatus) {
		this.neighbourRequestedSubscriptions.setStatus(subscriptionRequestStatus);
	}

	public SubscriptionRequest getOurRequestedSubscriptions() {
		return ourRequestedSubscriptions;
	}

	public void setOurRequestedSubscriptions(SubscriptionRequest fedIn) {
		this.ourRequestedSubscriptions = fedIn;
	}

	public String getControlChannelPort() {
		return controlChannelPort;
	}

	public void setControlChannelPort(String controlChannelPort) {
		this.controlChannelPort = controlChannelPort;
	}

	public Set<Subscription> getSubscriptionsForPolling() {
		return getOurRequestedSubscriptions().getSubscriptions().stream()
				.filter(s -> SubscriptionStatus.REQUESTED.equals(s.getSubscriptionStatus()) ||
						SubscriptionStatus.ACCEPTED.equals(s.getSubscriptionStatus()) ||
						SubscriptionStatus.FAILED.equals(s.getSubscriptionStatus()))
				.collect(Collectors.toSet());
	}

	@Override
	public String toString() {
		return "Neighbour{" +
				"neighbour_id=" + neighbour_id +
				", name=" + name +
				", capabilities=" + capabilities +
				", neighbourRequestedSubscriptions=" + neighbourRequestedSubscriptions +
				", ourRequestedSubscriptions=" + ourRequestedSubscriptions +
				", lastUpdated=" + lastUpdated +
				", controlConnection=" + controlConnection +
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

	public boolean hasEstablishedSubscriptions() {
		return getNeighbourRequestedSubscriptions() != null && getNeighbourRequestedSubscriptions().getStatus() == NeighbourSubscriptionRequestStatus.ESTABLISHED;
	}

	public boolean hasCapabilities() {
		return getCapabilities() != null && getCapabilities().getStatus() == Capabilities.CapabilitiesStatus.KNOWN;
	}

	public void setDnsProperties(Neighbour dnsNeighbour) {
		assert dnsNeighbour.getName().equals(this.getName());
		logger.debug("Found neighbour {} in DNS, populating with port values from DNS: control {}",
				this.getName(),
				dnsNeighbour.getControlChannelPort());
		this.setControlChannelPort(dnsNeighbour.getControlChannelPort());
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

		if (this.getOurRequestedSubscriptions() == null || !this.getOurRequestedSubscriptions().getSuccessfulRequest().isPresent()) {
			logger.debug("Should check subscription for neighbour with no previous subscription request");
			return true;
		}

		LocalDateTime neighbourSubscriptionRequestTime = this.getOurRequestedSubscriptions().getSuccessfulRequest().get();

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

	public Connection getControlConnection() { return controlConnection; }

	public void setControlConnection(Connection controlConnection) {
		this.controlConnection = controlConnection;
	}
}
