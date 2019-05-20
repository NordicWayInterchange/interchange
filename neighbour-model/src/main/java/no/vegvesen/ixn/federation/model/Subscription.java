package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "subscriptions")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name = "sub_generator", sequenceName = "sub_seq")
	@Column(name = "sub_id")
	private Integer sub_id;

	public enum SubscriptionStatus {REQUESTED, ACCEPTED, CREATED, ILLEGAL, NOT_VALID, NO_OVERLAP, GIVE_UP, FAILED, UNREACHABLE, REJECTED}

	@Enumerated(EnumType.STRING)
	private SubscriptionStatus status;

	private String selector;

	private String path;

	@JsonIgnore
	private int numberOfPolls = 0;

	public Subscription() {
	}

	public Subscription(String selector, SubscriptionStatus status) {
		this.selector = selector;
		this.status = status;
	}

	public SubscriptionStatus getStatus() {
		return status;
	}

	public void setStatus(SubscriptionStatus status) {
		this.status = status;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public Integer getId() {
		return sub_id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@JsonIgnore
	public int getNumberOfPolls() {
		return numberOfPolls;
	}

	public void setNumberOfPolls(int numberOfPolls) {
		this.numberOfPolls = numberOfPolls;
	}

	@Override
	public String toString() {
		return "Subscription{" +
				"sub_id=" + sub_id +
				", status=" + status +
				", selector='" + selector + '\'' +
				", path='" + path + '\'' +
				'}';
	}
}
