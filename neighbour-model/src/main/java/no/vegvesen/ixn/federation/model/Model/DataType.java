package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

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

	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "what")
	Set<String> what;

	public DataType(){}

	public DataType(String how, String version, Set<String> what) {
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

	public Set<String> getWhat() {
		return what;
	}

	public void setWhat(Set<String> what) {
		this.what = what;
	}
}
