package no.vegvesen.ixn.federation.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<Datex2DataTypeApi> capabilities = new HashSet<>();
		capabilities.add(new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", "pv1", "ct3", Collections.emptySet(), "myPublicationType", Sets.newHashSet("aa", "bb")));
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
		assertThat(datex2DataTypeFromJson.getPublisherId()).isEqualTo("myPublisherId");
		assertThat(datex2DataTypeFromJson.getPublisherName()).isEqualTo("myPublisherName");
		assertThat(datex2DataTypeFromJson.getProtocolVersion()).isEqualTo("pv1");
		assertThat(datex2DataTypeFromJson.getContentType()).isEqualTo("ct3");
	}
}
