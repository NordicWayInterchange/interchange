package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityTransformerTest {


	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	@Test
	public void capabilityApiIsConvertedToDataTypes(){
		final String messageType = "DATEX2";
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		final String [] publicationSubType = new String[] {"SiteMeasurements", "TravelTimeData"};
		final String publicationSubTypeString = ",SiteMeasurements,TravelTimeData,";
		DataTypeApi capability = new Datex2DataTypeApi(originatingCountry, publicationType, publicationSubType);
		Set<DataTypeApi> capabilities = Collections.singleton(capability);

		Set<DataType> dataTypes = capabilityTransformer.dataTypeApiToDataType(capabilities);

		assertThat(dataTypes).hasSize(1);
		assertThat(dataTypes).containsExactly(new DataType(messageType,originatingCountry, publicationType, publicationSubTypeString));
	}

	@Test
	public void dataTypeIsConvertedToApi(){
		final String messageType = "DATEX2";
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		final String publicationSubType = ",SiteMeasurements,TravelTimeData,";
		DataType dataType = new DataType(messageType, originatingCountry, publicationType, publicationSubType);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypeToDataTypeApi(Collections.singleton(dataType));

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
		DataType dataType = new DataType(Datex2DataTypeApi.DATEX_2, messageType, originatingCountry, null);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypeToDataTypeApi(Collections.singleton(dataType));

		assertThat(dataTypeApis).hasSize(1);
		DataTypeApi dataTypeApi = dataTypeApis.iterator().next();
		assertThat(dataTypeApi).isInstanceOf(DataTypeApi.class);
	}
}
