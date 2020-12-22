package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class DataTypeCapabilityApiTransformer {
	private static Logger logger = LoggerFactory.getLogger(DataTypeCapabilityApiTransformer.class);

	public DataTypeCapabilityApiTransformer() {
	}

	Set<CapabilityApi> dataTypesTocapabilityApis(Set<DataType> dataTypes) {
		Set<CapabilityApi> apis = new HashSet<>();
		for (DataType dataType : dataTypes) {
			CapabilityApi capabilityApi = dataTypeToCapabilityApi(dataType);
			if (capabilityApi != null) {
				apis.add(capabilityApi);
			}
		}
		return apis;
	}

	public CapabilityApi dataTypeToCapabilityApi(DataType dataType) {
		String messageType = dataType.getPropertyValue(MessageProperty.MESSAGE_TYPE);
		if (messageType == null) {
			logger.error("Can not convert DataType to CapabilityApi when no value for {}. DataType.id {}, values: {}",
					MessageProperty.MESSAGE_TYPE.getName(), dataType.getData_id(), dataType.getValues());
			return null;
		}
		switch (messageType) {
			case CapabilityApi.DATEX_2:
				return new DatexCapabilityApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValueAsSet(MessageProperty.PUBLICATION_TYPE));
			case DenmDataTypeApi.DENM:
				return new DenmCapabilityApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValueAsSet(MessageProperty.CAUSE_CODE));
			case IviDataTypeApi.IVI:
				return new IviCapabilityApi(
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE),
						dataType.getPropertyValueAsSet(MessageProperty.IVI_TYPE));
			default:
				logger.warn("Unknown message type to be converted to API data type: {}", messageType);
				return new CapabilityApi(
						messageType,
						dataType.getPropertyValue(MessageProperty.PUBLISHER_ID),
						dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY),
						dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION),
						dataType.getPropertyValueAsSet(MessageProperty.QUAD_TREE)
				);
		}
	}

	public Set<DataType> capabilityApiToDataType(Set<? extends CapabilityApi> capabilities) {
		Set<DataType> dataTypes = new HashSet<>();
		for (CapabilityApi capability : capabilities) {
			logger.debug("Converting message type {}", capability.getMessageType());
			dataTypes.add(capabilityApiToDataType(capability));
		}
		return dataTypes;
	}

	private DataType capabilityApiToDataType(CapabilityApi capability) {
		return new DataType(capability.getValues());
	}


}