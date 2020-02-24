package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.properties.MessageProperty;
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
		capabilityApi.setCapabilities(dataTypesToDataTypeApis(serviceProvider.getCapabilities().getDataTypes()));

		return capabilityApi;
	}

	public CapabilityApi selfToCapabilityApi(Self self) {

		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName(self.getName());
		capabilityApi.setCapabilities(dataTypesToDataTypeApis(self.getLocalCapabilities()));
		return capabilityApi;
	}

	Set<DataTypeApi> dataTypesToDataTypeApis(Set<DataType> dataTypes) {
		Set<DataTypeApi> apis = new HashSet<>();
		for (DataType dataType : dataTypes) {
			DataTypeApi dataTypeApi = dataTypeToApi(dataType);
			if (dataTypeApi != null) {
				apis.add(dataTypeApi);
			}
		}
		return apis;
	}

	public DataTypeApi dataTypeToApi(DataType dataType) {
		String messageType = dataType.getPropertyValue(MessageProperty.MESSAGE_TYPE);
		if (messageType == null) {
			logger.error("Can not convert DataType to DataTypeApi when no value for {}. DataType.id {}, values: {}",
					MessageProperty.MESSAGE_TYPE.getName(), dataType.getData_id(), dataType.getValues());
			return null;
		}
		switch (messageType) {
			case Datex2DataTypeApi.DATEX_2:
				return new Datex2DataTypeApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValue(MessageProperty.PUBLICATION_TYPE),
						dataType.getPropertyValueAsSet(MessageProperty.PUBLICATION_SUB_TYPE));
			case DenmDataTypeApi.DENM:
				return new DenmDataTypeApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValue(MessageProperty.SERVICE_TYPE),
						dataType.getPropertyValue(MessageProperty.CAUSE_CODE),
						dataType.getPropertyValue(MessageProperty.SUB_CAUSE_CODE));
			case IviDataTypeApi.IVI:
				return new IviDataTypeApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValue(MessageProperty.SERVICE_TYPE),
						dataType.getPropertyValueAsInteger(MessageProperty.IVI_TYPE),
						dataType.getPropertyValueAsIntegerSet(MessageProperty.PICTOGRAM_CATEGORY_CODE));
			default:
				logger.warn("Unknown message type to be converted to API data type: {}", messageType);
				return new DataTypeApi(
						messageType,
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE)
				);
		}
	}

	public Set<DataType> dataTypeApiToDataType(Set<? extends DataTypeApi> capabilities) {
		Set<DataType> dataTypes = new HashSet<>();
		for (DataTypeApi capability : capabilities) {
			logger.debug("Converting message type {}", capability.getMessageType());
			dataTypes.add(dataTypeApiToDataType(capability));
		}
		return dataTypes;
	}

	public DataType dataTypeApiToDataType(DataTypeApi capability) {
		return new DataType(capability.getValues());
	}

}
