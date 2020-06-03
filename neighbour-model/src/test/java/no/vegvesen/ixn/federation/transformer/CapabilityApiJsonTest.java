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

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityApiJsonTest {
	@Test
	public void capabilitiesApiDatexTransformedToJsonAndBackToCapabilitiesApi() throws IOException {
		HashSet<Datex2DataTypeApi> capabilities = new HashSet<>();
		capabilities.add(new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", "pv1", "ct3", Collections.emptySet(), "myPublicationType", Sets.newLinkedHashSet("aa", "bb")));
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
