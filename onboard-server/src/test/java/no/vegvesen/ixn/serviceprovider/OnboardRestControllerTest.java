package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.SelfRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
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

import java.util.Collections;
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

@RunWith(MockitoJUnitRunner.class)
@WebMvcTest(controllers = OnboardRestController.class)
public class OnboardRestControllerTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private MockMvc mockMvc;

	private ServiceProviderRepository serviceProviderRepository = mock(ServiceProviderRepository.class);
	private SelfRepository selfRepository = mock(SelfRepository.class);

	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();

	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);


	private OnboardRestController onboardRestController = new OnboardRestController(serviceProviderRepository, selfRepository, capabilityTransformer, subscriptionRequestTransformer);

	private String capabilitiesPath = "/capabilities";
	private String subscriptionPath = "/subscription";


	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(onboardRestController).setControllerAdvice(OnboardServerErrorAdvice.class).build();
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
	public void postingCapabilitiesReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		// Create Capabilities API object for capabilities to add, convert to JSON string and POST to server.
		DataType a = new DataType("datex2;1.0", "NO", "Obstruction");
		DataType b = new DataType("datex2;1.0", "SE", "Works");
		Set<DataType> capabilities = Stream.of(a, b).collect(Collectors.toSet());
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
	public void deletingExistingCapabilitiesReturnsStatusOk() throws Exception {
		mockCertificate("Second Service Provider");

		// The existing data types of the positng Service Provider
		DataType a = new DataType("datex2;1.0", "NO", "Obstruction");
		DataType b = new DataType("datex2;1.0", "SE", "Works");

		// Create Capabilities API object for capabilities to delete, convert to JSON string and POST to server.

		CapabilityApi capabilityApi = new CapabilityApi("Second Service Provider", Collections.singleton(a));
		String capabilityApiToServerJson = objectMapper.writeValueAsString(capabilityApi);

		// Mock existing service provider with two capabilities in database
		ServiceProvider secondServiceProvider = new ServiceProvider("Second Service Provider");
		Set<DataType> capabilities = Stream.of(a, b).collect(Collectors.toSet());
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
	public void commonNameAndApiNameMismatchReturnsStatusForbiddenInCapabilities() throws Exception {

		mockCertificate("First Service Provider");

		CapabilityApi capabilityApi = new CapabilityApi();
		DataType a = new DataType("datex2;1.0", "FI", "Conditions");
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
	public void postingSubscriptionReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE 'SE'");

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
	public void postingUnparsableSubscriptionReturnsBadRequest() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE `SE`");

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
	public void postingInvalidSubscriptionReturnsStatusBadRequest() throws Exception {
		mockCertificate("First Service Provider");

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE '%'");

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
	public void deletingSubscriptionReturnsStatusOk() throws Exception {

		mockCertificate("First Service Provider");

		// The existing subscriptions of the Service Provider
		Subscription a = new Subscription("where LIKE 'SE'", Subscription.SubscriptionStatus.REQUESTED);
		Subscription b = new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);

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
	public void commonNameAndApiNameMismatchReturnsStatusForbiddenInSubscription() throws Exception {

		mockCertificate("First Service Provider");

		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi();
		subscriptionRequestApi.setName("Second Service Provider");
		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE 'NO'");
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
	public void calculateSelfCapabilitiesTest() {

		DataType a = new DataType("datex2;1.0", "SE", "Obstruction");
		DataType b = new DataType("datex2;1.0", "FI", "Works");
		DataType c = new DataType("datex2;1.0", "NO", "Obtruction");

		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName("First Service Provider");
		Capabilities firstServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(a, b).collect(Collectors.toSet()));
		firstServiceProvider.setCapabilities(firstServiceProviderCapabilities);

		ServiceProvider secondServiceProvider = new ServiceProvider();
		secondServiceProvider.setName("Second Service Provider");
		Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, Stream.of(b, c).collect(Collectors.toSet()));
		secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);

		doReturn(Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet())).when(serviceProviderRepository).findAll();

		Set<DataType> selfCapabilities = onboardRestController.calculateSelfCapabilities();

		assertEquals(selfCapabilities.size(), 3);
		assertTrue(selfCapabilities.containsAll(Stream.of(a, b, c).collect(Collectors.toSet())));
	}

	@Test
	public void calculateSelfSubscriptionsTest() {

		Subscription a = new Subscription("where LIKE 'FI'", Subscription.SubscriptionStatus.REQUESTED);
		Subscription b = new Subscription("where LIKE 'SE'", Subscription.SubscriptionStatus.REQUESTED);
		Subscription c = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);

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

		doReturn(Stream.of(firstServiceProvider, secondServiceProvider).collect(Collectors.toSet())).when(serviceProviderRepository).findAll();

		Set<Subscription> selfSubscriptions = onboardRestController.calculateSelfSubscriptions();

		assertEquals(selfSubscriptions.size(), 3);
		assertTrue(selfSubscriptions.containsAll(Stream.of(a, b, c).collect(Collectors.toSet())));
	}

}
