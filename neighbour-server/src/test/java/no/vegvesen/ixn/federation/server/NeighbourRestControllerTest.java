package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.NeighbourSubscriptionRequest;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NeighbourRestController.class)
@ContextConfiguration(classes = {NeighbourRestController.class, CertService.class, InterchangeNodeProperties.class})
class NeighbourRestControllerTest {

	private MockMvc mockMvc;


	@MockBean
	private ServiceProviderService serviceProviderService;

	@MockBean
	NeighbourService neighbourService;

	@Autowired
	private NeighbourRestController neighbourRestController;

	private String subscriptionRequestPath = "/subscriptions";
	private String capabilityExchangePath = "/capabilities";

	private Set<String> quadTree = Collections.singleton("12004");

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
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");
		CapabilitySplitApi ericssonDataType = new CapabilitySplitApi(
				new DatexApplicationApi(
						"NO-12345",
						"pub-2",
						"NO",
						"DATEX2:2.3",
						quadTree,
						"SituationPublication"
				),
				new MetadataApi()
		);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
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
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");
		CapabilitySplitApi ericssonDataType = new CapabilitySplitApi(
				new DenmApplicationApi(
						"NO-12345",
						"pub-1",
						"NO",
						"DENM:1.2.2",
						quadTree,
						Collections.singleton(3)
				),
				new MetadataApi()
		);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
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
	void postingIvimCapabilitiesReturnsStatusOK() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");

