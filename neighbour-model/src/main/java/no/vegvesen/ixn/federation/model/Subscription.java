package no.vegvesen.ixn.federation.model;

import javax.persistence.*;

@Entity
@Table(name = "Subscriptions")
@DiscriminatorValue("Subscription")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name="sub_generator", sequenceName = "sub_seq", allocationSize=50)
	@Column(name="sub_id")
	int id;

	private String path;
	private String status;
	private String selector;

	public Subscription(){}

	public Subscription(String country, String selector, String path, String status) {
		this.selector = selector;
		this.path = path;
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
}
