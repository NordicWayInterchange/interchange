package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.springframework.stereotype.Component;

@Component
public class CapabilityTransformer {

	private final DataTypeTransformer dataTypeTransformer = new DataTypeTransformer();

	public Neighbour capabilityApiToNeighbour(CapabilityApi capabilityApi) {
		Neighbour neighbour = new Neighbour();
		neighbour.setName(capabilityApi.getName());
		neighbour.setCapabilities(toCapabilities(capabilityApi));
		return neighbour;
	}

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeTransformer.dataTypeApiToDataType(capabilityApi.getCapabilities()));
		return capabilities;
	}

	public ServiceProvider capabilityApiToServiceProvider(CapabilityApi capabilityApi) {
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName(capabilityApi.getName());
		serviceProvider.setCapabilities(toCapabilities(capabilityApi));
		return serviceProvider;
	}

	private Capabilities toCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilitiesObject = new Capabilities();
		capabilitiesObject.setDataTypes(dataTypeTransformer.dataTypeApiToDataType(capabilityApi.getCapabilities()));
		return capabilitiesObject;
	}

	public CapabilityApi serviceProviderToCapabilityApi(ServiceProvider serviceProvider) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(serviceProvider.getName());
		capabilityApi.setCapabilities(dataTypeTransformer.dataTypesToDataTypeApis(serviceProvider.getCapabilities().getDataTypes()));

		return capabilityApi;
	}

	public CapabilityApi selfToCapabilityApi(Self self) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypeTransformer.dataTypesToDataTypeApis(self.getLocalCapabilities()));
		return capabilityApi;
	}

}
