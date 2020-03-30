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
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;


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
	private SubscriptionRequest subscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "neighbour_id_fed_in", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_subreq_neighbour_fed_in"))
	private SubscriptionRequest fedIn = new SubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());

	@UpdateTimestamp
	private LocalDateTime lastUpdated;
	private LocalDateTime backoffStart;
	private int backoffAttempts = 0;
	private String messageChannelPort;
	private String controlChannelPort;
	private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

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

	public void setSubscriptionRequest(SubscriptionRequest subscriptionRequest) {
		this.subscriptionRequest = subscriptionRequest;
	}

	@Override
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


	@SuppressWarnings("WeakerAccess")
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

	public boolean hasEstablishedSubscriptions() {
		return getSubscriptionRequest() != null && getSubscriptionRequest().getStatus() == SubscriptionRequestStatus.ESTABLISHED;
	}

	public boolean hasCapabilities() {
		return getCapabilities() != null && getCapabilities().getStatus() == Capabilities.CapabilitiesStatus.KNOWN;
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

	public boolean shouldCheckSubscriptionRequestsForUpdates(LocalDateTime localSubscriptionsUpdated) {
		return this.getFedIn() == null
				|| this.getFedIn().getSuccessfulRequest() == null
				|| this.getFedIn().getSuccessfulRequest().isBefore(localSubscriptionsUpdated);
	}

	public void failedConnection(int maxAttemptsBeforeUnreachable) {
		if (this.getBackoffStartTime() == null) {
			this.setConnectionStatus(ConnectionStatus.FAILED);
			this.backoffStart = LocalDateTime.now();
			this.backoffAttempts = 0;
			logger.warn("Starting backoff now {}", this.backoffStart);
		}
		else {
			this.backoffAttempts++;
			logger.warn("Increasing backoff counter to {}", this.backoffAttempts);
			if (this.getBackoffAttempts() > maxAttemptsBeforeUnreachable) {
				this.setConnectionStatus(ConnectionStatus.UNREACHABLE);
				logger.warn("Unsuccessful in reestablishing contact with neighbour. Exceeded number of allowed connection attempts.");
				logger.warn("Number of allowed connection attempts: {} Number of actual connection attempts: {}", maxAttemptsBeforeUnreachable, this.getBackoffAttempts());
				logger.warn("Setting status of neighbour to UNREACHABLE.");
			}
		}
	}

	public void setConnectionStatus(ConnectionStatus connectionStatus) {
		this.connectionStatus = connectionStatus;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public boolean canBeContacted(int randomShiftUpperLimit, int startIntervalLength) {
		if (this.getConnectionStatus() == ConnectionStatus.UNREACHABLE) {
			return false;
		}
		if (this.getConnectionStatus() == ConnectionStatus.CONNECTED) {
			return true;
		}

		if (this.getConnectionStatus() == ConnectionStatus.FAILED) {
			return  this.getBackoffStartTime() == null || LocalDateTime.now().isAfter(this.getNextPostAttemptTime(randomShiftUpperLimit, startIntervalLength));
		}
		return true;
	}

	// Calculates next possible post attempt time, using exponential backoff
	LocalDateTime getNextPostAttemptTime(int randomShiftUpperLimit, int startIntervalLength) {

		logger.info("Calculating next allowed time to contact neighbour.");
		int randomShift = new Random().nextInt(randomShiftUpperLimit);
		long exponentialBackoffWithRandomizationMillis = (long) (Math.pow(2, this.getBackoffAttempts()) * startIntervalLength) + randomShift;
		LocalDateTime nextPostAttempt = this.getBackoffStartTime().plus(exponentialBackoffWithRandomizationMillis, ChronoField.MILLI_OF_SECOND.getBaseUnit());

		logger.info("Next allowed post time: {}", nextPostAttempt.toString());
		return nextPostAttempt;
	}


	public boolean needsOurUpdatedCapabilities(LocalDateTime localCapabilitiesUpdated) {
		return this.getCapabilities() == null
				|| this.getCapabilities().getLastCapabilityExchange() == null
				|| localCapabilitiesUpdated.isAfter(this.getCapabilities().getLastCapabilityExchange());
	}
}
