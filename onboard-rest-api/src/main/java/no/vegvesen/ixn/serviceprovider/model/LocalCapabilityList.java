package no.vegvesen.ixn.serviceprovider.model;

import java.util.LinkedList;
import java.util.List;

public class LocalCapabilityList {
	List<LocalCapability> capabilities = new LinkedList<>();

	public LocalCapabilityList() {
	}

	public LocalCapabilityList(List<LocalCapability> capabilities) {
		this.capabilities = capabilities;
	}

	public List<LocalCapability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(List<LocalCapability> capabilities) {
		this.capabilities = capabilities;
	}
}
