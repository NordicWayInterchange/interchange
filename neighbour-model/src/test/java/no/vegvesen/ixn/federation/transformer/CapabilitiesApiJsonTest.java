package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<CapabilityApi> capabilities = new HashSet<>();
		capabilities.add(new DatexCapabilityApi("myPublisherId", "NO", "pv1", Collections.emptySet(), Sets.newLinkedHashSet("myPublicationType")));
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("norway", capabilities);

		ObjectMapper objectMapper = new ObjectMapper();
		String capabilityApiJson = objectMapper.writeValueAsString(capabilitiesApi);
		assertThat(capabilityApiJson).contains("myPublicationType");

		CapabilitiesApi fromString = objectMapper.readValue(capabilityApiJson.getBytes(), CapabilitiesApi.class);
		assertThat(fromString.getCapabilities()).hasSize(1);
		CapabilityApi dataTypeFromJson = fromString.getCapabilities().iterator().next();
		assertThat(dataTypeFromJson.getMessageType()).isEqualTo("DATEX2");
		assertThat(dataTypeFromJson).isInstanceOf(DatexCapabilityApi.class);
		DatexCapabilityApi datex2DataTypeFromJson = (DatexCapabilityApi) dataTypeFromJson;
		assertThat(datex2DataTypeFromJson.getPublicationType()).containsExactly("myPublicationType");
		assertThat(datex2DataTypeFromJson.getPublisherId()).isEqualTo("myPublisherId");
		assertThat(datex2DataTypeFromJson.getProtocolVersion()).isEqualTo("pv1");
	}
}
