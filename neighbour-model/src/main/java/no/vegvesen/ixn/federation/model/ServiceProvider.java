package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "service_providers", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_spr_name"))
public class ServiceProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sp_generator")
	@SequenceGenerator(name = "spr_generator", sequenceName = "spr_seq")
	@Column(name = "spr_id")
	private Integer id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "spr_id_cap", foreignKey = @ForeignKey(name = "fk_cap_spr"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@JoinColumn(name = "spr_locsub_id", foreignKey = @ForeignKey(name = "fk_spr_locsub"))
	private Set<LocalSubscription> subscriptions = new HashSet<>();

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
	}

	//TODO gjør om til streams-basert
	public Set<String> wantedLocalBindings() {
		Set<String> wantedBindings = new HashSet<>();
		for (LocalSubscription subscription : subscriptions) {
			if (subscription.isSubscriptionWanted() ) {
				wantedBindings.add(subscription.bindKey());
			}
		}
		return wantedBindings;
	}

	public Set<String> unwantedLocalBindings(Set<String> existingKeys) {
		Set<String> wantedBindKeys = this.wantedLocalBindings();
		Set<String> unwantedBindKeys = new HashSet<>(existingKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
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

	public void setSubscriptions(Set<LocalSubscription> subscriptions) {
	    this.subscriptions = subscriptions;
	}
}
