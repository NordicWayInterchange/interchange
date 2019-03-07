package no.vegvesen.ixn.federation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;

@Entity
@Table(name = "Subscriptions")
public class Subscription {


	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name="sub_generator", sequenceName = "sub_seq", allocationSize=50)
	@Column(name="sub_id")
	private Integer sub_id;

	public enum Status{REQUESTED, CREATED, REJECTED}

	@Enumerated(EnumType.STRING)
	private Status status;

	private String selector;

	public Subscription(){}

	public Subscription(String selector, Status status) {
		this.selector = selector;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@JsonIgnore
	public Integer getId() {
		return sub_id;
	}


}
