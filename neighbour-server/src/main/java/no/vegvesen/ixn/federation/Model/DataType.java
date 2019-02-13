package no.vegvesen.ixn.federation.Model;

import java.util.List;

public class DataType {

	private String how;
	private String version;
	private List<String> what;


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
