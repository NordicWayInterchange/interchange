package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<CapabilitySplitApi> capabilities = new HashSet<>();
		capabilities.add(new CapabilitySplitApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", Collections.emptySet(), "myPublicationType"), new MetadataApi()));
		CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi("norway", capabilities);

		ObjectMapper objectMapper = new ObjectMapper();
		String capabilityApiJson = objectMapper.writeValueAsString(capabilitiesApi);
		assertThat(capabilityApiJson).contains("myPublicationType");

		CapabilitiesSplitApi fromString = objectMapper.readValue(capabilityApiJson.getBytes(), CapabilitiesSplitApi.class);
		assertThat(fromString.getCapabilities()).hasSize(1);
		CapabilitySplitApi dataTypeFromJson = fromString.getCapabilities().iterator().next();
		assertThat(dataTypeFromJson.getApplication().getMessageType()).isEqualTo("DATEX2");
		assertThat(dataTypeFromJson.getApplication()).isInstanceOf(DatexApplicationApi.class);
		DatexApplicationApi datex2DataTypeFromJson = (DatexApplicationApi) dataTypeFromJson.getApplication();
		assertThat(datex2DataTypeFromJson.getPublicationType()).isEqualTo("myPublicationType");
		assertThat(datex2DataTypeFromJson.getPublisherId()).isEqualTo("myPublisherId");
		assertThat(datex2DataTypeFromJson.getProtocolVersion()).isEqualTo("pv1");
	}
}
