package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class NeighbourRESTFacade {

	private Logger logger = LoggerFactory.getLogger(NeighbourRESTFacade.class);
	private String capabilityExchangePath;
	private String subscriptionRequestPath;
	private RestTemplate restTemplate;
	private CapabilityTransformer capabilityTransformer;
	private SubscriptionTransformer subscriptionTransformer;

	@Autowired
	public NeighbourRESTFacade(@Value("${path.subscription-request}") String subscriptionRequestPath,
							   @Value("${path.capabilities-exchange}") String capabilityExchangePath,
							   RestTemplate restTemplate,
							   CapabilityTransformer capabilityTransformer,
							   SubscriptionTransformer subscriptionTransformer) {

		this.capabilityExchangePath = capabilityExchangePath;
		this.subscriptionRequestPath = subscriptionRequestPath;
		this.restTemplate = restTemplate;
		this.capabilityTransformer = capabilityTransformer;
		this.subscriptionTransformer = subscriptionTransformer;
	}

	String getUrl(Interchange neighbour) {
		return "https://" + neighbour.getName() + neighbour.getDomainName() + ":" + neighbour.getControlChannelPort();
	}

	// Posts representation local service provider capabilities to neighbour
	Interchange postCapabilities(Interchange discoveringInterchange, Interchange neighbour) {

		String url = getUrl(neighbour) + capabilityExchangePath;
		logger.debug("Posting capabilities to {} on URL: {}", neighbour.getName(), url);
		logger.debug("Representation of discovering interchange: {}", discoveringInterchange.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Convert discovering interchange to CapabilityApi object and post to neighbour
		CapabilityApi discoveringInterchangeToCapabilityApi = capabilityTransformer.interchangeToCapabilityApi(discoveringInterchange);
		HttpEntity<CapabilityApi> entity = new HttpEntity<>(discoveringInterchangeToCapabilityApi, headers);

		ResponseEntity<CapabilityApi> response = restTemplate.exchange(url, HttpMethod.POST, entity, CapabilityApi.class);

		logger.debug("Response from {}: {}", neighbour.getName(), response.toString());

		if (response.getBody() == null) {
			throw new CapabilityPostException("Post response from interchange gave null object. Unsuccessful capabilities exchange. ");
		}

		// Convert response API object to Interchange representation of neighbour.
		CapabilityApi neighbourResponse = response.getBody();
		Interchange neighbourInterchangeRepresentation = capabilityTransformer.capabilityApiToInterchange(neighbourResponse);

		HttpStatus responseStatusCode = response.getStatusCode();

		if (responseStatusCode == HttpStatus.OK) {
			logger.info("Response code for posting subscription request to {} is {}", url, response.getStatusCodeValue());
			return neighbourInterchangeRepresentation;
		} else {
			throw new CapabilityPostException("Unable to post capabilities to neighbour " + neighbour.getName());
		}
	}


	SubscriptionRequest postSubscriptionRequest(Interchange discoveringInterchange, Interchange neighbour) {

		String url = getUrl(neighbour) + subscriptionRequestPath;
		logger.debug("Posting subscription request to {} on URL: {}", neighbour.getName(), url);
		logger.debug("Representation of discovering interchange: {}", discoveringInterchange.toString());

		// Post representation to neighbour
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		SubscriptionRequestApi subscriptionRequestApi = subscriptionTransformer.interchangeToSubscriptionRequestApi(discoveringInterchange);
		HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi, headers);

		// Posting and receiving response
		ResponseEntity<SubscriptionRequestApi> responseApi = restTemplate.exchange(url, HttpMethod.POST, entity, SubscriptionRequestApi.class);

		if (responseApi.getBody() == null) {
			throw new SubscriptionRequestException("Subscription request failed. Post response from neighbour gave null object.");
		}

		// convert response back to interchange.
		Interchange responseInterchange = subscriptionTransformer.subscriptionRequestApiToInterchange(responseApi.getBody());

		HttpStatus statusCode = responseApi.getStatusCode();

		if (!discoveringInterchange.getSubscriptionRequest().getSubscriptions().isEmpty() && responseInterchange.getSubscriptionRequest().getSubscriptions().isEmpty()) {
			// we posted a non empty subscription request, but received an empty subscription request.
			throw new SubscriptionRequestException("Subscription request failed. Posted non-empty subscription request, but received response with empty subscription request from neighbour.");
		} else if (statusCode != HttpStatus.ACCEPTED) {
			throw new SubscriptionRequestException("Subscription request failed. Neighbour returned bad status code:  " + responseApi.getStatusCodeValue());
		} else {
			// Everything went ok, set status of subscription request to requested.
			logger.info("Response code for posting subscription request to {} is {}", url, responseApi.getStatusCodeValue());
			responseInterchange.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);
			return responseInterchange.getSubscriptionRequest();
		}
	}

	Subscription pollSubscriptionStatus(Subscription subscription, Interchange neighbour) {

		String url = getUrl(neighbour) + "/" + subscription.getPath();
		logger.debug("URL: " + url);

		ResponseEntity<Subscription> response = restTemplate.getForEntity(url, Subscription.class);

		if(response.getBody() == null){
			throw new SubscriptionPollException("Polling subscription failed. GET response from neighbour was null.");
		}

		Subscription responseSubscription = response.getBody();
		HttpStatus statusCode = response.getStatusCode();

		if (statusCode != HttpStatus.OK) {
			logger.info("Response code: {}", statusCode);
			logger.info("Response body: {}", responseSubscription.toString());
			throw new SubscriptionPollException("Polling subscription failed. Neighbour returned bad status code.");
		} else {
			logger.info("Response code: {}", statusCode);
			logger.info("Received response: " + responseSubscription.toString());
			return responseSubscription;
		}

	}


}
