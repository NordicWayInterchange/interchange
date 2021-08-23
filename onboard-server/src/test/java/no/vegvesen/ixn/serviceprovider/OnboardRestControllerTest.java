package no.vegvesen.ixn.serviceprovider;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.Datex2DataTypeApi;
import no.vegvesen.ixn.federation.api.v1_0.DatexCapabilityApi;
import no.vegvesen.ixn.federation.auth.CertService;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.onboard.SelfService;
import no.vegvesen.ixn.serviceprovider.model.*;
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

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = OnboardRestController.class)
@ContextConfiguration(classes = {CertService.class, OnboardRestController.class})
public class OnboardRestControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private ServiceProviderRepository serviceProviderRepository;
	@MockBean
	private SelfService selfService;

	@Autowired
	private OnboardRestController onboardRestController;

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

		String firstServiceProvider = "First Service Provider";
		mockCertificate(firstServiceProvider);

		// Create Capabilities API object for capabilities to add, convert to JSON string and POST to server.
		DatexCapabilityApi datexNo = new DatexCapabilityApi("NO");
		String datexNoString = objectMapper.writeValueAsString(datexNo);
		String capabilitiesPath = String.format("/%s/capabilities", firstServiceProvider);

		when(serviceProviderRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(selfService.fetchSelf()).thenReturn(new Self("myName"));

		mockMvc.perform(
				post(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(datexNoString))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void deletingExistingCapabilitiesReturnsStatusOk() throws Exception {
		String serviceProviderName = "Second Service Provider";
		mockCertificate(serviceProviderName);

		// Create Capabilities API object for capabilities to delete, convert to JSON string and POST to server.

		// Mock existing service provider with three capabilities in database
		DatexCapability capability42 = mock(DatexCapability.class);
		when(capability42.getId()).thenReturn(42);
		Set<Capability> capabilities = Sets.newLinkedHashSet(capability42, mock(Capability.class), mock(Capability.class));
		Capabilities secondServiceProviderCapabilities = new Capabilities(Capabilities.CapabilitiesStatus.KNOWN, capabilities);

		ServiceProvider secondServiceProvider = new ServiceProvider(serviceProviderName);
		secondServiceProvider.setCapabilities(secondServiceProviderCapabilities);
		when(selfService.fetchSelf()).thenReturn(new Self("myName"));

		doReturn(secondServiceProvider).when(serviceProviderRepository).findByName(any(String.class));

		Integer dataTypeId = 42;
		mockMvc.perform(
				delete(String.format("/%s/capabilities/%s", serviceProviderName, dataTypeId))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void commonNameAndApiNameMismatchReturnsStatusForbiddenInCapabilities() throws Exception {

		mockCertificate("First Service Provider");

		DatexCapabilityApi datexFi = new DatexCapabilityApi("FI");
		String capabilitiesPath = String.format("/%s/capabilities", "SecondServiceProvider");

		String datexFiString = objectMapper.writeValueAsString(datexFi);

		mockMvc.perform(
				post(capabilitiesPath)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(datexFiString))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void postingSubscriptionReturnsStatusOk() throws Exception {
		String firstServiceProvider = "FirstServiceProvider";
		mockCertificate(firstServiceProvider);

		SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' and originatingCountry = 'SE'");
		SubscriptionsPostRequestApi requestApi = new SubscriptionsPostRequestApi(
				firstServiceProvider,
				Collections.singleton(selectorApi)
		);

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(requestApi);
		//TODO this is dirty!
		when(serviceProviderRepository.save(any())).thenAnswer(i -> {
			Object argument = i.getArguments()[0];
			ServiceProvider s = (ServiceProvider) argument;
			s.setId(1);
			int j = 0;
			for (LocalSubscription subscription : s.getSubscriptions()) {
				subscription.setSub_id(j++);
				subscription.setLastUpdated(LocalDateTime.now());
			}
			return s;
		});
		when(selfService.fetchSelf()).thenReturn(new Self("myName"));

		mockMvc.perform(
				post(String.format("/%s/subscriptions", firstServiceProvider))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isOk());
	}

	@Test
	void postingUnparsableSubscriptionReturnsBadRequest() throws Exception {
		String firstServiceProvider = "FirstServiceProvider";
		mockCertificate(firstServiceProvider);

		DataTypeApi subscriptionApi = new Datex2DataTypeApi("SE");

		String validJson = objectMapper.writeValueAsString(subscriptionApi);
		String invalidJson = validJson.replaceAll("messageType", "someMessyType");

		mockMvc.perform(
				post(String.format("/%s/subscriptions", firstServiceProvider))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalidJson))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void postingInvalidSubscriptionReturnsStatusBadRequest() throws Exception {
		String firstServiceProvider = "FirstServiceProvider";
		mockCertificate(firstServiceProvider);

		DataTypeApi subscriptionApi = new DataTypeApi();
		String emptySubscription = objectMapper.writeValueAsString(subscriptionApi);

		mockMvc.perform(
				post(String.format("/%s/subscriptions", firstServiceProvider))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(emptySubscription))
				.andDo(print())
				.andExpect(status().isBadRequest());
	}

	@Test
	void deletingSubscriptionReturnsStatusRedirectToRefreshTheCurrentSubscriptions() throws Exception {
		String firstServiceProviderName = "FirstServiceProvider";
		mockCertificate(firstServiceProviderName);

		// The existing subscriptions of the Service Provider
		Set<LocalSubscription> serviceProviderSubscriptionRequest = new HashSet<>();
		String se = "originatingCountry = 'SE'";
		LocalSubscription seSubs = new LocalSubscription(1,LocalSubscriptionStatus.CREATED,se);
		String fi = "originatingCountry = 'FI'";
		LocalSubscription fiSubs = new LocalSubscription(2,LocalSubscriptionStatus.CREATED,fi);
		ServiceProvider firstServiceProvider = new ServiceProvider();
		firstServiceProvider.setName(firstServiceProviderName);
		firstServiceProvider.updateSubscriptions(new HashSet<>(Arrays.asList(seSubs,fiSubs)));
		doReturn(firstServiceProvider).when(serviceProviderRepository).findByName(any(String.class));

		//Self
		Self self = new Self("this-server-name");
		self.setLocalSubscriptions(serviceProviderSubscriptionRequest);//same subscriptions as the service provider
		doReturn(self).when(selfService).fetchSelf();
		when(serviceProviderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

		// Subscription request api posted to the server

		String deleteUrl = String.format("/%s/subscriptions/%s", firstServiceProviderName, "1");
		mockMvc.perform(
				delete(deleteUrl)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().is3xxRedirection());
	}

	@Test
	void deletingNonExistingSubscriptionReturnsStatusNotFound() throws Exception {
		String firstServiceProviderName = "FirstServiceProvider";
		mockCertificate(firstServiceProviderName);

		// The existing subscriptions of the Service Provider
		ServiceProvider firstServiceProvider = new ServiceProvider(firstServiceProviderName);
		doReturn(firstServiceProvider).when(serviceProviderRepository).findByName(any(String.class));
		when(selfService.fetchSelf()).thenReturn(new Self("myName"));

		// Subscription request api posted to the server

		String deleteUrl = String.format("/%s/subscriptions/%s", firstServiceProviderName, "3");
		mockMvc.perform(
				delete(deleteUrl)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void deletingSubscriptionOnServiceProviderThatDoesNotExistReturnsStatusNotFound() throws Exception {
		String firstServiceProviderName = "some-non-existing-service-provider";
		mockCertificate(firstServiceProviderName);

		// The existing subscriptions of the Service Provider
		doReturn(null).when(serviceProviderRepository).findByName(any(String.class));
		when(selfService.fetchSelf()).thenReturn(new Self("myName"));

		// Subscription request api posted to the server

		String deleteUrl = String.format("/%s/subscriptions/%s", firstServiceProviderName, "1");
		mockMvc.perform(
				delete(deleteUrl)
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isNotFound());
	}

	@Test
	void commonNameAndApiNameMismatchReturnsStatusForbiddenInSubscription() throws Exception {
		String firstServiceProviderName = "FirstServiceProvider";
		String secondServiceProviderName = "SecondServiceProvider";
		mockCertificate(secondServiceProviderName);

		SelectorApi selectorApi = new SelectorApi("messageType = 'DATEX2' and originatingCountry = 'SE'");
		SubscriptionsPostRequestApi requestApi = new SubscriptionsPostRequestApi(
				firstServiceProviderName,
				Collections.singleton(selectorApi)
		);

		String subscriptionRequestApiToServerJson = objectMapper.writeValueAsString(requestApi);

		mockMvc.perform(
				post(String.format("/%s/subscriptions", firstServiceProviderName))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(subscriptionRequestApiToServerJson))
				.andDo(print())
				.andExpect(status().isForbidden());
	}

	@Test
	void postUnknownPropertyNameThrowsBadRequestException() throws Exception {
		String serviceProviderName = "best service provider";
		mockCertificate(serviceProviderName);
		String capabilityApiToServerJson = "{\"messageType\":\"DENM\",\"noSuchProperty\":\"pubid\",\"publisherName\":\"pubname\",\"originatingCountry\":\"NO\",\"protocolVersion\":\"1.0\",\"contentType\":\"application/base64\",\"quadTree\":[],\"serviceType\":\"serviceType\",\"causeCode\":\"1\",\"subCauseCode\":\"1\"}";
		when(serviceProviderRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
		mockMvc.perform(
				post(String.format("/%s/capabilities", serviceProviderName))
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content(capabilityApiToServerJson))
				.andDo(print())
				.andExpect(status().is4xxClientError());
	}
}
