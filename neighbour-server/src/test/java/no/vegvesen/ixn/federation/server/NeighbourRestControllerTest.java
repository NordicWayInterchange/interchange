package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.service.CapabilitiesService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.onboard.SelfService;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NeighbourRestController.class)
@ContextConfiguration(classes = {NeighbourRestController.class, CertService.class})
class NeighbourRestControllerTest {

	private MockMvc mockMvc;

	@MockBean
	NeighbourService neighbourService;

	@MockBean
	CapabilitiesService capabilitiesService;

	@MockBean
	SelfService selfService;

	@Autowired
	private NeighbourRestController neighbourRestController;

	private String subscriptionRequestPath = "/subscriptions";
	private String capabilityExchangePath = "/capabilities";

	private Set<String> quadTree = Collections.emptySet();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(neighbourRestController).setControllerAdvice(NeighbourServiceErrorAdvice.class).build();
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
	void postingDatexCapabilitiesReturnsStatusOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new DatexCapabilityApi("NO");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesApi selfCapabilities = new CapabilitiesApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(capabilitiesService).incomingCapabilities(any(), any());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void postingDenmCapabilitiesReturnsStatusOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		DenmCapabilityApi ericssonDataType = new DenmCapabilityApi();
		ericssonDataType.setCauseCode(Sets.newLinkedHashSet("cc3"));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesApi selfCapabilities = new CapabilitiesApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(capabilitiesService).incomingCapabilities(any(), any());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void postingIviCapabilitiesReturnsStatusOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		IvimCapabilityApi ericssonDataType = new IvimCapabilityApi();
		ericssonDataType.setIviType(Sets.newLinkedHashSet("3993"));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesApi selfCapabilities = new CapabilitiesApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(capabilitiesService).incomingCapabilities(any(), any());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsClientError() throws Exception {
		// Mocking the incoming certificate
		mockCertificate("unknownNeighbour");

		// Mock the incoming API object.
		CapabilitiesApi unknownNeighbour = new CapabilitiesApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new DatexCapabilityApi("NO")));

		// Mock response from DNS facade on Server
		doThrow(new InterchangeNotInDNSException("expected")).when(capabilitiesService).incomingCapabilities(any(), any());

		// Create capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(unknownNeighbour);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}

	@Test
	void postingSubscriptionRequestReturnsStatusAccepted() throws Exception {
		String name = "ericsson";
		String selector = "originatingCountry = 'FI'";
		String path = "/subscriptions/ericsson/1";

		mockCertificate(name);

		// Create incoming subscription request api objcet
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi(name,
				Collections.singleton(new RequestedSubscriptionApi(selector, true, "ericsson")));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		SubscriptionResponseApi response = new SubscriptionResponseApi(name,
				Collections.singleton(new RequestedSubscriptionResponseApi("1",selector,path,SubscriptionStatusApi.REQUESTED, true, "ericsson")));

		doReturn(response).when(neighbourService).incomingSubscriptionRequest(any());

		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isAccepted());
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsClientError() throws Exception {
		String name = "ericsson";
		String selector = "originatingCountry = 'FI'";

		mockCertificate(name);

		// Create incoming subscription request api object
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi(name,
				Collections.singleton(new RequestedSubscriptionApi(selector, true, "ericsson"))
		);

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);
		doThrow(SubscriptionRequestException.class).when(neighbourService).incomingSubscriptionRequest(any());

		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void postCommonNameOfCertificateIsNotTheSameAsNameInApiObjectReturnsClientError() throws Exception {
		mockCertificate("bouvet");

		// Create incoming capability api object.
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		ericsson.setCapabilities(Collections.singleton(new DatexCapabilityApi("NO")));

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
	void postDatexDataTypeCapabilityIsOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new DatexCapabilityApi("myPublisherId", "NO", null, quadTree, Sets.newLinkedHashSet("myPublicationType"));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesApi selfCapabilities = new CapabilitiesApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(capabilitiesService).incomingCapabilities(any(), any());

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void postUnknownMessageTypeReturnsClientError() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesApi ericsson = new CapabilitiesApi();
		ericsson.setName("ericsson");
		CapabilityApi ericssonDataType = new CapabilityApi("unknown", "myPublisherId", "NO", null, quadTree);
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
