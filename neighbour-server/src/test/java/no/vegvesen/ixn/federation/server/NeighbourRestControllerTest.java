package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(controllers = NeighbourRestController.class)
public class NeighbourRestControllerTest {


	private MockMvc mockMvc;

	// Mocks
	private NeighbourRepository neighbourRepository = mock(NeighbourRepository.class);
	private SelfRepository selfRepository = mock(SelfRepository.class);
	private DNSFacade dnsFacade = mock(DNSFacade.class);

	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	//@Spy
	private NeighbourRestController neighbourRestController = new NeighbourRestController(
			neighbourRepository,
			selfRepository,
			capabilityTransformer,
			subscriptionTransformer,
			subscriptionRequestTransformer,
			dnsFacade );

	private String subscriptionRequestPath = "/subscription";
	private String capabilityExchangePath = "/capabilities";


	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(neighbourRestController).build();
	}

	private void mockCertificate(String commonName) {
		Authentication principal = mock(Authentication.class);
		when(principal.getName()).thenReturn(commonName);
		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(principal);
		SecurityContextHolder.setContext(securityContext);
	}

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void postingCapabilitiesReturnsStatusCreated() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataType ericssonDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		List<Neighbour> dnsReturn = Arrays.asList(ericssonNeighbour);
		doReturn(dnsReturn).when(dnsFacade).getNeighbours();

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
		verify(dnsFacade,times(1)).getNeighbours();
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() throws Exception {
		expectedException.expectCause(isA(InterchangeNotInDNSException.class));

		// Mocking the incoming certificate
		mockCertificate("unknownNeighbour");

		// Mock the incoming API object.
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new DataType("datex2;1.0", "NO", "Obstruction")));

		// Mock response from DNS facade on Server
		doReturn(Collections.emptyList()).when(dnsFacade).getNeighbours();

		// Create capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(unknownNeighbour);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print());
		verify(dnsFacade,times(1)).getNeighbours();
	}

	@Test
	public void postingSubscriptionRequestReturnsStatusAccepted() throws Exception {
		mockCertificate("ericsson");


		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi();
		ericsson.setName("ericsson");
		ericsson.setSubscriptions(Collections.singleton(new SubscriptionApi("where LIKE 'FI'", "", Subscription.SubscriptionStatus.REQUESTED)));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock saving Neighbour to Neighbour repository
		Neighbour updatedNeighbour = new Neighbour();
		updatedNeighbour.setName("ericsson");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		updatedNeighbour.setCapabilities(capabilities);
		Subscription firstSubscription = new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscription/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		updatedNeighbour.setSubscriptionRequest(returnedSubscriptionRequest);
		doReturn(updatedNeighbour).when(neighbourRepository).save(any(Neighbour.class));


		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Collections.singletonList(ericssonNeighbour)).when(dnsFacade).getNeighbours();


		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isAccepted());
		//TODO do we really need to save the Neighbour twice here?
		verify(neighbourRepository,times(2)).save(any(Neighbour.class));
		verify(dnsFacade).getNeighbours();

	}

	@Test
	public void postThrowsExceptionIfCommonNameOfCertificateIsNotTheSameAsNameInApiObject() throws Exception {
		mockCertificate("bouvet");
		expectedException.expectCause(isA(CNAndApiObjectMismatchException.class));

		// Create incoming capability api object.
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		ericsson.setCapabilities(Collections.singleton(new DataType("datex2;1.0", "NO", "Obstruction")));

		// Convert to JSON
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is5xxServerError());
	}
}
