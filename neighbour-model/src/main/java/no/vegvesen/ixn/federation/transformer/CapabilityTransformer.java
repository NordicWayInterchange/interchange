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
			String messageType = dataType.getPropertyValue(MessageProperty.MESSAGE_TYPE);
			if (messageType != null) {
				switch (messageType) {
					case Datex2DataTypeApi.DATEX_2:
						dataTypeApi = new Datex2DataTypeApi(
								dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
								dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
								dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
								dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
								dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
								dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
								dataType.getPropertyValue(MessageProperty.PUBLICATION_TYPE),
								dataType.getPropertyValueAsSet(MessageProperty.PUBLICATION_SUB_TYPE));
						break;
					case DenmDataTypeApi.DENM:
						dataTypeApi = new DenmDataTypeApi(
								dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
								dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
								dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
								dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
								dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
								dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
								dataType.getPropertyValue(MessageProperty.SERVICE_TYPE),
								dataType.getPropertyValue(MessageProperty.CAUSE_CODE),
								dataType.getPropertyValue(MessageProperty.SUB_CAUSE_CODE));
						break;
					case IvyDataTypeApi.IVY:
						dataTypeApi = new IvyDataTypeApi(
								dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
								dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
								dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
								dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
								dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
								dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
								dataType.getPropertyValue(MessageProperty.SERVICE_TYPE),
								dataType.getPropertyValueAsInteger(MessageProperty.IVI_TYPE),
								dataType.getPropertyValueAsIntegerSet(MessageProperty.PICTOGRAM_CATEGORY_CODE));
						break;
					default:
						logger.warn("Unknown message type to be converted to API data type: {}", messageType);
						dataTypeApi = new DataTypeApi(
								messageType,
								dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
								dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME),
								dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
								dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
								dataType.getPropertyValue(MessageProperty.CONTENT_TYPE),
								dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE)
						);
						break;
				}
				apis.add(dataTypeApi);
			} else {
				logger.error("Can not convert DataType to DataTypeApi when no value for {}. DataType.id {}, values: {}",
						MessageProperty.MESSAGE_TYPE.getName(), dataType.getData_id(), dataType.getValues());
			}
		}
		return apis;
	}

	public Set<DataType> dataTypeApiToDataType(Set<? extends DataTypeApi> capabilities) {
		Set<DataType> dataTypes = new HashSet<>();
		for (DataTypeApi capability : capabilities) {
			logger.debug("Converting message type {}", capability.getMessageType());
			dataTypes.add(new DataType(capability.getValues()));
		}
		return dataTypes;
	}

}
