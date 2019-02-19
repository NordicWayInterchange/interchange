package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sub_id")
	int id;

	@Column(name = "country")
	private String country;

	@OneToMany(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
	private Set<DataType> dataSets;

	public Subscription(){}

	public Subscription(Interchange interchange, String country, Set<DataType> dataSets) {

		this.country = country;
		this.dataSets = dataSets;
	}


	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Set<DataType> getDataSets() {
		return dataSets;
	}

	public void setDataSets(Set<DataType> dataSets) {
		this.dataSets = dataSets;
	}
}
