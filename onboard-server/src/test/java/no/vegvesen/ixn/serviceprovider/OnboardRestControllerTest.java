package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.DiscoveryStateRepository;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.properties.MessageProperty;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OnboardRestController.class)
public class OnboardRestControllerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockMvc mockMvc;

	@MockBean
	private ServiceProviderRepository serviceProviderRepository;
	@MockBean
	private SelfRepository selfRepository;

	//Unfortunately we have to mock beans not used in the class under test
	//because the spring test wires up all jpa repositories not mocked
	@MockBean
	private DiscoveryStateRepository discoveryStateRepository;
	@MockBean
	private NeighbourRepository neighbourRepository;

	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();

	@Autowired
	private OnboardRestController onboardRestController;

	private String capabilitiesPath = "/capabilities";
	private String subscriptionPath = "/subscription";


	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders
				.standaloneSetup(onboardRestController)
				.setMessageConverters(OnboardStrictWebConfig.strictJsonMessageConverter())
				.setControllerAdvice(OnboardServerErrorAdvice.class)
				.build();
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
	void postingCapabilitiesReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		// Create Capabilities API object for capabilities to add, convert to JSON string and POST to server.
		DataTypeApi a = new Datex2DataTypeApi( "NO");
		DataTypeApi b = new Datex2DataTypeApi("SE");
		Set<DataTypeApi> capabilities = Stream.of(a, b).collect(Collectors.toSet());
		CapabilityApi capabilityApi = new CapabilityApi("First Service Provider", capabilities);
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApi);

		mockMvc.perform(
				post(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void deletingExistingCapabilitiesReturnsStatusOk() throws Exception {
		mockCertificate("Second Service Provider");

		// The existing data types of the positng Service Provider
		DataTypeApi a = new Datex2DataTypeApi("NO");
		DataTypeApi b = new Datex2DataTypeApi("SE");

		// Create Capabilities API object for capabilities to delete, convert to JSON string and POST to server.

		CapabilityApi capabilityApi = new CapabilityApi("Second Service Provider", Collections.singleton(a));
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApi);

		// Mock existing service provider with two capabilities in database
		ServiceProvider secondServiceProvider = new ServiceProvider("Second Service Provider");
		Set<DataType> capabilities = capabilityTransformer.dataTypeApiToDataType(Stream.of(a, b).collect(Collectors.toSet()));
		Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, capabilities);
		secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

		doReturn(secondServiceProvider).when(serviceProviderRepository).findByName(any(String.class));

		mockMvc.perform(
				delete(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void commonNameAndApiNameMismatchReturnsStatusForbiddenInCapabilities() throws Exception {

		mockCertificate("First Service Provider");

		CapabilityApi capabilityApi = new CapabilityApi();
		DataTypeApi a = new Datex2DataTypeApi("FI");
		capabilityApi.setCapabilities(Collections.singleton(a));
		capabilityApi.setName("Second Service Provider");

		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApi);

		mockMvc.perform(
				post(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().isForbidden());
	}


	@Test
	void postingSubscriptionReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("originatingCountry LIKE 'SE'");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("First Service Provider");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		mockMvc.perform(
				post(subscriptionPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());

	}

	@Test
	void postingUnparsableSubscriptionReturnsBadRequest() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("originatingCountry = `SE`");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("First Service Provider");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		mockMvc.perform(
				post(subscriptionPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isBadRequest());

	}

	@Test
	void postingInvalidSubscriptionReturnsStatusBadRequest() throws Exception {
		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("originatingCountry LIKE '%'");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("First Service Provider");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

		String postJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		mockMvc.perform(
				post(subscriptionPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(postJson))
				.andDo(print())
				.andExpect(status().isBadRequest());


	}

	@Test
	void deletingSubscriptionReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		// The existing subscriptions of the Service Provider
		Subscription a = new Subscription("originatingCountry = 'SE'", SubscriptionStatus.REQUESTED);
		Subscription b = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		SubscriptionRequest serviceProviderSubscriptionRequest = new SubscriptionRequest();
		serviceProviderSubscriptionRequest.setSubscriptions(Stream.of(a, b).collect(Collectors.toSet()));
		serviceProviderSubscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		firstServiceProvider.setSubscriptionRequest(serviceProviderSubscriptionRequest);

		doReturn(firstServiceProvider).when(serviceProviderRepository).findByName(any(String.class));

		// Subscription request api posted to the server
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName(firstServiceProvider.getName());
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(a);
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		mockMvc.perform(
				delete(subscriptionPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void commonNameAndApiNameMismatchReturnsStatusForbiddenInSubscription() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Second Service Provider");
		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("originatingCountry = 'NO'");
		subscriptionRequestApi.setSubscriptions(Collections.singleton(subscriptionApi));

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(subscriptionRequestApi);

		mockMvc.perform(
				post(subscriptionPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void calculateSelfCapabilitiesTest() {

		DataType a = getDatex("SE");
		DataType b = getDatex("FI");
		DataType c = getDatex("NO");

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		Capabilities firstServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(a, b).collect(Collectors.toSet()));
		firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(b, c).collect(Collectors.toSet()));
		secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

		Set<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());

		Set<DataType> selfCapabilities = onboardRestController.calculateSelfCapabilities(serviceProviders);

		assertEquals(selfCapabilities.size(), 3);
		assertTrue(selfCapabilities.containsAll(Stream.of(a, b, c).collect(Collectors.toSet())));
	}

	@NotNull
	private DataType getDatex(String se) {
		HashMap<String, String> datexHeaders = new HashMap<>();
		datexHeaders.put(MessageProperty.MESSAGE_TYPE.getName(), Datex2DataTypeApi.DATEX_2);
		datexHeaders.put(MessageProperty.ORIGINATING_COUNTRY.getName(), se);
		return new DataType(datexHeaders);
	}

	@Test
	void calculateSelfSubscriptionsTest() {

		Subscription a = new Subscription("originatingCountry = 'FI'", SubscriptionStatus.REQUESTED);
		Subscription b = new Subscription("originatingCountry = 'SE'", SubscriptionStatus.REQUESTED);
		Subscription c = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		SubscriptionRequest firstServiceProviderSubscriptionRequest = new SubscriptionRequest();
		firstServiceProviderSubscriptionRequest.setSubscriptions(Stream.of(a, b).collect(Collectors.toSet()));
		firstServiceProviderSubscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		firstServiceProvider.setSubscriptionRequest(firstServiceProviderSubscriptionRequest);

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		SubscriptionRequest secondServiceProviderSubscriptionRequest = new SubscriptionRequest();
		secondServiceProviderSubscriptionRequest.setSubscriptions(Stream.of(b, c).collect(Collectors.toSet()));
		secondServiceProviderSubscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
		secondServiceProvider.setSubscriptionRequest(secondServiceProviderSubscriptionRequest);

		Set<ServiceProvider> serviceProviders = Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet());

		Set<Subscription> selfSubscriptions = onboardRestController.calculateSelfSubscriptions(serviceProviders);

		assertEquals(selfSubscriptions.size(), 3);
		assertTrue(selfSubscriptions.containsAll(Stream.of(a, b, c).collect(Collectors.toSet())));
	}

	@Test
	void postUnknownPropertyNameThrowsBadRequestException() throws Exception {
		mockCertificate("best service provider");
		String capabilityApiToServerJson = "{\"version\":\"1.0\",\"name\":\"best service provider\",\"capabilities\":[{\"messageType\":\"DENM\",\"noSuchProperty\":\"pubid\",\"publisherName\":\"pubname\",\"originatingCountry\":\"NO\",\"protocolVersion\":\"1.0\",\"contentType\":\"application/base64\",\"quadTree\":[],\"serviceType\":\"serviceType\",\"causeCode\":\"1\",\"subCauseCode\":\"1\"}]}";
		mockMvc.perform(
				post(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}
}
