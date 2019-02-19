package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
public class Capability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cap_id")
	int id;

	@Column(name = "country")
	private String country;

	@OneToMany(cascade= CascadeType.ALL)
	private Set<DataType> dataSets;

	public Capability(){}

	public Capability(String country, Set<DataType> dataSets) {
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
