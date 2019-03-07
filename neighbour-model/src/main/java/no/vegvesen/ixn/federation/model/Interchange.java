package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "Interchanges")
public class Interchange {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ixn_generator")
	@SequenceGenerator(name="ixn_generator", sequenceName = "ixn_seq", allocationSize=50)
	@Column(name="ixn_id")
	private Integer ixn_id;

	@Column(unique = true)
	private String name; // common name from the certificate

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "fk_ixn_id")
	private Set<DataType> capabilities;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "fk_ixn_id")
	private Set<Subscription> subscriptions;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "fk_ixn_id")
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
