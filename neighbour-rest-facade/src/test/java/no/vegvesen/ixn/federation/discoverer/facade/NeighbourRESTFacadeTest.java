package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NeighbourRESTFacadeTest {


	private RestTemplate restTemplate = new RestTemplate();
	private ObjectMapper mapper = new ObjectMapper();
	private CapabilitiesTransformer capabilitiesTransformer = new CapabilitiesTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	private NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(new NeighbourRESTClient(restTemplate,mapper),
			capabilitiesTransformer,
			subscriptionTransformer,
			subscriptionRequestTransformer);

	private Neighbour ericsson;
	private Self self;

	private MockRestServiceServer server;

	@BeforeEach
	public void setUp() {
		ericsson = new Neighbour();
		ericsson.setName("ericsson.itsinterchange.eu");
		ericsson.setControlChannelPort("8080");

		self = new Self();

		this.server = MockRestServiceServer.createServer(restTemplate);
	}


	@Test
	public void expectedUrlIsCreated() {
		String expectedURL = "https://ericsson.itsinterchange.eu:8080/";
		String actualURL = ericsson.getControlChannelUrl("/");
		assertThat(expectedURL).isEqualTo(actualURL);
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithDatexCapabilities()throws Exception{

		CapabilityApi capabilityApi = new DatexCapabilityApi("NO");
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("remote server", Collections.singleton(capabilityApi));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self );

		assertThat(res.getCapabilities()).hasSize(1);

		Iterator<Capability> dataTypes = res.getCapabilities().iterator();
		Capability capability = dataTypes.next();

		assertThat(capability).isInstanceOf(DatexCapability.class);
		assertThat(capability.getOriginatingCountry()).isEqualTo(capabilityApi.getOriginatingCountry());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithDenmCapabilities() throws Exception {
		DenmCapabilityApi capbility = new DenmCapabilityApi("NO-123123", "NO", "P1", Sets.newSet("aaa"), Sets.newSet("road"));
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("remote server", Collections.singleton(capbility));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);

		assertThat(res.getCapabilities()).hasSize(1);

		Iterator<Capability> dataTypes = res.getCapabilities().iterator();
		Capability capability = dataTypes.next();

		assertThat(capability).isInstanceOf(DenmCapability.class);
		assertThat(capability.getOriginatingCountry()).isEqualTo(capbility.getOriginatingCountry());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithIviCapabilities() throws Exception {
		IviCapabilityApi dataType = new IviCapabilityApi("NO-123123", "NO", "P1", Sets.newSet("aaa"), Sets.newSet("12321"));
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("remote server", Collections.singleton(dataType));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);

		assertThat(res.getCapabilities()).hasSize(1);

		Iterator<Capability> dataTypes = res.getCapabilities().iterator();
		Capability remoteServerResponse = dataTypes.next();

		assertThat(remoteServerResponse).isInstanceOf(IviCapability.class);
	}

	@Test
	public void successfulPollOfSubscriptionReturnsSubscription()throws Exception{

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setId(1);
		subscription.setPath("bouvet/subscriptions/1");
		SubscriptionPollResponseApi responseApi = subscriptionRequestTransformer.subscriptionToSubscriptionPollResponseApi(subscription, "ericsson.itsinterchange.eu", "bouvet");
		String remoteServerJson = new ObjectMapper().writeValueAsString(responseApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscriptions/1"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Subscription response = neighbourRESTFacade.pollSubscriptionStatus(subscription, ericsson);

		assertThat(response.getSelector()).isEqualTo(subscription.getSelector());
		assertThat(response.getPath()).isEqualTo(subscription.getPath());
		assertThat(response.getSubscriptionStatus()).isEqualTo(subscription.getSubscriptionStatus());
	}

	@Test
	public void unsuccessfulPostOfCapabilitiesThrowsCapabilityPostException() throws Exception {

		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Error error");
		String errorDetailsJson = new ObjectMapper().writeValueAsString(errorDetails);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(() -> {
			neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);
		});
	}

	@Test
	public void unsuccessfulPostOfSubscriptionRequestThrowsSubscriptionRequestException() throws Exception{

		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Error error");
		String errorDetailsJson = new ObjectMapper().writeValueAsString(errorDetails);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		Set<Subscription> subscriptionSet = Collections.emptySet();

		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() -> {
			neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptionSet, self.getName());
		});
	}

	@Test
	public void clientSubscriptionRequestNotEmptyButServerResponseEmptySubscriptionRequest() {

		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		Set<Subscription> subscriptionSet = Collections.singleton(subscription);

		// Subscription request received from the neighbour has empty set of subscription
		String errorDetailsJson = "this is an error";

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));


		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() -> {
			neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptionSet, self.getName());
		});
	}

	@Test
	public void serverClosesConnectionUnexpectedlyOnSubscriptionRequestPost() throws Exception {
		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		Set<Subscription> subscriptions = Collections.singleton(subscription);

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() -> {
			neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptions, self.getName());
		});
	}

	@Test
	public void capabilitiesPostServerUnexpectedlyClosesConnection() throws IOException {

	    final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
	    Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);
		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(() -> {
			neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);
		});
	}

	@Test
	public void test() throws IOException {

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setPath("bouvet/subscriptions/1");

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscriptions/1"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(SubscriptionPollException.class).isThrownBy(() -> {
			Subscription response = neighbourRESTFacade.pollSubscriptionStatus(subscription, ericsson);
		});

	}
}
