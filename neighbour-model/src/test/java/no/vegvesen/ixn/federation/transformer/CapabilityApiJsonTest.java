package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityApiJsonTest {
	@Test
	public void capabilitiesDatexTransformedToJsonAndBack() throws IOException {
		HashSet<Datex2DataTypeApi> capabilities = new HashSet<>();
		capabilities.add(new Datex2DataTypeApi("NO", "myPublicationType", new String[] {"aa", "bb"}));
		CapabilityApi capabilityApi = new CapabilityApi("norway", capabilities);

		ObjectMapper objectMapper = new ObjectMapper();
		String capabilityApiJson = objectMapper.writeValueAsString(capabilityApi);
		assertThat(capabilityApiJson).contains("myPublicationType");

		CapabilityApi fromString = objectMapper.readValue(capabilityApiJson.getBytes(), CapabilityApi.class);
		assertThat(fromString.getCapabilities()).hasSize(1);
		DataTypeApi dataTypeFromJson = fromString.getCapabilities().iterator().next();
		assertThat(dataTypeFromJson.getMessageType()).isEqualTo("DATEX2");
		assertThat(dataTypeFromJson).isInstanceOf(Datex2DataTypeApi.class);
		Datex2DataTypeApi datex2DataTypeFromJson = (Datex2DataTypeApi) dataTypeFromJson;
		assertThat(datex2DataTypeFromJson.getPublicationType()).isEqualTo("myPublicationType");
	}
}
