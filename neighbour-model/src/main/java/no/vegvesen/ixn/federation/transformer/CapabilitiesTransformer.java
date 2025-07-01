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
        return new Capabilities(
				dataTypeTransformer.capabilitiesApiToCapabilities(capabilitiesApi.getCapabilities())
		);
	}
	public NeighbourCapabilities capabilitiesApiToNeighbourCapabilities(CapabilitiesApi capabilitiesApi){
        return new NeighbourCapabilities(
                CapabilitiesStatus.KNOWN,
                dataTypeTransformer.capabilityApiToNeighbourCapabilities(capabilitiesApi.getCapabilities())
        );
	}

	public CapabilitiesApi selfToCapabilityApi(String name, Set<Capability> localCapabilities) {
        return new CapabilitiesApi(
                name,
                dataTypeTransformer.capabilitiesToCapabilitiesApi(localCapabilities)
        );
	}

}
