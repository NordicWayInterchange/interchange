package no.vegvesen.ixn.federation.api.v1_0;

import no.vegvesen.ixn.federation.model.*;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityTransformer {

	public Neighbour capabilityApiToNeighbour(CapabilityApi capabilityApi){

		Neighbour neighbour = new Neighbour();
		neighbour.setName(capabilityApi.getName());

		Capabilities capabilitiesObject = new Capabilities();
		capabilitiesObject.setDataTypes(dataTypeApiToDataType(capabilityApi.getCapabilities()));
		neighbour.setCapabilities(capabilitiesObject);

		return neighbour;
	}

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeApiToDataType(capabilityApi.getCapabilities()));
		return capabilities;
	}

	public ServiceProvider capabilityApiToServiceProvider(CapabilityApi capabilityApi){

		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setName(capabilityApi.getName());

		Capabilities serviceProviderCapabilities = new Capabilities();
		serviceProviderCapabilities.setDataTypes(dataTypeApiToDataType(capabilityApi.getCapabilities()));
		serviceProvider.setCapabilities(serviceProviderCapabilities);

		return serviceProvider;
	}
	public CapabilityApi serviceProviderToCapabilityApi(ServiceProvider serviceProvider){

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(serviceProvider.getName());
		capabilityApi.setCapabilities(dataTypeToDataTypeApi(serviceProvider.getCapabilities().getDataTypes()));

		return capabilityApi;
	}

	public CapabilityApi selfToCapabilityApi(Self self){

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypeToDataTypeApi(self.getLocalCapabilities()));
		return capabilityApi;
	}

	public Set<DataTypeApi> dataTypeToDataTypeApi(Set<DataType> dataTypes) {
		Set<DataTypeApi> apis = new HashSet<>();
		for (DataType dataType : dataTypes) {
			apis.add(new DataTypeApi(dataType.getHow(), dataType.getWhere()));
		}
		return apis;
	}

	public Set<DataType> dataTypeApiToDataType(Set<DataTypeApi> capabilities) {
		Set<DataType> dataTypes = new HashSet<>();
		for (DataTypeApi capability : capabilities) {
			dataTypes.add(new DataType(capability.getHow(), capability.getWhere()));
		}
		return dataTypes;
	}
}
