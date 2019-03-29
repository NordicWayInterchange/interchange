package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.bytebuddy.asm.Advice;
import no.vegvesen.ixn.federation.exceptions.SubscriptionNotFoundException;
import org.hibernate.annotations.UpdateTimestamp;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(	name = "interchanges",
		uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "uk_ixn_name"))
public class Interchange {

	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ixn_generator")
	@SequenceGenerator(name="ixn_generator", sequenceName = "ixn_seq", allocationSize=50)
	@Column(name="ixn_id")
	private Integer ixn_id;

	private String name = ""; // common name from the certificate

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_cap", foreignKey = @ForeignKey(name="fk_dat_ixn"))
	private Set<DataType> capabilities = Collections.emptySet();

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_sub_out", foreignKey = @ForeignKey(name = "fk_sub_ixn_sub_out"))
	private Set<Subscription> subscriptions = Collections.emptySet();

	@JsonIgnore
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "ixn_id_fed_in", foreignKey = @ForeignKey(name="fk_sub_ixn_fed_in"))
	private Set<Subscription> fedIn = Collections.emptySet();

	@UpdateTimestamp
	private LocalDateTime lastUpdated;

	@JsonIgnore
	private LocalDateTime lastSeen;

	@JsonIgnore
	private LocalDateTime backoffStart;

	@JsonIgnore
	private int backoffAttempts = 0;

	public enum InterchangeStatus {NEW, KNOWN, FEDERATED, UNREACHABLE,
		FAILED_CAPABILITY_EXCHANGE, FAILED_SUBSCRIPTION_REQUEST}


	@Enumerated(EnumType.STRING)
	private InterchangeStatus interchangeStatus = InterchangeStatus.NEW;

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

	public Subscription getSubscriptionById(Integer id) throws SubscriptionNotFoundException{

		for (Subscription subscription : subscriptions){
			if (subscription.getId().equals(id)){
				return subscription;
			}
		}

		throw new SubscriptionNotFoundException("Could not find subscription with id " + id + " on interchange " + name);
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

	public LocalDateTime getBackoffStartTime() {
		return backoffStart;
	}

	public void setBackoffStart(LocalDateTime backoffStart) {
		this.backoffStart = backoffStart;
	}

	public int getBackoffAttempts() {
		return backoffAttempts;
	}

	public void setBackoffAttempts(int backoffAttempts) {
		this.backoffAttempts = backoffAttempts;
	}

	@Override
	public String toString() {
		return "Interchange{" +
				"ixn_id=" + ixn_id +
				", name='" + name + '\'' +
				", capabilities=" + capabilities +
				", subscriptions=" + subscriptions +
				", fedIn=" + fedIn +
				", lastUpdated=" + lastUpdated +
				", lastSeen=" + lastSeen +
				", backoffStart=" + backoffStart +
				", backoffAttempts=" + backoffAttempts +
				", interchangeStatus=" + interchangeStatus +
				", domainName='" + domainName + '\'' +
				", messageChannelPort='" + messageChannelPort + '\'' +
				", controlChannelPort='" + controlChannelPort + '\'' +
				'}';
	}
}
