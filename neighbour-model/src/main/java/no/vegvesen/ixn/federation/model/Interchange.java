package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(	name = "interchanges",
		uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_ixn_name"))
public class Interchange {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ixn_generator")
	@SequenceGenerator(name="ixn_generator", sequenceName = "ixn_seq", allocationSize=50)
	@Column(name="ixn_id")
	private Integer ixn_id;

	private String name; // common name from the certificate

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_cap", foreignKey = @ForeignKey(name="fk_dat_ixn"))
	private Set<DataType> capabilities;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_sub_out", foreignKey = @ForeignKey(name = "fk_sub_ixn_sub_out"))
	private Set<Subscription> subscriptions;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_fed_in", foreignKey = @ForeignKey(name="fk_sub_ixn_fed_in"))
	private Set<Subscription> fedIn;

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Interchange(){}

	public Interchange(String name, Set<DataType> capabilities, Set<Subscription> subscriptions, Set<Subscription> fedIn) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions = subscriptions;
		this.fedIn = fedIn;
	}

	public Set<DataType> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<DataType> capabilities) {
		this.capabilities = capabilities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Subscription getSubscriptionById(Integer id) throws Exception{

		for (Subscription subscription : subscriptions){
			if (subscription.getId().equals(id)){
				return subscription;
			}
		}

		throw new Exception("Could not find subscription");
	}

	public Set<Subscription> getFedIn() {
		return fedIn;
	}

	public void setFedIn(Set<Subscription> fedIn) {
		this.fedIn = fedIn;
	}
}