		CapabilitySplitApi capability = new CapabilitySplitApi(new IvimApplicationApi("NO-123", "NO-pub", "NO", "IVIM:1.0", quadTree), new MetadataApi());
		ericsson.setCapabilities(Collections.singleton(capability));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
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
		CapabilitiesSplitApi unknownNeighbour = new CapabilitiesSplitApi();
		unknownNeighbour.setName("unknownNeighbour");
		unknownNeighbour.setCapabilities(Collections.singleton(
				new CapabilitySplitApi(
						new DatexApplicationApi(
								"NO-12345",
								"pub-2",
								"NO",
								"DATEX2:2.3",
								quadTree,
								"SituationPublication"
						),
						new MetadataApi()
				)
		));

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
				Collections.singleton(new RequestedSubscriptionApi(selector, "sp-ericsson")));

		// Convert to JSON
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(ericsson);

		SubscriptionResponseApi response = new SubscriptionResponseApi(name,
				Collections.singleton(new RequestedSubscriptionResponseApi("1",selector,path,SubscriptionStatusApi.REQUESTED, "sp-ericsson")));

		doReturn(response).when(neighbourService).incomingSubscriptionRequest(any());

		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isAccepted());
	}

	@Test
	void postingNullSubscriptionSetRequestReturnsException() throws Exception{
		mockCertificate("ericsson");

		String request = """
				{
				"name": "ericsson",
				"version": "1.1"
				}
				""";
		when(neighbourService.incomingSubscriptionRequest(any())).thenThrow(new SubscriptionRequestException(""));
		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof SubscriptionRequestException));
	}

	@Test
	void postingEmptySubscriptionSetReturnsException()throws Exception{
		String name = "ericsson";
		mockCertificate(name);

		SubscriptionRequestApi request = new SubscriptionRequestApi(name, Set.of());
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(request);

		when(neighbourService.incomingSubscriptionRequest(any())).thenThrow(new SubscriptionRequestException(""));
		mockMvc.perform(post(subscriptionRequestPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof SubscriptionRequestException));
	}

	@Test
	void postingSubscriptionRequestFromUnseenNeighbourReturnsClientError() throws Exception {
		String name = "ericsson";
		String selector = "originatingCountry = 'FI'";

		mockCertificate(name);

		// Create incoming subscription request api object
		SubscriptionRequestApi ericsson = new SubscriptionRequestApi(name,
				Collections.singleton(new RequestedSubscriptionApi(selector, "sp-ericsson"))
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
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");
		ericsson.setCapabilities(Collections.singleton(
				new CapabilitySplitApi(
						new DatexApplicationApi(
								"NO-12345",
								"pub-2",
								"NO",
								"DATEX2:2.3",
								quadTree,
								"SituationPublication"
						),
						new MetadataApi()
				)
		));

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
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");
		CapabilitySplitApi ericssonDataType = new CapabilitySplitApi(
				new DatexApplicationApi(
						"NO-12345",
						"pub-2",
						"NO",
						"DATEX2:2.3",
						quadTree,
						"SituationPublication"
				),
				new MetadataApi()
		);
		ericsson.setCapabilities(Collections.singleton(ericssonDataType));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
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
	void postNullCapabilitiesReturnsException() throws Exception{
		String name = "ericsson";
		mockCertificate(name);

		String request = """
				{
				"name": "ericsson",
				"version": "1.1"
				}
				""";
		when(neighbourService.incomingCapabilities(any(),any())).thenThrow(new CapabilityPostException(""));
		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(request))
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof CapabilityPostException));
	}

	@Test
	void postCapabilityWithMissingPropertiesReturnsException() throws Exception{
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");

		CapabilitySplitApi capability = new CapabilitySplitApi(new IvimApplicationApi("NO-123", "NO-pub", "NO", "IVIM:1.0", Collections.emptySet()), new MetadataApi());
		ericsson.setCapabilities(Collections.singleton(capability));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());
		when(neighbourService.incomingCapabilities(any(),any())).thenThrow(new CapabilityPostException(""));

		mockMvc.perform(
						post(capabilityExchangePath)
								.accept(MediaType.APPLICATION_JSON)
								.contentType(MediaType.APPLICATION_JSON)
								.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof CapabilityPostException));
	}

	@Test
	void postCapabilityWithDuplicatePublicationIdReturnsException() throws Exception{
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");

		CapabilitySplitApi capability1 = new CapabilitySplitApi(new IvimApplicationApi("NO-123", "NO-pub", "NO", "IVIM:1.0", quadTree), new MetadataApi());
		CapabilitySplitApi capability2 = new CapabilitySplitApi(new IvimApplicationApi("NO-1233", "NO-pub", "NO", "IVIM:1.0", quadTree), new MetadataApi());

		ericsson.setCapabilities(Set.of(capability1, capability2));

		// Create JSON string of capability api object to send to the server
		String capabilityApiToServerJson = objectMapper.writeValueAsString(ericsson);

		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());
		when(neighbourService.incomingCapabilities(any(),any())).thenThrow(new CapabilityPostException(""));

		mockMvc.perform(
						post(capabilityExchangePath)
								.accept(MediaType.APPLICATION_JSON)
								.contentType(MediaType.APPLICATION_JSON)
								.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isBadRequest())
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof CapabilityPostException));
	}

	@Test
	void testUnknownPropertiesAreIgnoredWhenPostingCapabilities() throws Exception {
		mockCertificate("ericsson");
		CapabilitiesSplitApi selfCapabilities = new CapabilitiesSplitApi("bouvet", Sets.newLinkedHashSet());
		doReturn(selfCapabilities).when(neighbourService).incomingCapabilities(any(), any());
		String json = """
				    {
				         "version": "2.0",
				         "name": "ericsson",
				         "capabilities": [
				             {
				                 "application": {
				                     "messageType": "DATEX2",
				                     "publisherId": "NO-12345",
				                     "publicationId": "pub-2",
				                     "originatingCountry": "NO",
				                     "protocolVersion": "DATEX2:2.3",
				                     "quadTree": ["1220"],
				                     "publicationType": "SituationPublication"
				                 },
				                 "metadata": {
				                     "shardCount": 1
				                 }
				             }
				         ]
				     }
				""";
		mockMvc.perform(
						post(capabilityExchangePath)
								.accept(MediaType.APPLICATION_JSON)
								.contentType(MediaType.APPLICATION_JSON)
								.content(json))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void postUnknownMessageTypeReturnsClientError() throws Exception {
		mockCertificate("ericsson");

		// Mock incoming capabiity API
		CapabilitiesSplitApi ericsson = new CapabilitiesSplitApi();
		ericsson.setName("ericsson");
		CapabilitySplitApi ericssonDataType = new CapabilitySplitApi(
				new ApplicationApi(
						"unknown",
						"NO-12345",
						"pub-2",
						"NO",
						"DATEX2:2.3",
						quadTree
				),
				new MetadataApi()
		);
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

	@Test
	void listSubscriptionsForNeighbourThatDoesNotExistInDatabaseReturnsNotFound() throws Exception {
		mockCertificate("ericsson");
		doThrow(new InterchangeNotFoundException("")).when(neighbourService).findSubscriptions("ericsson");
		mockMvc.perform(
				get("/ericsson/subscriptions")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect( result -> assertThat(result.getResponse().getStatus()).isEqualTo(404));
	}

	@Test
	void testPollSubscriptionReturnsStatusOk() throws Exception {
		mockCertificate("ericsson");

		String ixnName = "ericsson";
		Integer subscriptionId = 1;
		when(neighbourService.incomingSubscriptionPoll(ixnName, subscriptionId)).thenReturn(new SubscriptionPollResponseApi());
		mockMvc.perform(get("/"+ixnName+subscriptionRequestPath+"/"+subscriptionId)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
				).andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void testPollSubscriptionThrowsExceptionWhenNonExistent() throws Exception {
		mockCertificate("ericsson");
		String ixnName = "ericsson";
		Integer subscriptionId = 1;

		when(neighbourService.incomingSubscriptionPoll(ixnName, subscriptionId)).thenThrow(new InterchangeNotFoundException(""));
		mockMvc.perform(get("/"+ixnName+subscriptionRequestPath+"/"+subscriptionId)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
				).andDo(print())
				.andExpect(status().isNotFound());

	}
	@Test
	void testDeleteSubscriptionReturnsStatusOk() throws Exception{
		mockCertificate("ericsson");
		String ixnName = "ericsson";
		Integer subscriptionId = 1;

		mockMvc.perform(delete("/"+ixnName+subscriptionRequestPath+"/"+subscriptionId))
				.andDo(print())
				.andExpect(status().isOk());
	}
	@Test
	void testDeleteSubscriptionThrowsExceptionWhenSubDoesNotExist()throws Exception{
		mockCertificate("ericsson");
		String ixnName = "ericsson";
		Integer subscriptionId = 1;

		doThrow(new NeighbourSubscriptionNotFound("")).when(neighbourService).incomingSubscriptionDelete(any(), any());
		mockMvc.perform(delete("/"+ixnName+subscriptionRequestPath+"/"+subscriptionId))
				.andDo(print())
				.andExpect(status().isNotFound());

	}

}
