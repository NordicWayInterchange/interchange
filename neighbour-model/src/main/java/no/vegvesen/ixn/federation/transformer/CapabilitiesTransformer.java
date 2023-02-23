package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Capability;
import no.vegvesen.ixn.federation.model.NeighbourCapabilities;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilitiesTransformer {

	private final CapabilityToCapabilityApiTransformer dataTypeTransformer = new CapabilityToCapabilityApiTransformer();

	public Capabilities capabilitiesApiToCapabilities(CapabilitiesApi capabilitiesApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilitiesApiToCapabilities(capabilitiesApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public NeighbourCapabilities capabilitiesApiToNeighbourCapabilities(CapabilitiesApi capabilitiesApi) {
		NeighbourCapabilities neighbourCapabilities = new NeighbourCapabilities();
		neighbourCapabilities.setCapabilities(dataTypeTransformer.capabilitiesApiToCapabilities(capabilitiesApi.getCapabilities()));
		neighbourCapabilities.setStatus(NeighbourCapabilities.NeighbourCapabilitiesStatus.KNOWN);
		return neighbourCapabilities;
	}

	public CapabilitiesApi selfToCapabilityApi(String name, Set<Capability> localCapabilities) {
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
		capabilitiesApi.setName(name);
		capabilitiesApi.setCapabilities(dataTypeTransformer.capabilitiesToCapabilityApis(localCapabilities));
		return capabilitiesApi;
	}

}
