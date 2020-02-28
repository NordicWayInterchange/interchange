package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;


public class CapabilityApi{

	private String version = "1.0";
	private String name;
	private Set<DataTypeApi> capabilities = new HashSet<>();

	public CapabilityApi() {
	}

	public CapabilityApi(String name, Set<? extends DataTypeApi> capabilities) {
		this.name = name;
		this.capabilities.addAll(capabilities);
	}

	public String getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<DataTypeApi> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<? extends DataTypeApi> capabilities) {
		if (this.capabilities == null) {
			this.capabilities = new HashSet<>();
		}
		this.capabilities.clear();
		this.capabilities.addAll(capabilities);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "CapabilityApi{" +
				"version='" + version + '\''+
				", name='" + name + '\'' +
				", capabilities=" + capabilities +
				'}';
	}
}
