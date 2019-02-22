package no.vegvesen.ixn.federation.model.Model;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "DataTypes")
public class DataType {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "datatype_generator")
	@SequenceGenerator(name="datatype_generator", sequenceName = "datatype_seq", allocationSize=50)
	@Column(name="data_id")
	int id;

	@Column(name = "how")
	private String how;

	@Column(name= "version")
	private String version;

	@ElementCollection(fetch = FetchType.EAGER)
	@Column(name = "what")
	private Set<String> what;

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
