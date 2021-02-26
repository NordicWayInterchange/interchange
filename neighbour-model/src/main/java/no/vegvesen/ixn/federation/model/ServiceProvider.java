package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

	private LocalDateTime subscriptionUpdated;

	public ServiceProvider() {
	}

	@JsonCreator
	public ServiceProvider(@JsonProperty("name") String name) {
		this.name = name;
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

	public void addLocalSubscription(LocalSubscription subscription) {
		subscriptions.add(subscription);
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
				.filter(subscription -> subscription.getSub_id().equals(dataTypeId))
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

	public void updateSubscriptionWithBrokerUrl(LocalSubscription subscription, String brokerUrl) {
		for(LocalSubscription localSubscription : subscriptions) {
			if (localSubscription.getSelector().equals(subscription.getSelector()) &&
					localSubscription.isCreateNewQueue()){
				localSubscription.setBrokerUrl(brokerUrl);
			}
		}
	}

	public boolean hasCapabilitiesOrActiveSubscriptions() {
		return (capabilities.hasDataTypes() ||
				!activeSubscriptions().isEmpty());
	}

	public boolean hasCapabilities () {
		return capabilities.hasDataTypes();
	}

	public Set<LocalSubscription> activeSubscriptions() {
		return subscriptions.stream()
		.filter(subscription -> subscription.getStatus() != LocalSubscriptionStatus.TEAR_DOWN)
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
				", capabilities=" + capabilities +
				", subscriptions=" + Arrays.toString(subscriptions.toArray()) +
				'}';
	}

}
