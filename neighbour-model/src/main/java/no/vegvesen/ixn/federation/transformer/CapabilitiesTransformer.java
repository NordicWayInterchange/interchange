package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Self;
import org.springframework.stereotype.Component;

@Component
public class CapabilitiesTransformer {

	private final CapabilityToCapabilityApiTransformer dataTypeTransformer = new CapabilityToCapabilityApiTransformer();

	public Capabilities capabilitiesApiToCapabilities(CapabilitiesApi capabilitiesApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setCapabilities(dataTypeTransformer.capabilitiesApiToCapabilities(capabilitiesApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilitiesApi selfToCapabilityApi(Self self) {
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
		capabilitiesApi.setName(self.getName());
		capabilitiesApi.setCapabilities(dataTypeTransformer.capabilitiesToCapabilityApis(self.getLocalCapabilities()));
		return capabilitiesApi;
	}

}
