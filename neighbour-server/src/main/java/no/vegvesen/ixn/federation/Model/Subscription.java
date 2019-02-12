package idaberge.springbootrestapi.Model;
import java.util.List;

public class Subscription {

	private String country;
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
