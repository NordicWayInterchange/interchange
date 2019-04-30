package no.vegvesen.ixn.federation.discoverer;

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

	private String subscriptionRequestPath = "/requestSubscription";
	private String capabilityExchangePath = "/updateCapabilities";

	@Spy
	private NeighbourRESTFacade neighbourRESTFacade = new NeighbourRESTFacade(subscriptionRequestPath, capabilityExchangePath, restTemplate);

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
		String expectedURL = "http://ericsson.itsinterchange.eu:8080";
		String actualURL = neighbourRESTFacade.getUrl(ericsson);

		Assert.assertEquals(expectedURL, actualURL);
	}

	@Test(expected = CapabilityPostException.class)
	public void nullCapabilitiesPostResponseThrowsCapabilityPostException() {

		Interchange returnInterchange = new Interchange();
		returnInterchange.setName("ReturnInterchange");
		returnInterchange = null;
		ResponseEntity<Interchange> response = new ResponseEntity<>(returnInterchange, HttpStatus.CREATED);
		when(restTemplate.postForEntity(any(String.class), any(Interchange.class), eq(Interchange.class))).thenReturn(response);

		neighbourRESTFacade.postCapabilities(ericsson, ericsson);

	}

	@Test(expected = CapabilityPostException.class)
	public void unsuccessfulPostOfCapabilitiesThrowsCapabilityPostException(){
		Interchange returnInterchange = new Interchange();
		returnInterchange.setName("ReturnInterchange");

		ResponseEntity<Interchange> response = new ResponseEntity<>(returnInterchange, HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.postForEntity(any(String.class), any(Interchange.class), eq(Interchange.class))).thenReturn(response);

		neighbourRESTFacade.postCapabilities(ericsson, ericsson);
	}


	@Test(expected = SubscriptionRequestException.class)
	public void nullSubscriptionRequestResponseThrowsSubscriptionRequestException(){

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");

		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));
		subscriptionRequest=null;

		ResponseEntity<SubscriptionRequest> response = new ResponseEntity<>(subscriptionRequest, HttpStatus.ACCEPTED);
		when(restTemplate.exchange(any(String.class),eq(HttpMethod.POST),any(HttpEntity.class), eq(SubscriptionRequest.class))).thenReturn(response);

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);
	}

	@Test(expected = SubscriptionRequestException.class)
	public void subscriptionRequestResponseIsEmptyListOfSubscriptions(){


		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();

		subscriptionRequest.setSubscriptions(Collections.emptySet());

		ResponseEntity<SubscriptionRequest> response = new ResponseEntity<>(subscriptionRequest, HttpStatus.ACCEPTED);
		when(restTemplate.exchange(any(String.class),eq(HttpMethod.POST),any(HttpEntity.class), eq(SubscriptionRequest.class))).thenReturn(response);

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);

	}

	@Test(expected = SubscriptionRequestException.class)
	public void failedSubscriptionRequestThrowsException(){

		SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
		Subscription subscription = new Subscription();
		subscription.setSelector("where LIKE 'NO'");

		subscriptionRequest.setSubscriptions(Collections.singleton(subscription));

		ResponseEntity<SubscriptionRequest> response = new ResponseEntity<>(subscriptionRequest, HttpStatus.INTERNAL_SERVER_ERROR);
		when(restTemplate.exchange(any(String.class),eq(HttpMethod.POST),any(HttpEntity.class), eq(SubscriptionRequest.class))).thenReturn(response);

		neighbourRESTFacade.postSubscriptionRequest(ericsson, ericsson);

	}



}
