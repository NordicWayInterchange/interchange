package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<CapabilityApi> capabilities = new HashSet<>();
		capabilities.add(new CapabilityApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", List.of(), "myPublicationType", "publisherName"), new MetadataApi()));
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("norway", capabilities);

		ObjectMapper objectMapper = new ObjectMapper();
		String capabilityApiJson = objectMapper.writeValueAsString(capabilitiesApi);
		assertThat(capabilityApiJson).contains("myPublicationType");

		CapabilitiesApi fromString = objectMapper.readValue(capabilityApiJson.getBytes(), CapabilitiesApi.class);
		assertThat(fromString.getCapabilities()).hasSize(1);
		CapabilityApi dataTypeFromJson = fromString.getCapabilities().iterator().next();
		assertThat(dataTypeFromJson.getApplication().getMessageType()).isEqualTo("DATEX2");
		assertThat(dataTypeFromJson.getApplication()).isInstanceOf(DatexApplicationApi.class);
		DatexApplicationApi datex2DataTypeFromJson = (DatexApplicationApi) dataTypeFromJson.getApplication();
		assertThat(datex2DataTypeFromJson.getPublicationType()).isEqualTo("myPublicationType");
		assertThat(datex2DataTypeFromJson.getPublisherId()).isEqualTo("myPublisherId");
		assertThat(datex2DataTypeFromJson.getProtocolVersion()).isEqualTo("pv1");
	}
}
