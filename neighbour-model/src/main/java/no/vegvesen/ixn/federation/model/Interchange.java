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

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_fed_in", foreignKey = @ForeignKey(name="fk_sub_ixn_fed_in"))
	private Set<Subscription> fedIn;

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	@JsonIgnore
	private LocalDateTime lastSeen;

	public enum InterchangeStatus {NEW, KNOWN, FEDERATED}

	@Enumerated(EnumType.STRING)
	private InterchangeStatus interchangeStatus;

	@JsonIgnore
	private String domainName;
	@JsonIgnore
	private String messageChannelPort;
	@JsonIgnore
	private String controlChannelPort;

	public Interchange(){}

	public Interchange(String name, Set<DataType> capabilities, Set<Subscription> subscriptions, Set<Subscription> fedIn) {
		this.name = name;
		this.capabilities = capabilities;
		this.subscriptions = subscriptions;
		this.fedIn = fedIn;
		this.interchangeStatus = InterchangeStatus.NEW;
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

	public InterchangeStatus getInterchangeStatus() {
		return interchangeStatus;
	}

	public void setInterchangeStatus(InterchangeStatus interchangeStatus) {
		this.interchangeStatus = interchangeStatus;
	}

	public Set<Subscription> getFedIn() {
		return fedIn;
	}

	public void setFedIn(Set<Subscription> fedIn) {
		this.fedIn = fedIn;
	}

	public LocalDateTime getLastSeen() {
		return lastSeen;
	}

	public void setLastSeen(LocalDateTime lastSeen) {
		this.lastSeen = lastSeen;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
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
}
