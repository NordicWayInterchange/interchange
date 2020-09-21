package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.transformer.CapabilityTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import no.vegvesen.ixn.properties.MessageProperty;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class NeighbourRESTFacadeTest {


	private RestTemplate restTemplate = new RestTemplate();
	private ObjectMapper mapper = new ObjectMapper();
	private CapabilityTransformer capabilityTransformer = new CapabilityTransformer();
	private SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
	private SubscriptionRequestTransformer subscriptionRequestTransformer = new SubscriptionRequestTransformer(subscriptionTransformer);

	private NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(new NeighbourRESTClient(restTemplate,mapper),
			capabilityTransformer,
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

		DataTypeApi dataType = new Datex2DataTypeApi("NO");
		CapabilityApi capabilityApi = new CapabilityApi("remote server", Collections.singleton(dataType));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilityApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self );

		assertThat(res.getDataTypes()).hasSize(1);

		Iterator<DataType> dataTypes = res.getDataTypes().iterator();
		DataType dataTypeInCapabilities = dataTypes.next();

		assertThat(dataTypeInCapabilities.getPropertyValue(MessageProperty.MESSAGE_TYPE)).isEqualTo(dataType.getMessageType());
		assertThat(dataTypeInCapabilities.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY)).isEqualTo(dataType.getOriginatingCountry());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithDenmCapabilities() throws Exception {
		DenmDataTypeApi dataType = new DenmDataTypeApi("NO-123123", "Norwegian Road Broadcasting", "NO", "P1", "application/base64", Sets.newSet("aaa"), "road", "cc1", "scc2");
		CapabilityApi capabilityApi = new CapabilityApi("remote server", Collections.singleton(dataType));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilityApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);

		assertThat(res.getDataTypes()).hasSize(1);

		Iterator<DataType> dataTypes = res.getDataTypes().iterator();
		DataType remoteServerResponse = dataTypes.next();

		assertThat(remoteServerResponse.getPropertyValue(MessageProperty.MESSAGE_TYPE)).isEqualTo(dataType.getMessageType());
		assertThat(remoteServerResponse.getPropertyValue(MessageProperty.ORIGINATING_COUNTRY)).isEqualTo(dataType.getOriginatingCountry());
		assertThat(remoteServerResponse.getPropertyValue(MessageProperty.SERVICE_TYPE)).isEqualTo(dataType.getServiceType());
	}

	@Test
	public void successfulPostOfCapabilitiesReturnsInterchangeWithIviCapabilities() throws Exception {
		IviDataTypeApi dataType = new IviDataTypeApi("NO-123123", "Norwegian Road Broadcasting", "NO", "P1", "application/base64", Sets.newSet("aaa"), "road", 12321, Sets.newSet(92827));
		CapabilityApi capabilityApi = new CapabilityApi("remote server", Collections.singleton(dataType));

		String remoteServerJson = new ObjectMapper().writeValueAsString(capabilityApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/capabilities"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		Capabilities res = neighbourRESTFacade.postCapabilitiesToCapabilities(ericsson, self);

		assertThat(res.getDataTypes()).hasSize(1);

		Iterator<DataType> dataTypes = res.getDataTypes().iterator();
		DataType remoteServerResponse = dataTypes.next();

		assertThat(remoteServerResponse.getPropertyValue(MessageProperty.MESSAGE_TYPE)).isEqualTo(dataType.getMessageType());
		assertThat(remoteServerResponse.getPropertyValueAsInteger(MessageProperty.IVI_TYPE)).isEqualTo(dataType.getIviType());
	}

	@Test
	public void successfulPostOfSubscriptionRequestReturnsSubscriptionRequest() throws Exception{

		SubscriptionApi subscriptionApi = new SubscriptionApi();
		subscriptionApi.setSelector("originatingCountry = 'NO'");
		subscriptionApi.setStatus(SubscriptionStatus.REQUESTED);
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("remote server", Collections.singleton(subscriptionApi) );

		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionRequestApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/subscriptions"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
				.andExpect(MockRestRequestMatchers.content().contentType(MediaType.APPLICATION_JSON))
				.andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK).body(remoteServerJson).contentType(MediaType.APPLICATION_JSON));

		self.setName("ericsson.itsinterchange.eu");
		Set<Subscription> subscriptionSet = Collections.emptySet();

		SubscriptionRequest response = neighbourRESTFacade.postSubscriptionRequest(ericsson,subscriptionSet, self.getName());

		assertThat(response.getSubscriptions()).hasSize(1);

		Iterator<Subscription> subscriptions = response.getSubscriptions().iterator();
		Subscription subscriptionInSubscriptionRequest = subscriptions.next();

		assertThat(subscriptionInSubscriptionRequest.getSelector()).isEqualTo(subscriptionApi.getSelector());
		assertThat(subscriptionInSubscriptionRequest.getSubscriptionStatus()).isEqualTo(subscriptionApi.getStatus());
	}

	@Test
	public void successfulPollOfSubscriptionReturnsSubscription()throws Exception{

		Subscription subscription = new Subscription("originatingCountry = 'NO'", SubscriptionStatus.REQUESTED);
		subscription.setPath("bouvet/subscription/1");
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionApi);

		server.expect(MockRestRequestMatchers.requestTo("https://ericsson.itsinterchange.eu:8080/bouvet/subscription/1"))
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
	public void clientSubscriptionRequestNotEmptyButServerResponseEmptySubscriptionRequest() throws Exception{

		// Subscription request posted to neighbour has non empty subscription set.
		Subscription subscription = new Subscription();
		subscription.setSelector("originatingCountry = 'NO'");
		Set<Subscription> subscriptionSet = Collections.singleton(subscription);

		// Subscription request received from the neighbour has empty set of subscription
		SubscriptionRequestApi serverResponse = new SubscriptionRequestApi("remote server", new HashSet<>());
		String errorDetailsJson = new ObjectMapper().writeValueAsString(serverResponse);

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
		SubscriptionApi subscriptionApi = subscriptionTransformer.subscriptionToSubscriptionApi(subscription);
		String remoteServerJson = new ObjectMapper().writeValueAsString(subscriptionApi);

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
