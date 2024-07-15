package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.CapabilitiesStatus;
import no.vegvesen.ixn.federation.model.NeighbourCapabilities;
import no.vegvesen.ixn.federation.model.capability.Capability;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilitiesTransformer {

	private final CapabilityToCapabilityApiTransformer dataTypeTransformer = new CapabilityToCapabilityApiTransformer();

	public Capabilities capabilitiesApiToCapabilities(CapabilitiesApi capabilitiesApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilitiesApiToCapabilities(capabilitiesApi.getCapabilities()));
		return capabilities;
	}
	public NeighbourCapabilities capabilitiesApiToNeighbourCapabilities(CapabilitiesApi capabilitiesApi){
		NeighbourCapabilities capabilities = new NeighbourCapabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilityApiToNeighbourCapabilities(capabilitiesApi.getCapabilities()));
		capabilities.setStatus(CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilitiesApi selfToCapabilityApi(String name, Set<Capability> localCapabilities) {
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
		capabilitiesApi.setName(name);
		capabilitiesApi.setCapabilities(dataTypeTransformer.capabilitiesToCapabilitiesApi(localCapabilities));
		return capabilitiesApi;
	}

}
