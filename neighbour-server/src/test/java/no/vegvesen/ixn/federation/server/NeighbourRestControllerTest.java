package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.InterchangeNotInDNSException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
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
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("NO");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilityApi selfCapabilities = new CapabilityApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());

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
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DenmDataTypeApi ericssonDataType = new DenmDataTypeApi();
		ericssonDataType.setCauseCode("cc3");
		ericssonDataType.setSubCauseCode("scc34");
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilityApi selfCapabilities = new CapabilityApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());

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
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		IviDataTypeApi ericssonDataType = new IviDataTypeApi();
		ericssonDataType.setPictogramCategoryCodes(Sets.newLinkedHashSet(3993));
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilityApi selfCapabilities = new CapabilityApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());

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
		CapabilityApi unknownNeighbour = new CapabilityApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(new Datex2DataTypeApi("NO")));

		// Mock response from DNS facade on Server
		doThrow(new InterchangeNotInDNSException("expected")).when(neighbourService).incomingCapabilities(any(), any());

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
				Collections.singleton(new SubscriptionExchangeSubscriptionRequestApi(selector)));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		SubscriptionResponseApi response = new SubscriptionResponseApi(name,
				Collections.singleton(new SubscriptionExchangeSubscriptionResponseApi("1",selector,path,SubscriptionStatusApi.REQUESTED)));

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
				Collections.singleton(new SubscriptionExchangeSubscriptionRequestApi(selector))
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
	void postDatexDataTypeCapabilityIsOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilityApi ericsson = new CapabilityApi();
		ericsson.setName("ericsson");
		DataTypeApi ericssonDataType = new Datex2DataTypeApi("myPublisherId", "myPublisherName", "NO", null, null, quadTree, "myPublicationType", null);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilityApi selfCapabilities = new CapabilityApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());

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
