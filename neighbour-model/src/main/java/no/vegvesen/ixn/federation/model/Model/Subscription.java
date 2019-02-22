package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;

@Entity
@Table(name = "Subscriptions")
@DiscriminatorValue("Subscription")
public class Subscription extends Capability {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sub_generator")
	@SequenceGenerator(name="sub_generator", sequenceName = "sub_seq", allocationSize=50)
	@Column(name="sub_id")
	int id;

	@Column(name = "path")
	private String path;

	@Column(name = "status")
	private String status;

	public Subscription(){}

	public Subscription(String country, DataType dataSet, String path, String status) {
		super(country, dataSet);
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
}
