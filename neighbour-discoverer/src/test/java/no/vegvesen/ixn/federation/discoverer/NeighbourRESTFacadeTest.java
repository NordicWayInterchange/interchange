package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

public class NeighbourRESTFacadeTest {


	private RestTemplate restTemplate = new RestTemplate();
	private ObjectMapper mapper = new ObjectMapper();
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	private NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(
			restTemplate,
			capabilityTransformer,
			subscriptionTransformer,
			subscriptionRequestTransformer,
			mapper);

	private Neighbour ericsson;
	private Self self;

	private MockRestServiceServer server;

	@Before
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
	public void successfulPostOfCapabilitiesReturnsInterchange()throws Exception{

		DataType dataType = new DataType();
		dataType.setHow("datex2.0;1");
		dataType.setWhere("NO");
		CapabilityApi capabilityApi = new CapabilityApi("remote server", Collections.singleton(dataType));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilityApi);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Neighbour response = neighbourRESTFacade.postCapabilities(self, ericsson);

		assertThat(response.getName()).isEqualTo(capabilityApi.getName());
		assertThat(response.getCapabilities().getDataTypes()).hasSize(1);

		Iterator<DataType> dataTypes = response.getCapabilities().getDataTypes().iterator();
		DataType dataTypeInCapabilities = dataTypes.next();

		assertThat(dataTypeInCapabilities.getHow()).isEqualTo(dataType.getHow());
		assertThat(dataTypeInCapabilities.getWhere()).isEqualTo(dataType.getWhere());
	}

	@Test
	public void successfulPostOfSubscriptionRequestReturnsSubscriptionRequest() throws Exception{

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("where LIKE 'NO'");
		subscriptionApi.setStatus(Subscription.SubscriptionStatus.REQUESTED);
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("remote server", Collections.singleton(subscriptionApi) );

		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionRequestApi);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/subscription"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Self self = new Self("myName");
		SubscriptionRequest response = neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);

		assertThat(response.getSubscriptions()).hasSize(1);

		Iterator<Subscription> subscriptions = response.getSubscriptions().iterator();
		Subscription subscriptionInSubscriptionRequest = subscriptions.next();

		assertThat(subscriptionInSubscriptionRequest.getSelector()).isEqualTo(subscriptionApi.getSelector());
		assertThat(subscriptionInSubscriptionRequest.getSubscriptionStatus()).isEqualTo(subscriptionApi.getStatus());
	}

	@Test
	public void successfulPollOfSubscriptionReturnsSubscription()throws Exception{

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscription.setPath("bouvet/subscription/1");
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionApi);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscription/1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond(withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Subscription response = neighbourRESTFacade.pollSubscriptionStatus(subscription, ericsson);

		assertThat(response.getSelector()).isEqualTo(subscription.getSelector());
		assertThat(response.getPath()).isEqualTo(subscription.getPath());
		assertThat(response.getSubscriptionStatus()).isEqualTo(subscription.getSubscriptionStatus());
	}

	@Test(expected = CapabilityPostException.class)
	public void unsuccessfulPostOfCapabilitiesThrowsCapabilityPostException() throws Exception {

		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Error error");
		String errorDetailsJson = new ObjectMapper().writeValueAsString(errorDetails);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		neighbourRESTFacade.postCapabilities(self, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void unsuccessfulPostOfSubscriptionRequestThrowsSubscriptionRequestException() throws Exception{

		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(), HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Error error");
		String errorDetailsJson = new ObjectMapper().writeValueAsString(errorDetails);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/subscription"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void clientSubscriptionRequestNotEmptyButServerResponseEmptySubscriptionRequest() throws Exception{

		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		ericsson.setSubscriptionRequest(subscriptionRequest);

		// Subscription request received from the neighbour has empty set of subscription
		SubscriptionRequestApi serverResponse = new SubscriptionRequestApi("remote server", new HashSet<>());
		String errorDetailsJson = new ObjectMapper().writeValueAsString(serverResponse);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/subscription"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(withStatus(HttpStatus.OK).body(errorDetailsJson).contentType(MediaType.APPLICATION_JSON));

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void serverClosesConnectionUnexpectedlyOnSubscriptionRequestPost() throws Exception {
		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		ericsson.setSubscriptionRequest(subscriptionRequest);

		// Subscription request received from the neighbour has empty set of subscription
		SubscriptionRequestApi serverResponse = new SubscriptionRequestApi("remote server", new HashSet<>());
		String errorDetailsJson = new ObjectMapper().writeValueAsString(serverResponse);

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/subscription"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);



		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = CapabilityPostException.class)
	public void capabilitiesPostServerUnexpectedlyClosesConnection() throws IOException {

	    final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
	    Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);
		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(method(HttpMethod.POST))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andRespond((request) -> mock);

		neighbourRESTFacade.postCapabilities(self, ericsson);
	}

	@Test(expected = SubscriptionPollException.class)
	public void test() throws IOException {

		Subscription subscription = new Subscription("where LIKE 'NO'", Subscription.SubscriptionStatus.REQUESTED);
		subscription.setPath("bouvet/subscription/1");
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionApi);

		final ClientHttpResponse mock = Mockito.mock(ClientHttpResponse.class);
		Mockito.when(mock.getRawStatusCode()).thenThrow(IOException.class);

		server.expect(requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscription/1"))
				.andExpect(method(HttpMethod.GET))
				.andRespond((request) -> mock);

		Subscription response = neighbourRESTFacade.pollSubscriptionStatus(subscription, ericsson);

	}
}
