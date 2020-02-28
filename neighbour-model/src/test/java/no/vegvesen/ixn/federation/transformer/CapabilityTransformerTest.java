package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DenmDataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.IviDataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.properties.MessageProperty;
import org.assertj.core.util.Sets;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityTransformerTest {
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	@Test
	public void capabilityApiIsConvertedToDataTypes(){
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		String travelTimeData = "TravelTimeData";
		String siteMeasurements = "SiteMeasurements";
		String publisherId = "NO-91247";
		String publisherName = "Some norwegian publisher name";
		String protocolVersion = "pv2";
		String contentType = "ct-3";
		DataTypeApi capability = new Datex2DataTypeApi(publisherId, publisherName, originatingCountry, protocolVersion, contentType, Collections.emptySet(), publicationType, Sets.newLinkedHashSet(siteMeasurements, travelTimeData));
		Set<DataTypeApi> capabilities = Collections.singleton(capability);

		Set<DataType> dataTypes = capabilityTransformer.dataTypeApiToDataType(capabilities);

		assertThat(dataTypes).hasSize(1);
		DataType dataType = dataTypes.iterator().next();
		assertThat(dataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY)).isEqualTo(originatingCountry);
		assertThat(dataType.getPropertyValue(MessageProperty.PUBLICATION_TYPE)).isEqualTo(publicationType);
		assertThat(dataType.getPropertyValueAsSet(MessageProperty.PUBLICATION_SUB_TYPE)).hasSize(2).contains(siteMeasurements).contains(travelTimeData);
		assertThat(dataType.getValues().containsKey(MessageProperty.QUAD_TREE.getName())).isFalse();
		assertThat(dataType.getPropertyValue(MessageProperty.PUBLISHER_ID)).isEqualTo(publisherId);
		assertThat(dataType.getPropertyValue(MessageProperty.PUBLISHER_NAME)).isEqualTo(publisherName);
		assertThat(dataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION)).isEqualTo(protocolVersion);
	}

	@Test
	public void dataTypeIsConvertedToApi(){
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		final String publicationSubType = ",SiteMeasurements,TravelTimeData,";
		DataType dataType = getDatexHeaders(originatingCountry, publicationType, publicationSubType);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(Collections.singleton(dataType));

		assertThat(dataTypeApis).hasSize(1);
		DataTypeApi dataTypeApi = dataTypeApis.iterator().next();
		assertThat(dataTypeApi).isInstanceOf(Datex2DataTypeApi.class);
		assertThat(((Datex2DataTypeApi)dataTypeApi).getPublicationType()).isEqualTo(publicationType);
		assertThat(((Datex2DataTypeApi)dataTypeApi).getPublicationSubType()).hasSize(2);
	}

	@Test
	public void unknownDataTypeIsConvertedToApi() {
		final String messageType = "myfantasticmessagetype";
		final String originatingCountry = "ON";
		HashMap<String, String> dataTypeHeaders = new HashMap<>();
		dataTypeHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), messageType);
		dataTypeHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);

		DataType dataType = new DataType(dataTypeHeaders);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(Collections.singleton(dataType));

		assertThat(dataTypeApis).hasSize(1);
		DataTypeApi dataTypeApi = dataTypeApis.iterator().next();
		assertThat(dataTypeApi).isInstanceOf(DataTypeApi.class);
		assertThat(dataTypeApi.getMessageType()).isEqualTo(messageType);
		assertThat(dataTypeApi.getOriginatingCountry()).isEqualTo(originatingCountry);
	}

	@Test
	public void dataTypeWithQuadTreeIsConvertedToDatex() {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), null);
		datexHeaders.put(MessageProperty.QUAD_TREE.getName(), "abc,bcd");
		datexHeaders.put(MessageProperty.PUBLICATION_TYPE.getName(), "myPublication");
		DataType datexWithQuad = new DataType(datexHeaders);
		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(Sets.newLinkedHashSet(datexWithQuad));
		assertThat(dataTypeApis).hasSize(1);
		assertThat(dataTypeApis.iterator().next().getQuadTree()).hasSize(2).contains("abc").contains("bcd");
	}

	@Test
	public void dataTypeApiWithQuadIsConvertedToDataType() {
		DataTypeApi dataTypeApi = new DataTypeApi("myQuadMessageType", "myPublisherId", "myPublisherName", "no", "pv3", "ct3", Sets.newLinkedHashSet("aaa", "bbb"));
		Set<DataType> dataTypes = capabilityTransformer.dataTypeApiToDataType(Sets.newLinkedHashSet(dataTypeApi));
		assertThat(dataTypes).hasSize(1);
		assertThat(dataTypes.iterator().next().getPropertyValueAsSet(MessageProperty.QUAD_TREE)).hasSize(2);
	}

	@Test
	public void datexDataTypeApiWithoutPulicationSubTypeReturnsNoPropertyForPublicationSubType() {
		Datex2DataTypeApi datex2DataTypeApi = new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", "pv3", "ct3", Collections.emptySet(), "myPublication", Collections.emptySet());
		assertThat(datex2DataTypeApi.getValues().containsKey(MessageProperty.PUBLICATION_SUB_TYPE.getName())).isFalse();
	}


	@Test
	public void denmDataTypeApiIsConvertedToDataTypeAndBack() {
		HashSet<String> quads = Sets.newLinkedHashSet("qt1", "qt2");
		DenmDataTypeApi denmDataTypeApi = new DenmDataTypeApi("NO-393783", "No such publisher",
				"NO", "pv3", "ct4", quads,
				"st5", "cc3", "scc55");
		Set<DataType> converted = capabilityTransformer.dataTypeApiToDataType(Collections.singleton(denmDataTypeApi));
		assertThat(converted).isNotNull().hasSize(1);
		DataType convertedDataType = converted.iterator().next();
		assertThat(convertedDataType.getPropertyValue(MessageProperty.MESSAGE_TYPE)).isEqualTo(DenmDataTypeApi.DENM);
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PUBLISHER_ID)).isEqualTo("NO-393783");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PUBLISHER_NAME)).isEqualTo("No such publisher");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY)).isEqualTo("NO");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION)).isEqualTo("pv3");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.CONTENT_TYPE)).isEqualTo("ct4");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.SERVICE_TYPE)).isEqualTo("st5");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.CAUSE_CODE)).isEqualTo("cc3");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.SUB_CAUSE_CODE)).isEqualTo("scc55");

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(Collections.singleton(convertedDataType));
		assertThat(dataTypeApis).isNotNull().hasSize(1);
		DataTypeApi convertedBack = dataTypeApis.iterator().next();
		assertThat(convertedBack).isInstanceOf(DenmDataTypeApi.class);
		assertThat(convertedBack).isEqualTo(denmDataTypeApi);
	}

	@Test
	public void iviDataTypeApiIsConvertedToDataTypeAndBack() {
		HashSet<String> quads = Sets.newLinkedHashSet("qt1", "qt2");
		IviDataTypeApi iviDataTypeApi = new IviDataTypeApi("NO-38367", "No such publisher",
				"NO", "pv7", "ct6", quads,
				"st8", 12134, Sets.newLinkedHashSet(9876, 7654));
		Set<DataType> converted = capabilityTransformer.dataTypeApiToDataType(Collections.singleton(iviDataTypeApi));
		assertThat(converted).isNotNull().hasSize(1);
		DataType convertedDataType = converted.iterator().next();
		assertThat(convertedDataType.getPropertyValue(MessageProperty.MESSAGE_TYPE)).isEqualTo(IviDataTypeApi.IVI);
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PUBLISHER_ID)).isEqualTo("NO-38367");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PUBLISHER_NAME)).isEqualTo("No such publisher");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY)).isEqualTo("NO");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PROTOCOL_VERSION)).isEqualTo("pv7");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.CONTENT_TYPE)).isEqualTo("ct6");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.SERVICE_TYPE)).isEqualTo("st8");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.IVI_TYPE)).isEqualTo("12134");
		assertThat(convertedDataType.getPropertyValue(MessageProperty.PICTOGRAM_CATEGORY_CODE)).isEqualTo("9876,7654");

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(Collections.singleton(convertedDataType));
		assertThat(dataTypeApis).isNotNull().hasSize(1);
		DataTypeApi convertedBack = dataTypeApis.iterator().next();
		assertThat(convertedBack).isInstanceOf(IviDataTypeApi.class);
		assertThat(convertedBack).isEqualTo(iviDataTypeApi);
	}

	private DataType getDatexHeaders(String originatingCountry, String publicationType, String publicationSubType) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), originatingCountry);
		if (publicationType != null) {
			datexHeaders.put(MessageProperty.PUBLICATION_TYPE.getName(), publicationType);
		}
		if (publicationSubType  != null) {
			datexHeaders.put(MessageProperty.PUBLICATION_SUB_TYPE.getName(), publicationSubType);
		}
		return new DataType(datexHeaders);
	}

	@Test
	public void dataTypeWithoutMessageTypeIsNotConvertedToDataTypeApi() {
		Set<DataType> dataTypesToBeConverted = new HashSet<>();

		HashMap<String, String> dtWithMessageTypeValues = new HashMap<>();
		dtWithMessageTypeValues.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		DataType dataTypeWithMessageType = new DataType(dtWithMessageTypeValues);
		dataTypesToBeConverted.add(dataTypeWithMessageType);

		HashMap<String, String> dtWithoutMessageTypeValues = new HashMap<>();
		dtWithoutMessageTypeValues.put(MessageProperty.PROTOCOL_VERSION.getName(), "9.99");
		dtWithoutMessageTypeValues.put(MessageProperty.PUBLISHER_ID.getName(), "publisherId-77");
		DataType dataTypeWithoutMessageType = new DataType(dtWithoutMessageTypeValues);
		dataTypesToBeConverted.add(dataTypeWithoutMessageType);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypesToDataTypeApis(dataTypesToBeConverted);
		assertThat(dataTypeApis).hasSize(1);
	}
}
