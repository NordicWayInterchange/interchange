package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;

public class LocalCapability {
	Integer id;
	CapabilityApi capabilityApi;

	public LocalCapability() {
	}

	public LocalCapability(Integer id, CapabilityApi capabilityApi) {
		this.id = id;
		this.capabilityApi = capabilityApi;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CapabilityApi getCapabilityApi() {
		return capabilityApi;
	}

	public void setCapabilityApi(CapabilityApi capabilityApi) {
		this.capabilityApi = capabilityApi;
	}
}
