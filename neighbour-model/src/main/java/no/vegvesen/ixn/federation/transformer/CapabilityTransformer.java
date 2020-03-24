package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CapabilityTransformer {

	private final DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

	public Neighbour capabilityApiToNeighbour(CapabilityApi capabilityApi) {
		Neighbour neighbour = new Neighbour();
		neighbour.setName(capabilityApi.getName());
		neighbour.setCapabilities(capabilityApiToCapabilities(capabilityApi));
		return neighbour;
	}

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeTransformer.dataTypeApiToDataType(capabilityApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		capabilities.setLastCapabilityExchange(LocalDateTime.now());
		return capabilities;
	}

	public CapabilityApi selfToCapabilityApi(Self self) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypeTransformer.dataTypesToDataTypeApis(self.getLocalCapabilities()));
		return capabilityApi;
	}

}
