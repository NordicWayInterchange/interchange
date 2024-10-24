package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.*;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NeighbourRESTFacadeTest {


	private final RestTemplate restTemplate = new RestTemplate();
	private final ObjectMapper mapper = new ObjectMapper();
	private final CapabilitiesTransformer capabilitiesTransformer = new CapabilitiesTransformer();
	private final SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private final SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	private final CapabilityToCapabilityApiTransformer capabilityTransformer = new CapabilityToCapabilityApiTransformer();
	private final NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(new NeighbourRESTClient(restTemplate,mapper),
			capabilitiesTransformer,
			capabilityTransformer,
			subscriptionTransformer,
			subscriptionRequestTransformer);

	private Neighbour ericsson;

	private MockRestServiceServer server;

	@BeforeEach
	public void setUp() {
		ericsson = new Neighbour();
		ericsson.setName("ericsson.itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
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

		CapabilityApi capabilityApi = new CapabilityApi();
		ApplicationApi app = new DatexApplicationApi("NO-123123","pub-123", "NO", "P1", List.of("aaa"), "SituationPublication", "publisherName");
		capabilityApi.setApplication(app);
		MetadataApi meta = new MetadataApi();
		capabilityApi.setMetadata(meta);

		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("ericsson.itsinterchange.eu", Collections.singleton(capabilityApi));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Set<NeighbourCapability> res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, "localserver", Collections.emptySet());

		assertThat(res).hasSize(1);

		Iterator<NeighbourCapability> dataTypes = res.iterator();
		NeighbourCapability capability = dataTypes.next();

		assertThat(capability.getApplication()).isInstanceOf(DatexApplication.class);
		assertThat(capability.getApplication().getOriginatingCountry()).isEqualTo(capabilityApi.getApplication().getOriginatingCountry());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithDenmCapabilities() throws Exception {
		CapabilityApi capability = new CapabilityApi();
		DenmApplicationApi app = new DenmApplicationApi("NO-123123","pub-123", "NO", "P1", List.of("aaa"), List.of(6));
		capability.setApplication(app);
		MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
		capability.setMetadata(meta);
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("ericsson.itsinterchange.eu", Collections.singleton(capability));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Set<Capability> localCapabilities = Collections.emptySet();
		Set<NeighbourCapability> res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, "localserver", localCapabilities);

		assertThat(res).hasSize(1);

		Iterator<NeighbourCapability> dataTypes = res.iterator();
		NeighbourCapability capabilityNext = dataTypes.next();

		assertThat(capability.getApplication()).isInstanceOf(DenmApplicationApi.class);
		assertThat(capabilityNext.getApplication().getOriginatingCountry()).isEqualTo(capability.getApplication().getOriginatingCountry());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithIviCapabilities() throws Exception {
		CapabilityApi capability = new CapabilityApi();
		IvimApplicationApi dataType = new IvimApplicationApi("NO-123123", "pub-123", "NO", "P1", List.of("aaa"));
		capability.setApplication(dataType);
		MetadataApi meta = new MetadataApi(RedirectStatusApi.OPTIONAL);
		capability.setMetadata(meta);
		CapabilitiesApi capabilitiesApi = new CapabilitiesApi("remote server", Collections.singleton(capability));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilitiesApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Set<Capability> localCapabilities = Collections.emptySet();
		Set<NeighbourCapability> res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, "localserver", localCapabilities);

		assertThat(res).hasSize(1);

		Iterator<NeighbourCapability> dataTypes = res.iterator();
		NeighbourCapability remoteServerResponse = dataTypes.next();

		assertThat(remoteServerResponse.getApplication()).isInstanceOf(IvimApplication.class);
	}

	@Test
	public void successfulPollOfSubscriptionReturnsSubscription()throws Exception{
		String uuid = UUID.randomUUID().toString();
		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setId(1);
		subscription.setPath("/bouvet/subscriptions/" + uuid);

		SubscriptionPollResponseApi subscriptionPollResponseApi = new SubscriptionPollResponseApi(
				uuid,
				"originatingCountry = 'NO'",
				"/bouvet/subscriptions/" + uuid,
				SubscriptionStatusApi.REQUESTED,
				""
		);

		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionPollResponseApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscriptions/" + uuid))
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
			Set<Capability> localCapabilities = Collections.emptySet();
			neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, "localserver", localCapabilities);
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

		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(
				() -> neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptionSet, "localserver")
		);
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


		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() -> neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptionSet, "localserver"));
	}

	@Test
	public void serverClosesConnectionUnexpectedlyOnSubscriptionRequestPost() throws Exception {
		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		Set<Subscription> subscriptions = Collections.singleton(subscription);

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getStatusCode()).thenThrow(IOException.class);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(SubscriptionRequestException.class).isThrownBy(() -> neighbourRESTFacade.postSubscriptionRequest(ericsson, subscriptions, "localserver"));
	}

	@Test
	public void capabilitiesPostServerUnexpectedlyClosesConnection() throws IOException {

	    final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
	    Mockito.when(mock.getStatusCode()).thenThrow(IOException.class);
		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(CapabilityPostException.class).isThrownBy(() -> {
			Set<Capability> localCapabilities = Collections.emptySet();
			neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, "localserver", localCapabilities);
		});
	}

	@Test
	public void test() throws IOException {

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setPath("/bouvet/subscriptions/1");

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getStatusCode()).thenThrow(IOException.class);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscriptions/1"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond((request) -> mock);

		assertThatExceptionOfType(SubscriptionPollException.class).isThrownBy(
				() -> neighbourRESTFacade.pollSubscriptionStatus(subscription, ericsson)
		);

	}

	@Test
	public void deleteSubscriptionTest() throws JsonProcessingException {

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED, "");
		subscription.setPath("/subscriptions/1");

		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), HttpStatus.NOT_FOUND.toString(), "Error error");
		String errorDetailsJson = new ObjectMapper().writeValueAsString(errorDetails);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions/1"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.DELETE))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.NOT_FOUND).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		assertThatExceptionOfType(SubscriptionNotFoundException.class).isThrownBy(
				() -> neighbourRESTFacade.deleteSubscription(ericsson, subscription)
		);
	}
}
