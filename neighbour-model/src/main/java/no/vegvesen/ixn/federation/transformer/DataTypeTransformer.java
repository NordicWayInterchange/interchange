package no.vegvesen.ixn.federation.transformer;

/*-
 * #%L
 * neighbour-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmDataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.IviDataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class DataTypeTransformer {
	private static Logger logger = LoggerFactory.getLogger(DataTypeTransformer.class);

	public DataTypeTransformer() {
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
