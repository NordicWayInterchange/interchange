package no.vegvesen.ixn.federation;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.model.IxnServiceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(OnboardRestController.class)
public class OnboardRestControllerTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void canSerializeAndDeserializeIxnServiceProvider() throws IOException {
		IxnServiceProvider findus = new IxnServiceProvider("findus");
		String jsonPayload = objectMapper.writeValueAsString(findus);
		IxnServiceProvider ixnServiceProvider = objectMapper.readValue(jsonPayload, IxnServiceProvider.class);
		System.out.println(jsonPayload);
		assertThat(ixnServiceProvider).isNotNull();
		assertThat(ixnServiceProvider.getName()).isEqualTo("findus");
		assertThat(ixnServiceProvider.getId()).isNull();
	}

	@Test
	public void canSerializeAndDeserializeIxnServiceProviderWithId() throws IOException {
		IxnServiceProvider findus = new IxnServiceProvider("findus");
		findus.setId("981279980");
		String jsonPayload = objectMapper.writeValueAsString(findus);
		IxnServiceProvider ixnServiceProvider = objectMapper.readValue(jsonPayload, IxnServiceProvider.class);
		System.out.println(jsonPayload);
		assertThat(ixnServiceProvider).isNotNull();
		assertThat(ixnServiceProvider.getName()).isEqualTo("findus");
		assertThat(ixnServiceProvider.getId()).isEqualTo("981279980");
	}

	@Test
	public void newServiceProviderGetsId() throws Exception {
		IxnServiceProvider findus2 = new IxnServiceProvider("findus");
		String findus1 = objectMapper.writeValueAsString(findus2);
		MvcResult findus = mvc.perform(post("/createServiceProvider")
				.content(findus1)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn();
		String contentAsString = findus.getResponse().getContentAsString();
		IxnServiceProvider ixnServiceProvider = objectMapper.readValue(contentAsString, IxnServiceProvider.class);
		assertThat(ixnServiceProvider.getId()).isNotNull();
	}
}