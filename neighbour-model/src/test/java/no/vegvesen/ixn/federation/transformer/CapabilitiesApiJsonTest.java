package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<Datex2DataTypeApi> capabilities = new HashSet<>();
		capabilities.add(new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", "pv1", "ct3", Collections.emptySet(), "myPublicationType", Sets.newLinkedHashSet("aa", "bb")));
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("norway", capabilities);

		ObjectMapper objectMapper = new ObjectMapper();
		String capabilityApiJson = objectMapper.writeValueAsString(capabilitiesApi);
		assertThat(capabilityApiJson).contains("myPublicationType");

		CapabilitiesApi fromString = objectMapper.readValue(capabilityApiJson.getBytes(), CapabilitiesApi.class);
		assertThat(fromString.getCapabilities()).hasSize(1);
		DataTypeApi dataTypeFromJson = fromString.getCapabilities().iterator().next();
		assertThat(dataTypeFromJson.getMessageType()).isEqualTo("DATEX2");
		assertThat(dataTypeFromJson).isInstanceOf(Datex2DataTypeApi.class);
		Datex2DataTypeApi datex2DataTypeFromJson = (Datex2DataTypeApi) dataTypeFromJson;
		assertThat(datex2DataTypeFromJson.getPublicationType()).isEqualTo("myPublicationType");
		assertThat(datex2DataTypeFromJson.getPublisherId()).isEqualTo("myPublisherId");
		assertThat(datex2DataTypeFromJson.getPublisherName()).isEqualTo("myPublisherName");
		assertThat(datex2DataTypeFromJson.getProtocolVersion()).isEqualTo("pv1");
		assertThat(datex2DataTypeFromJson.getContentType()).isEqualTo("ct3");
	}
}
