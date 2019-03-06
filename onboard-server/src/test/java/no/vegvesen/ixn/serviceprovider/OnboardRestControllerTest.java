package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.serviceprovider.model.IxnServiceProvider;
import no.vegvesen.ixn.serviceprovider.repository.ServiceProviderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(OnboardRestController.class)
public class OnboardRestControllerTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	ServiceProviderRepository repository;

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
		findus.setId(981279980);
		String jsonPayload = objectMapper.writeValueAsString(findus);
		IxnServiceProvider ixnServiceProvider = objectMapper.readValue(jsonPayload, IxnServiceProvider.class);
		System.out.println(jsonPayload);
		assertThat(ixnServiceProvider).isNotNull();
		assertThat(ixnServiceProvider.getName()).isEqualTo("findus");
		assertThat(ixnServiceProvider.getId()).isEqualTo(981279980);
	}

	@Test
	public void newServiceProviderGetsId() throws Exception {
		IxnServiceProvider findus2 = new IxnServiceProvider("findus");
		String findus1 = objectMapper.writeValueAsString(findus2);

		IxnServiceProvider storedFindus = new IxnServiceProvider("findus");
		storedFindus.setId(new Random(1923412973).nextInt());
		given(this.repository.findByName("findus")).willReturn(storedFindus);

		mvc.perform(post("/createServiceProvider")
				.content(findus1)
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				//.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn();
	}

	@Test
	public void newServiceProviderCapabilitiesCanBeStored() throws Exception {
		IxnServiceProvider provider = new IxnServiceProvider("findus");
		Set<DataType> capabilities = new HashSet<>();
		capabilities.add(new DataType("datex2", "NO", null));
		capabilities.add(new DataType("datex2", "SE", null));
		provider.setCapabilities(capabilities);

		IxnServiceProvider storedFindus = new IxnServiceProvider("findus");
		storedFindus.setId(new Random(1923412973).nextInt());
		given(this.repository.findByName("findus")).willReturn(storedFindus);

		mvc.perform(post("/createServiceProvider")
				.content(objectMapper.writeValueAsString(provider))
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				//.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andReturn();
	}
}