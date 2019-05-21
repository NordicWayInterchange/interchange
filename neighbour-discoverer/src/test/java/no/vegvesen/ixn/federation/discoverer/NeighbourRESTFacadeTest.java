package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityTransformer;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionTransformer;
import no.vegvesen.ixn.federation.model.DataType;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.exceptions.*;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NeighbourRESTFacadeTest {

	// Mocks
	private RestTemplate restTemplate = mock(RestTemplate.class);
	private CapabilityTransformer capabilityTransformer = mock(CapabilityTransformer.class);
	private SubscriptionTransformer subscriptionTransformer = mock(SubscriptionTransformer.class);

	private String subscriptionRequestPath = "/requestSubscription";
	private String capabilityExchangePath = "/updateCapabilities";

	@Spy
	private NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(subscriptionRequestPath,
			capabilityExchangePath,
			restTemplate,
			capabilityTransformer,
			subscriptionTransformer);

	private Interchange ericsson;

	@Before
	public void before() {
		ericsson = new Interchange();
		ericsson.setName("ericsson");
		ericsson.setDomainName(".itsinterchange.eu");
		ericsson.setControlChannelPort("8080");
	}


	@Test
	public void expectedUrlIsCreated() {
		String expectedURL = "https://ericsson.itsinterchange.eu:8080";
		String actualURL = neighbourRESTFacade.getUrl(ericsson);

		Assert.assertEquals(expectedURL, actualURL);
	}

	@Test(expected = CapabilityPostException.class)
	public void nullCapabilitiesPostResponseThrowsCapabilityPostException() {

		CapabilityApi capabilityApi = null;

		ResponseEntity<CapabilityApi> response = new ResponseEntity<>(capabilityApi, HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(CapabilityApi.class))).thenReturn(response);

		neighbourRESTFacade.postCapabilities(ericsson, ericsson);

	}

	@Test(expected = CapabilityPostException.class)
	public void unsuccessfulPostOfCapabilitiesThrowsCapabilityPostException() {

		DataType dataType = new DataType("datex2;1.0", "NO", "conditions");
		CapabilityApi capabilityApi = new CapabilityApi("ericsson", Collections.singleton(dataType));

		ResponseEntity<CapabilityApi> response = new ResponseEntity<>(capabilityApi, HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(CapabilityApi.class))).thenReturn(response);

		neighbourRESTFacade.postCapabilities(ericsson, ericsson);
	}


	@Test(expected = SubscriptionRequestException.class)
	public void nullSubscriptionRequestResponseThrowsSubscriptionRequestException() {

		SubscriptionRequestApi subscriptionRequestApi = null;

		ResponseEntity<SubscriptionRequestApi> response = new ResponseEntity<>(subscriptionRequestApi, HttpStatus.ACCEPTED);
		when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(SubscriptionRequestApi.class))).thenReturn(response);

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void clientSubscriptionRequestNotEmptyButServerResponseEmptySubscriptionRequest() {

		// Posting interchange with subscription request not empty
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		ericsson.setSubscriptionRequest(subscriptionRequest);

		// Receive empty subscription request from server
		SubscriptionRequestApi serverResponse = new SubscriptionRequestApi("ericsson", Collections.emptySet());
		ResponseEntity<SubscriptionRequestApi> response = new ResponseEntity<>(serverResponse, HttpStatus.ACCEPTED);
		doReturn(response).when(restTemplate).exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(SubscriptionRequestApi.class));

		Interchange returnInterchange = new Interchange();
		returnInterchange.setName("ericsson");
		doReturn(returnInterchange).when(subscriptionTransformer).subscriptionRequestApiToInterchange(any(SubscriptionRequestApi.class));

		// Should throw a SubscriptionRequestException
		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void serverErrorThrowsSubscriptionRequestException() {

		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");
		SubscriptionRequestApi subscriptionRequestApi = new SubscriptionRequestApi("ericsson", Collections.singleton(subscription));


		ResponseEntity<SubscriptionRequestApi> response = new ResponseEntity<>(subscriptionRequestApi, HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST), any(HttpEntity.class), eq(SubscriptionRequestApi.class))).thenReturn(response);

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);

	}


}
