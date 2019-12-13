package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class CapabilityTransformer {

	private static Logger logger = LoggerFactory.getLogger(CapabilityTransformer.class);

	public Neighbour capabilityApiToNeighbour(CapabilityApi capabilityApi) {
		Neighbour neighbour = new Neighbour();
		neighbour.setName(capabilityApi.getName());
		neighbour.setCapabilities(toCapabilities(capabilityApi));
		return neighbour;
	}

	public Capabilities capabilityApiToCapabilities(CapabilityApi capabilityApi) {
		Capabilities capabilities = new Capabilities();
		capabilities.setDataTypes(dataTypeApiToDataType(capabilityApi.getCapabilities()));
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
		capabilitiesObject.setDataTypes(dataTypeApiToDataType(capabilityApi.getCapabilities()));
		return capabilitiesObject;
	}

	public CapabilityApi serviceProviderToCapabilityApi(ServiceProvider serviceProvider) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(serviceProvider.getName());
		capabilityApi.setCapabilities(dataTypeToDataTypeApi(serviceProvider.getCapabilities().getDataTypes()));

		return capabilityApi;
	}

	public CapabilityApi selfToCapabilityApi(Self self) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypeToDataTypeApi(self.getLocalCapabilities()));
		return capabilityApi;
	}

	Set<DataTypeApi> dataTypeToDataTypeApi(Set<DataType> dataTypes) {
		Set<DataTypeApi> apis = new HashSet<>();
		for (DataType dataType : dataTypes) {
			DataTypeApi dataTypeApi;
			if (dataType.getMessageType().equals(Datex2DataTypeApi.DATEX_2)) {
				dataTypeApi = new Datex2DataTypeApi(dataType.getOriginatingCountry(), dataType.getPublicationType(), toArray(dataType.getPublicationSubTypes()));
			} else {
				logger.warn("Unknown message type to be converted to API data type: {}", dataType);
				dataTypeApi = new DataTypeApi(dataType.getMessageType(), dataType.getOriginatingCountry());
			}
			apis.add(dataTypeApi);
		}
		return apis;
	}

	private String[] toArray(String commaSeparatedString) {
		return commaSeparatedString == null ? null : commaSeparatedString
				.replaceAll("\\A,", "")
				.replaceAll(",\\z", "")
				.split(",");
	}

	public Set<DataType> dataTypeApiToDataType(Set<? extends DataTypeApi> capabilities) {
		Set<DataType> dataTypes = new HashSet<>();
		for (DataTypeApi capability : capabilities) {
			DataType dataType;
			if (capability.getMessageType().equals(Datex2DataTypeApi.DATEX_2)) {
				Datex2DataTypeApi datexApi = (Datex2DataTypeApi) capability;
				dataType = new DataType(datexApi.getMessageType(), datexApi.getOriginatingCountry(), datexApi.getPublicationType(), toDelimitedString(datexApi.getPublicationSubType()));
			} else {
				logger.warn("Unknown message type {}", capability.getMessageType());
				dataType = new DataType(capability.getMessageType(), capability.getOriginatingCountry());
			}
			dataTypes.add(dataType);
		}
		return dataTypes;
	}

	private String toDelimitedString(String[] publicationSubType) {
		return publicationSubType == null || publicationSubType.length == 0 ? "" : "," + String.join(",", publicationSubType) + ",";
	}

}
