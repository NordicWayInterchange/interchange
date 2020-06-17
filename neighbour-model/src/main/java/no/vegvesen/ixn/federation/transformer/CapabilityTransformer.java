package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.DataType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CapabilityTransformer {

	private final DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeTransformer.dataTypeApiToDataType(capabilityApi.getCapabilities()));
		capabilities.setStatus(Capabilities.CapabilitiesStatus.KNOWN);
		return capabilities;
	}

	public CapabilityApi selfToCapabilityApi(String selfName, Set<DataType> localCapabilities) {
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(selfName);
		capabilityApi.setCapabilities(dataTypeTransformer.dataTypesToDataTypeApis(localCapabilities));
		return capabilityApi;
	}

}
