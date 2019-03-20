package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "service_providers",
		uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_spr_name"))
public class ServiceProvider {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sp_generator")
	@SequenceGenerator(name="spr_generator", sequenceName = "spr_seq", allocationSize=50)
	@Column(name="spr_id")
	private Integer id;

	private String name;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "spr_id_cap", foreignKey = @ForeignKey(name = "fk_dat_spr"))
	private Set<DataType> capabilities;

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "spr_id_sub", foreignKey = @ForeignKey(name = "fk_sub_spr"))
	private Set<Subscription> subscriptions;

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

	public Set<DataType> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<DataType> capabilities) {
		this.capabilities = capabilities;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
