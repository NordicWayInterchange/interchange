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
	Integer id;

	@Column(unique = true)
	private String name;
	private String hostname;
	private String portNr;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "ixn_id")
	private Set<DataType> capabilities;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "ixn_id")
	private Set<Subscription> subscriptions;

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "ixn_id")
	private Set<Subscription> fedIn;

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	public Interchange(){}

	public Interchange(String name, Set<DataType> capabilities, Set<Subscription> subscriptions, Set<Subscription> fedIn, String hostname, String portNr) {
		this.name = name;
		this.hostname = hostname;
		this.portNr = portNr;
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

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPortNr() {
		return portNr;
	}

	public void setPortNr(String portNr) {
		this.portNr = portNr;
	}

	public Set<Subscription> getSubscriptions() {
		return subscriptions;
	}

	public void setSubscriptions(Set<Subscription> subscriptions) {
		this.subscriptions = subscriptions;
	}
}
