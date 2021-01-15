package no.vegvesen.ixn.federation.api.v1_0;

import java.util.HashSet;
import java.util.Set;


public class CapabilitiesApi {

	private String version = "1.1";
	private String name;
	private Set<CapabilityApi> capabilities = new HashSet<>();

	public CapabilitiesApi() {
	}

	public CapabilitiesApi(String name, Set<CapabilityApi> capabilities) {
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

	public Set<CapabilityApi> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<CapabilityApi> capabilities) {
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
