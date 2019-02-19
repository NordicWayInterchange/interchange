package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class Capability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="cap_id")
	int id;

	@Column(name = "country")
	private String country;

	@OneToMany(cascade= CascadeType.ALL)
	private List<DataType> dataSets;

	public Capability(){}

	public Capability(String country, List<DataType> dataSets) {
		this.country = country;
		this.dataSets = dataSets;
	}


	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<DataType> getDataSets() {
		return dataSets;
	}

	public void setDataSets(List<DataType> dataSets) {
		this.dataSets = dataSets;
	}
}
