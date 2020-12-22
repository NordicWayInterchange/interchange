package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Self;
import org.springframework.stereotype.Component;

@Component
public class CapabilitiesTransformer {

	private final DataTypeCapabilityApiTransformer dataTypeTransformer = new DataTypeCapabilityApiTransformer();

	public Capabilities capabilitiesApiToCapabilities(CapabilitiesApi capabilitiesApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeTransformer.capabilityApiToDataType(capabilitiesApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilitiesApi selfToCapabilityApi(Self self) {
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
		capabilitiesApi.setName(self.getName());
		capabilitiesApi.setCapabilities(dataTypeTransformer.dataTypesTocapabilityApis(self.getLocalCapabilities()));
		return capabilitiesApi;
	}

}
