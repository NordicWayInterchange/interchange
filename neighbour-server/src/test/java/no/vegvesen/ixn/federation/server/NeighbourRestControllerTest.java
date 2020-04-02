package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.discoverer.DNSFacade;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ComponentScan(basePackages = "no.vegvesen.ixn.federation")
class NeighbourRestControllerTest {


	private MockMvc mockMvc;

	// Mocks
	@MockBean
	private NeighbourRepository neighbourRepository;
	@MockBean
	private SelfRepository selfRepository;
	@MockBean
	private DNSFacade dnsFacade;

	@MockBean
	ServiceProviderRepository serviceProviderRepository;

	@Autowired
	private NeighbourRestController neighbourRestController;

	private String subscriptionRequestPath = "/subscription";
	private String capabilityExchangePath = "/capabilities";


	private Set<String> quadTree = Collections.emptySet();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(neighbourRestController).setControllerAdvice(NeighbourServiceErrorAdvice.class).build();
		neighbourRestController = new NeighbourRestController(new NeighbourService(neighbourRepository, selfRepository, dnsFacade));
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
	void postingDatexCapabilitiesReturnsStatusCreated() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("NO");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
		verify(dnsFacade,times(1)).findNeighbour(anyString());
	}

	@Test
	void postingDenmCapabilitiesReturnsStatusCreated() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DenmDataTypeApi ericssonDataType = new DenmDataTypeApi();
		ericssonDataType.setCauseCode("cc3");
		ericssonDataType.setSubCauseCode("scc34");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
		verify(dnsFacade,times(1)).findNeighbour(anyString());
	}

	@Test
	void postingIviCapabilitiesReturnsStatusCreated() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		IviDataTypeApi ericssonDataType = new IviDataTypeApi();
		ericssonDataType.setPictogramCategoryCodes(Sets.newLinkedHashSet(3993));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
		verify(dnsFacade,times(1)).findNeighbour(anyString());
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() throws Exception {
		// Mocking the incoming certificate
		mockCertificate("unknownNeighbour");

		// Mock the incoming API object.
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		// Mock response from DNS facade on Server
		doThrow(new InterchangeNotInDNSException("expected")).when(dnsFacade).findNeighbour(anyString());

		// Create capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(unknownNeighbour);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
		verify(dnsFacade,times(1)).findNeighbour("unknownNeighbour");
	}

	@Test
	void postingSubscriptionRequestReturnsStatusAccepted() throws Exception {
		mockCertificate("ericsson");

		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi();
		ericsson.setName("ericsson");
		ericsson.setSubscriptions(Collections.singleton(new SubscriptionApi("originatingCountry = 'FI'", "", SubscriptionStatus.REQUESTED)));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock saving Neighbour to Neighbour repository
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscription/1");
		Set<Subscription> subscriptions = Sets.newLinkedHashSet(firstSubscription);
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, subscriptions);
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		doReturn(updatedNeighbour).when(neighbourRepository).save(any(Neighbour.class));
		doReturn(updatedNeighbour).when(neighbourRepository).findByName(anyString());

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Collections.singletonList(ericssonNeighbour)).when(dnsFacade).getNeighbours();

		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isAccepted());
		verify(neighbourRepository,times(2)).save(any(Neighbour.class)); //saved twice because first save generates id, and second save saves the path derived from the ids
		verify(neighbourRepository,times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).getNeighbours();
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsException() throws Exception {
		mockCertificate("ericsson");

		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi();
		ericsson.setName("ericsson");
		ericsson.setSubscriptions(Collections.singleton(new SubscriptionApi("originatingCountry = 'FI'", "", SubscriptionStatus.REQUESTED)));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock saving Neighbour to Neighbour repository
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		Subscription firstSubscription = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/ericsson/subscription/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		Neighbour updatedNeighbour = new Neighbour("ericsson", capabilities, returnedSubscriptionRequest, null);

		doReturn(updatedNeighbour).when(neighbourRepository).save(any(Neighbour.class));

		// Mock response from DNS facade on Server
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(Collections.singletonList(ericssonNeighbour)).when(dnsFacade).getNeighbours();

		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().is4xxClientError());
		verify(neighbourRepository,times(1)).findByName(anyString());
		verify(dnsFacade, times(0)).getNeighbours();
	}

	@Test
	void postThrowsExceptionIfCommonNameOfCertificateIsNotTheSameAsNameInApiObject() throws Exception {
		mockCertificate("bouvet");

		// Create incoming capability api object.
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		ericsson.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		// Convert to JSON
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}

	@Test
	void postDatexDataTypeCapability() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", null, null, quadTree, "myPublicationType", null);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		// Mock dns lookup
		Neighbour ericssonNeighbour = new Neighbour();
		ericssonNeighbour.setName("ericsson");
		doReturn(ericssonNeighbour).when(dnsFacade).findNeighbour(anyString());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
		verify(dnsFacade,times(1)).findNeighbour(anyString());
		verify(neighbourRepository, times(1)).save(any(Neighbour.class));
	}

	@Test
	void postUnknownMessageTypeThrowsBadRequestException() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new DataTypeApi("unknown", "myPublisherId", "myPublisherName", "NO", null, null, quadTree);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}

}
