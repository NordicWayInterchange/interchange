package no.vegvesen.ixn.federation.Model;

import java.util.List;

public class Capability {

	private String country;
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
