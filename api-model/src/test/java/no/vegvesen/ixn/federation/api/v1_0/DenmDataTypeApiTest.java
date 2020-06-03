package no.vegvesen.ixn.federation.api.v1_0;

/*-
 * #%L
 * api-model
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class DenmDataTypeApiTest {
	ObjectMapper mapper = new ObjectMapper();


	@Test
	public void parseValidJson() throws IOException {
		DenmDataTypeApi denmDataTypeApi = mapper.readValue("{\"messageType\":\"DENM\",\"publisherId\":\"pubid\",\"publisherName\":\"pubname\",\"originatingCountry\":\"NO\",\"protocolVersion\":\"1.0\",\"contentType\":\"application/base64\",\"quadTree\":[],\"serviceType\":\"serviceType\",\"causeCode\":\"1\",\"subCauseCode\":\"1\"}", DenmDataTypeApi.class);
		assertThat(denmDataTypeApi).isNotNull();
	}

	@Test
	public void parseInvalidJson() throws IOException {
		assertThatExceptionOfType(UnrecognizedPropertyException.class).isThrownBy(() -> {
			mapper.readValue("{\"messageType\":\"DENM\",\"noSuchProperty\":\"anyValue\"}", DenmDataTypeApi.class);
		});
	}

	@Test
	public void printValidJson() throws JsonProcessingException {
		Set<String> quads = Collections.emptySet();
		DenmDataTypeApi denmDataTypeApi = new DenmDataTypeApi("pubid", "pubname", "NO", "1.0", "application/base64", quads, "serviceType", "1", "1");
		System.out.println(mapper.writeValueAsString(denmDataTypeApi));
	}
}
