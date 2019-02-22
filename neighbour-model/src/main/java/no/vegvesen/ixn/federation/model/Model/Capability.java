package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "Capabilities")
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "type")
public class Capability {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cap_generator")
	@SequenceGenerator(name="cap_generator", sequenceName = "cap_seq", allocationSize=50)
	@Column(name="cap_id")
	int id;

	@Column(name = "country")
	private String country;

	@OneToOne(cascade = CascadeType.ALL)
	private DataType dataSet;

	public Capability(){}

	public Capability(String country, DataType dataSet) {
		this.country = country;
		this.dataSet = dataSet;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public DataType getDataSet() {
		return dataSet;
	}

	public void setDataSets(DataType dataSet) {
		this.dataSet = dataSet;
	}
}
