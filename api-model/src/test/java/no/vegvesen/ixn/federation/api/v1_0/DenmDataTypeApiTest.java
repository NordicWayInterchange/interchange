package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


public class DenmDataTypeApiTest {
	ObjectMapper mapper = new ObjectMapper();


	@Test
	public void parseValidJson() throws IOException {
		DenmDataTypeApi denmDataTypeApi = mapper.readValue("{\"messageType\":\"DENM\",\"publisherId\":\"pubid\",\"publisherName\":\"pubname\",\"originatingCountry\":\"NO\",\"protocolVersion\":\"1.0\",\"contentType\":\"application/base64\",\"quadTree\":[],\"serviceType\":\"serviceType\",\"causeCode\":\"1\",\"subCauseCode\":\"1\"}", DenmDataTypeApi.class);
		assertThat(denmDataTypeApi).isNotNull();
	}

	@Test(expected = UnrecognizedPropertyException.class)
	public void parseInvalidJson() throws IOException {
		mapper.readValue("{\"messageType\":\"DENM\",\"noSuchProperty\":\"anyValue\"}", DenmDataTypeApi.class);
	}


	@Test
	public void printValidJson() throws JsonProcessingException {
		Set<String> quads = Collections.emptySet();
		DenmDataTypeApi denmDataTypeApi = new DenmDataTypeApi("pubid", "pubname", "NO", "1.0", "application/base64", quads, "serviceType", "1", "1");
		System.out.println(mapper.writeValueAsString(denmDataTypeApi));
	}
}