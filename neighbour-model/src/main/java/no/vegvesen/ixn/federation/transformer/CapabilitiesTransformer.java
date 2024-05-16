package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.CapabilitiesStatus;
import no.vegvesen.ixn.federation.model.NeighbourCapabilities;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilitiesTransformer {

	private final CapabilityToCapabilityApiTransformer dataTypeTransformer = new CapabilityToCapabilityApiTransformer();

	public Capabilities capabilitiesApiToCapabilities(CapabilitiesSplitApi capabilitiesApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilitiesSplitApiToCapabilitiesSplit(capabilitiesApi.getCapabilities()));
		return capabilities;
	}
	public NeighbourCapabilities capabilitiesApiToNeighbourCapabilities(CapabilitiesSplitApi capabilitiesApi){
		NeighbourCapabilities capabilities = new NeighbourCapabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilitySplitApiToNeighbourCapabilities(capabilitiesApi.getCapabilities()));
		capabilities.setStatus(CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilitiesSplitApi selfToCapabilityApi(String name, Set<CapabilitySplit> localCapabilities) {
		CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi();
		capabilitiesApi.setName(name);
		capabilitiesApi.setCapabilities(dataTypeTransformer.capabilitiesSplitToCapabilitiesSplitApi(localCapabilities));
		return capabilitiesApi;
	}

}
