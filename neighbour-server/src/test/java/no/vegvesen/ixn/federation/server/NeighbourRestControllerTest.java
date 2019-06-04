package no.vegvesen.ixn.federation.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.discoverer.DNSFacadeInterface;
import no.vegvesen.ixn.federation.exceptions.CNAndApiObjectMismatchException;
import no.vegvesen.ixn.federation.exceptions.DiscoveryException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.InterchangeRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

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

	@Mock
	InterchangeRepository interchangeRepository;

	@Mock
	ServiceProviderRepository serviceProviderRepository;

	@Mock
	CapabilityTransformer capabilityTransformer;

	@Mock
	SubscriptionRequestTransformer subscriptionRequestTransformer;

	@Mock
	DNSFacadeInterface dnsFacade;

	@InjectMocks
	private NeighbourRestController neighbourRestController;

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
		mockCertificate("bouvet");

		// Mock incoming interchange to be result of conversion from CapabilityApi object to Interchange by server.
		Interchange bouvet = new Interchange();
		bouvet.setName("bouvet");
		DataType bouvetDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		Capabilities bouvetCapabilities = new Capabilities();
		bouvetCapabilities.setDataTypes(Collections.singleton(bouvetDataType));
		bouvet.setCapabilities(bouvetCapabilities);
		doReturn(bouvet).when(capabilityTransformer).capabilityApiToInterchange(any(CapabilityApi.class));
		doReturn(Collections.singletonList(bouvet)).when(dnsFacade).getNeighbours();

		// Create capability api object to send to the server
		CapabilityApi capabilityApiToServer = new CapabilityApi(bouvet.getName(), bouvet.getCapabilities().getDataTypes());
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApiToServer);


		// Mock CapabilityApi object returned from the server
		CapabilityApi capabilityApi = new CapabilityApi("bouvet", Collections.singleton(bouvetDataType));
		doReturn(capabilityApi).when(capabilityTransformer).interchangeToCapabilityApi(any(Interchange.class));


		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	public void postingCapabilitiesUnknownInDNSReturnsError() throws Exception {
		expectedException.expectCause(isA(DiscoveryException.class));

		// Mocking the incoming certificate
		mockCertificate("unknowninterchange");

		// Mock incoming interchange to be result of conversion from CapabilityApi object to Interchange by server.
		Interchange unknowninterchange = new Interchange();
		unknowninterchange.setName("unknowninterchange");
		DataType bouvetDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		Capabilities unknownCapabilities = new Capabilities();
		unknownCapabilities.setDataTypes(Collections.singleton(bouvetDataType));
		unknowninterchange.setCapabilities(unknownCapabilities);
		doReturn(unknowninterchange).when(capabilityTransformer).capabilityApiToInterchange(any(CapabilityApi.class));
		doReturn(Collections.emptyList()).when(dnsFacade).getNeighbours();

		// Create capability api object to send to the server
		CapabilityApi capabilityApiToServer = new CapabilityApi(unknowninterchange.getName(), unknowninterchange.getCapabilities().getDataTypes());
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApiToServer);

		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}

	@Test
	public void postingSubscriptionRequestReturnsStatusAccepted() throws Exception {
		mockCertificate("bouvet");

		// Mock interchange object to be result of converting SubscriptionRequestApi object to Interchange by server.
		Interchange bouvet = new Interchange();
		bouvet.setName("bouvet");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		bouvet.setSubscriptionRequest(subscriptionRequest);
		doReturn(bouvet).when(subscriptionRequestTransformer).subscriptionRequestApiToInterchange(any(SubscriptionRequestApi.class));

		// Mock subscription api object sent to server.
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi(bouvet.getName(), bouvet.getSubscriptionRequest().getSubscriptions());
		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		// Mock saving interchange to interchange repository
		Interchange updatedInterchange = new Interchange();
		updatedInterchange.setName("bouvet");
		Capabilities capabilities = new Capabilities(Capabilities.CapabilitiesStatus.UNKNOWN, Collections.emptySet());
		updatedInterchange.setCapabilities(capabilities);
		Subscription firstSubscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		firstSubscription.setPath("/bouvet/subscription/1");
		SubscriptionRequest returnedSubscriptionRequest = new SubscriptionRequest(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED, Collections.singleton(firstSubscription));
		updatedInterchange.setSubscriptionRequest(returnedSubscriptionRequest);
		doReturn(updatedInterchange).when(interchangeRepository).save(any(Interchange.class));


		mockMvc.perform(post(subscriptionRequestPath)
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)
				.content(subscriptionRequestApiToServerJson))
				.andExpect(status().isAccepted());

	}

	@Test
	public void postThrowsExceptionIfCommonNameOfCertificateIsNotTheSameAsNameInApiObject() throws Exception {
		mockCertificate("bouvet");
		expectedException.expectCause(isA(CNAndApiObjectMismatchException.class));

		// Mock incoming interchange to be result of conversion from CapabilityApi object to Interchange by server.
		Interchange bouvet = new Interchange();
		bouvet.setName("ericsson");
		DataType bouvetDataType = new DataType("datex2;1.0", "NO", "Obstruction");
		Capabilities bouvetCapabilities = new Capabilities();
		bouvetCapabilities.setDataTypes(Collections.singleton(bouvetDataType));
		bouvet.setCapabilities(bouvetCapabilities);

		// Create capability api object to send to the server
		CapabilityApi capabilityApiToServer = new CapabilityApi(bouvet.getName(), bouvet.getCapabilities().getDataTypes());
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApiToServer);


		mockMvc.perform(
				post(capabilityExchangePath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}
}
