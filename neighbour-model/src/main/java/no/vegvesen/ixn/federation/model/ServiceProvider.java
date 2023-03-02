package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.vegvesen.ixn.federation.exceptions.PrivateChannelException;
import no.vegvesen.ixn.serviceprovider.NotFoundException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "service_providers", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_spr_name"))
public class ServiceProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spr_seq")
	@Column(name = "id")
	private Integer id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name = "fk_spr_cap"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "spr_id", foreignKey = @ForeignKey(name = "fk_locsub_spr"))
	private Set<LocalSubscription> subscriptions = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "priv_channel_id", foreignKey = @ForeignKey(name = "fk_priv_channel"))
	private Set<PrivateChannel> privateChannels = new HashSet<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "del_id", foreignKey = @ForeignKey(name = "fk_deliveries"))
	private Set<LocalDelivery> deliveries = new HashSet<>();

	private LocalDateTime subscriptionUpdated;

	public ServiceProvider() {
	}

	@JsonCreator
	public ServiceProvider(@JsonProperty("name") String name) {
		this.name = name;
	}

	public ServiceProvider(String name,
						   Set<LocalSubscription> subscriptions) {
		this.name = name;
		this.subscriptions.addAll(subscriptions);
	}

	public ServiceProvider(Integer id,
						   String name,
						   Capabilities capabilities,
						   Set<LocalSubscription> subscriptions,
						   Set<PrivateChannel> privateChannels,
						   LocalDateTime subscriptionUpdated) {

		this.id = id;
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions.addAll(subscriptions);
		this.privateChannels = privateChannels;
		this.subscriptionUpdated = subscriptionUpdated;
	}

	public ServiceProvider(String name,
						   Capabilities capabilities,
						   Set<LocalSubscription> subscriptions,
						   Set<PrivateChannel> privateChannels,
						   LocalDateTime subscriptionUpdated) {

		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions.addAll(subscriptions);
		this.privateChannels = privateChannels;
		this.subscriptionUpdated = subscriptionUpdated;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public Set<LocalSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void  addLocalSubscription(LocalSubscription subscription) {
		subscriptions.add(subscription);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void addLocalSubscriptions(Set<LocalSubscription> subscriptions) {
		this.subscriptions.addAll(subscriptions);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void setSubscriptions(Set<LocalSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Optional<LocalDateTime> getSubscriptionUpdated() {
		return Optional.ofNullable(subscriptionUpdated);
	}

	public void removeLocalSubscription(Integer dataTypeId) {
		LocalSubscription subscriptionToDelete = subscriptions
				.stream()
				.filter(subscription -> subscription.getId().equals(dataTypeId))
				.findFirst()
				.orElseThrow(
						() -> new NotFoundException("The subscription to delete is not in the Service Provider subscriptions. Cannot delete subscription that don't exist.")
				);
		subscriptionToDelete.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void updateSubscriptions(Set<LocalSubscription> newSubscriptions) {
		this.setSubscriptions(newSubscriptions);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void removeSubscription(LocalSubscription subscription) {
		subscriptions.remove(subscription);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public boolean hasCapabilitiesOrActiveSubscriptions() {
		return (capabilities.hasDataTypes() ||
				!activeSubscriptions().isEmpty());
	}

	public boolean hasCapabilities () {
		return capabilities.hasDataTypes();
	}

	public boolean hasDeliveries () {
		Set<LocalDelivery> validDeliveries = deliveries.stream()
				.filter(d -> d.getStatus().equals(LocalDeliveryStatus.CREATED)
						|| d.getStatus().equals(LocalDeliveryStatus.REQUESTED)
						|| d.getStatus().equals(LocalDeliveryStatus.NO_OVERLAP))
				.collect(Collectors.toSet());

		return validDeliveries.size() > 0;
	}

	public boolean hasActiveSubscriptions() {
		return !activeSubscriptions().isEmpty();
	}

	public Set<LocalSubscription> activeSubscriptions() {
		return subscriptions.stream()
		.filter(subscription -> LocalSubscriptionStatus.isAlive(subscription.getStatus()))
		.collect(Collectors.toSet());
	}

	public PrivateChannel addPrivateChannel(String peerName) {
		PrivateChannel newPrivateChannel = new PrivateChannel(peerName, PrivateChannelStatus.REQUESTED);
		if(privateChannels.contains(newPrivateChannel)){
			throw new PrivateChannelException("Client already has private channel");
		} else {
			privateChannels.add(newPrivateChannel);
		}
		return newPrivateChannel;
	}

	public void setPrivateChannelToTearDown(Integer privateChannelId) {
		PrivateChannel privateChannelToDelete = privateChannels
				.stream()
				.filter(privateChannel -> privateChannel.getId().equals(privateChannelId))
				.findFirst()
				.orElseThrow(
						() -> new NotFoundException("The private channel to delete is not in the Service Provider private channels. Cannot delete private channel that don't exist.")
				);
		privateChannelToDelete.setStatus(PrivateChannelStatus.TEAR_DOWN);
	}

	public Set<PrivateChannel> getPrivateChannels() {
		return privateChannels;
	}

	public void setPrivateChannels(Set<PrivateChannel> newPrivateChannels) {
		this.privateChannels = newPrivateChannels;
	}

	public void setDeliveries(Set<LocalDelivery> deliveries) {
		this.deliveries = deliveries;
	}

	public Set<LocalDelivery> getDeliveries() {
		return deliveries;
	}

	public void addDeliveries(Set<LocalDelivery> newDeliveries) {
		deliveries.addAll(newDeliveries);
	}

	public void removeLocalDelivery(Integer deliveryId) {
		LocalDelivery localDeliveryToDelete = deliveries
				.stream()
				.filter(localDelivery -> localDelivery.getId().equals(deliveryId))
				.findFirst()
				.orElseThrow(
						() -> new NotFoundException("The delivery to delete is not in the Service Provider deliveries. Cannot delete delivery that don't exist.")
				);
		localDeliveryToDelete.setStatus(LocalDeliveryStatus.TEAR_DOWN);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ServiceProvider that = (ServiceProvider) o;

		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return "ServiceProvider{" +
				"id=" + id +
				", name='" + name + '\'' +
				", capabilities=" + capabilities +
				", subscriptions=" + Arrays.toString(subscriptions.toArray()) +
				", privateChannels=" + Arrays.toString(privateChannels.toArray()) +
				", deliveries=" + Arrays.toString(deliveries.toArray()) +
				'}';
	}

}
