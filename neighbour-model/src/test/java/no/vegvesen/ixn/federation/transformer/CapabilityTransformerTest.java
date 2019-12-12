package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Neighbour;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityTransformerTest {


	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	@Test
	public void capabilityApiIsConvertedToInterchange(){
		CapabilityApi capabilityApi = new CapabilityApi();
		capabilityApi.setName("Test 2");
		final String messageType = "DATEX2";
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		DataTypeApi capabilities = new Datex2DataTypeApi(originatingCountry, publicationType);
		capabilityApi.setCapabilities(Collections.singleton(capabilities));

		Neighbour interchange = capabilityTransformer.capabilityApiToNeighbour(capabilityApi);

		assertThat(interchange.getCapabilities().getDataTypes()).hasSize(1);
		assertThat(interchange.getCapabilities().getDataTypes()).containsExactly(new DataType(messageType,originatingCountry, publicationType));
	}

	@Test
	public void dataTypeIsConvertedToApi(){
		final String messageType = "DATEX2";
		final String originatingCountry = "NO";
		final String publicationType = "MeasuredDataPublication";
		DataType dataType = new DataType(messageType, originatingCountry, publicationType);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypeToDataTypeApi(Collections.singleton(dataType));

		assertThat(dataTypeApis).hasSize(1);
		DataTypeApi dataTypeApi = dataTypeApis.iterator().next();
		assertThat(dataTypeApi).isInstanceOf(Datex2DataTypeApi.class);
		assertThat(((Datex2DataTypeApi)dataTypeApi).getPublicationType()).isEqualTo(publicationType);
	}

	@Test
	public void unknownDataTypeIsConvertedToApi() {
		final String messageType = "myfantasticmessagetype";
		final String originatingCountry = "ON";
		DataType dataType = new DataType(messageType, originatingCountry);

		Set<DataTypeApi> dataTypeApis = capabilityTransformer.dataTypeToDataTypeApi(Collections.singleton(dataType));

		assertThat(dataTypeApis).hasSize(1);
		DataTypeApi dataTypeApi = dataTypeApis.iterator().next();
		assertThat(dataTypeApi).isInstanceOf(DataTypeApi.class);
	}
}
