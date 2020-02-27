package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.stream.Collectors;

@Entity
@Table(name = "service_providers", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_spr_name"))
public class ServiceProvider implements Subscriber {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sp_generator")
	@SequenceGenerator(name = "spr_generator", sequenceName = "spr_seq")
	@Column(name = "spr_id")
	private Integer id;

	private String name;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "spr_id_cap", referencedColumnName = "cap_id", foreignKey = @ForeignKey(name = "fk_cap_spr"))
	private Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, new HashSet<>());

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "local_sub_id", referencedColumnName = "subreq_id", foreignKey = @ForeignKey(name = "fk_sub_spr"))
	private LocalSubscriptionRequest subscriptionRequest = new LocalSubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());

	public ServiceProvider() {
	}

	@JsonCreator
	public ServiceProvider(@JsonProperty("name") String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public SubscriptionRequest getSubscriptionRequest() {
		LocalSubscriptionRequest localSubscriptionRequest = getOrCreateLocalSubscriptionRequest();
		return new SubscriptionRequest(localSubscriptionRequest.getStatus(), localSubscriptionRequest.getSubscriptions().stream().map(DataType::toSubscription).collect(Collectors.toSet()));
	}

	@Override
	public void setSubscriptionRequestStatus(SubscriptionRequestStatus subscriptionRequestStatus) {
		this.getOrCreateLocalSubscriptionRequest().setStatus(subscriptionRequestStatus);
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

	public LocalSubscriptionRequest getLocalSubscriptionRequest() {
		return subscriptionRequest;
	}

	public LocalSubscriptionRequest getOrCreateLocalSubscriptionRequest() {
		if (subscriptionRequest == null) {
			subscriptionRequest = new LocalSubscriptionRequest(SubscriptionRequestStatus.EMPTY, new HashSet<>());
		}
		return subscriptionRequest;
	}

	public void setLocalSubscriptionRequest(LocalSubscriptionRequest subscriptionRequest) {
		this.subscriptionRequest = subscriptionRequest;
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
				", subscriptionRequest=" + subscriptionRequest +
				'}';
	}

}
