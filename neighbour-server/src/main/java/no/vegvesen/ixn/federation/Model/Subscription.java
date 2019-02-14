package no.vegvesen.ixn.federation.Model;


import javax.persistence.*;
import java.util.List;

@Entity
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="sub_id")
	private int id;

	@Column(name = "country")
	private String country;

	@OneToMany(cascade= CascadeType.ALL)
	private List<DataType> dataSets;

	public Subscription(){}

	public Subscription(Interchange interchange, String country, List<DataType> dataSets) {

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