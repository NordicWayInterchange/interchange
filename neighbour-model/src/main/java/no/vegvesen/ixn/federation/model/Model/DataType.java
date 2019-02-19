package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class DataType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="data_id")
	int id;

	@Column(name = "how")
	String how;

	@Column(name= "version")
	String version;

	@ElementCollection
	@Column(name = "what")
	List<String> what;

	public DataType(){}

	public DataType(String how, String version, List<String> what) {
		this.how = how;
		this.version = version;
		this.what = what;
	}

	public String getHow() {
		return how;
	}

	public void setHow(String how) {
		this.how = how;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<String> getWhat() {
		return what;
	}

	public void setWhat(List<String> what) {
		this.what = what;
	}
}
