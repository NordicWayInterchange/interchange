package no.vegvesen.ixn.federation.model;

import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityStatus;
import no.vegvesen.ixn.serviceprovider.NotFoundException;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "service_providers", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_spr_name"))
public class ServiceProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "spr_seq")
	@Column(name = "id")
	private Integer id;

	@Column(length = 320)
	private String name;

	private boolean biConsumer = false;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "cap_id", foreignKey = @ForeignKey(name = "fk_spr_cap"))
	private Capabilities capabilities = new Capabilities(new HashSet<>());

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "spr_id", foreignKey = @ForeignKey(name = "fk_locsub_spr"))
	private List<LocalSubscription> subscriptions = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "del_id", foreignKey = @ForeignKey(name = "fk_deliveries"))
	private Set<LocalDelivery> deliveries = new HashSet<>();

	private LocalDateTime subscriptionUpdated;

	public ServiceProvider() {
	}

	public ServiceProvider(String name) {
		this.name = name;
	}

	public ServiceProvider(String name,
						   Set<LocalSubscription> subscriptions) {
		this.name = name;
		this.subscriptions.addAll(subscriptions);
	}

	public ServiceProvider(String name,
						   Capabilities capabilities) {
		this.name = name;
		this.capabilities = capabilities;
	}

	public ServiceProvider(Integer id,
						   String name,
						   Capabilities capabilities,
						   Set<LocalSubscription> subscriptions,
						   LocalDateTime subscriptionUpdated) {

		this.id = id;
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions.addAll(subscriptions);
		this.subscriptionUpdated = subscriptionUpdated;
	}

	public ServiceProvider(String name,
						   Capabilities capabilities,
						   Set<LocalSubscription> subscriptions,
						   LocalDateTime subscriptionUpdated) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions.addAll(subscriptions);
		this.subscriptionUpdated = subscriptionUpdated;
	}

	public ServiceProvider(String name,
						   Capabilities capabilities,
						   Set<LocalSubscription> localSubscriptions,
						   Set<LocalDelivery> localDeliveries,
						   LocalDateTime subscriptionUpdated) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions.addAll(localSubscriptions);
		this.deliveries.addAll(localDeliveries);
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

	public boolean hasBiConsumerAccess() {
		return biConsumer;
	}

	public void setHasBiConsumerAccess(boolean biConsumer) {
		this.biConsumer = biConsumer;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Capabilities capabilities) {
		this.capabilities = capabilities;
	}

	public List<LocalSubscription> getSubscriptions() {
		return subscriptions;
	}

	public void  addLocalSubscription(LocalSubscription subscription) {
		subscriptions.add(subscription);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void addLocalSubscriptions(List<LocalSubscription> subscriptions) {
		this.subscriptions.addAll(subscriptions);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void setSubscriptions(List<LocalSubscription> subscriptions) {
		this.subscriptions = subscriptions;
		this.subscriptionUpdated = LocalDateTime.now();
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

	public void removeLocalSubscription(String uuid){
		LocalSubscription subscriptionToDelete = subscriptions
				.stream()
				.filter(subscription -> subscription.getUuid().equals(uuid))
				.findFirst()
				.orElseThrow(
						() -> new NotFoundException("The subscription to delete is not in the Service Provider subscriptions. Cannot delete subscription that doesn't exist.")
				);
		subscriptionToDelete.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void removeOneOrMultipleLocalSubscriptions(List<String> subscriptionUuidList){
		subscriptionUuidList.forEach(uuid -> {
					LocalSubscription subscriptionToDelete = subscriptions
							.stream()
							.filter(subscription -> subscription.getUuid().equals(uuid))
							.findFirst()
							.orElseThrow(
									() -> new NotFoundException("The subscription to delete with Uuid '" + uuid + "' is not in the Service Provider subscriptions. Cannot delete subscription that doesn't exist.")
							);
					subscriptionToDelete.setStatus(LocalSubscriptionStatus.TEAR_DOWN);
				});

		this.subscriptionUpdated = LocalDateTime.now();
	}

	public void removeSubscriptions(Set<LocalSubscription> subscriptionsToRemove) {
		subscriptions.removeAll(subscriptionsToRemove);
		this.subscriptionUpdated = LocalDateTime.now();
	}

	public boolean hasCapabilitiesOrActiveSubscriptions() {
		return (!capabilities.getCapabilities().isEmpty() ||
				!activeSubscriptions().isEmpty());
	}

	public boolean hasCapabilities () {
		return !capabilities.getCapabilities().isEmpty();
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

	public void setDeliveries(Set<LocalDelivery> deliveries) {
		this.deliveries = deliveries;
	}

	public Set<LocalDelivery> getDeliveries() {
		return deliveries;
	}

	public void addDeliveries(Set<LocalDelivery> newDeliveries) {
		deliveries.addAll(newDeliveries);
	}

    public void addDelivery(LocalDelivery newDelivery){
        deliveries.add(newDelivery);
    }

	public void removeLocalDelivery(String deliveryId) {
		LocalDelivery localDeliveryToDelete = deliveries
				.stream()
				.filter(localDelivery -> localDelivery.getUuid().equals(deliveryId))
				.findFirst()
				.orElseThrow(
						() -> new NotFoundException("The delivery to delete is not in the Service Provider deliveries. Cannot delete delivery that don't exist.")
				);
		localDeliveryToDelete.setStatus(LocalDeliveryStatus.TEAR_DOWN);
	}

	public LocalDelivery findDeliveryByUuid(String deliveryUuid) {
		return deliveries
				.stream()
				.filter(localDelivery -> localDelivery.getUuid().equals(deliveryUuid))
				.findFirst()
				.orElse(null);
	}

	public Capability getCapability(String capabilityId){
		return
				getCapabilities().getCapabilities().stream()
						.filter(c-> c.getUuid().equals(capabilityId))
						.findFirst()
						.orElseThrow(() -> new NotFoundException(String.format("Could not find capability with ID %s for service provider %s", capabilityId, name)));
	}

	public Capability getCreatedCapability(String capabilityId){
		return getCapabilities().getCapabilitiesByStatus(CapabilityStatus.CREATED).stream()
				.filter(c->c.getUuid().equals(capabilityId))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find capability with ID %s for service provider %s", capabilityId, name)));
	}

	public LocalSubscription getSubscription(String subscriptionId){
		return getSubscriptions()
				.stream()
				.filter(s -> s.getUuid().equals(subscriptionId))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find subscription with ID %s for service provider %s",subscriptionId,name)));
	}

	public Set<LocalDelivery> getSavedDeliveries(Set<LocalDelivery> allDeliveries){
		return this.getDeliveries()
				.stream()
				.filter(allDeliveries::contains)
				.collect(Collectors.toSet());
	}

	public LocalDelivery getDelivery(String deliveryId){
		return getDeliveries()
				.stream()
				.filter(d -> d.getUuid().equals(deliveryId))
				.findFirst()
				.orElseThrow(() -> new NotFoundException(String.format("Could not find delivery with ID %s for service provider %s",deliveryId,name)));
	}

    public Set<LocalSubscription> wantedNonRedirectSubscriptions() {
        return getSubscriptions()
                .stream()
                .filter(LocalSubscription::isSubscriptionWanted)
                .filter(l -> !l.getConsumerCommonName().equals(name))
                .collect(Collectors.toSet());
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
				", biConsumer=" + biConsumer +
				", capabilities=" + capabilities +
				", subscriptions=" + Arrays.toString(subscriptions.toArray()) +
				", deliveries=" + Arrays.toString(deliveries.toArray()) +
				'}';
	}
}
