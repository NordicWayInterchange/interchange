package no.vegvesen.ixn.federation.discoverer;

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

	@Autowired
	public NeighbourRESTFacade(@Value("${path.subscription-request}") String subscriptionRequestPath,
						@Value("${path.capabilities-exchange}") String capabilityExchangePath,
						RestTemplate restTemplate) {

		this.capabilityExchangePath = capabilityExchangePath;
		this.subscriptionRequestPath = subscriptionRequestPath;
		this.restTemplate = restTemplate;
	}

	String getUrl(Interchange neighbour) {
		return "http://" + neighbour.getName() + neighbour.getDomainName() + ":" + neighbour.getControlChannelPort();
	}

	Interchange postCapabilities(Interchange discoveringInterchange, Interchange neighbour) {

		String url = getUrl(neighbour) + capabilityExchangePath;
		logger.info("Posting capabilities to URL: " + url);
		logger.info("Discovering node representation: \n" + discoveringInterchange.toString());

		ResponseEntity<Interchange> response = restTemplate.postForEntity(url, discoveringInterchange, Interchange.class);
		logger.info("Response: " + response.toString());

		if (response.getBody() == null) {
			throw new CapabilityPostException("Post response from interchange gave null object. Unsuccessful capabilities exchange. ");
		}

		Interchange neighbourResponse = response.getBody();
		logger.info("Response.getBody(): " + neighbourResponse.toString());
		HttpStatus responseStatusCode = response.getStatusCode();
		logger.info("Response status code: " + response.getStatusCodeValue());

		if (responseStatusCode == HttpStatus.CREATED) {
			return neighbourResponse;
		} else {
			throw new CapabilityPostException("Unable to post capabilities to neighbour " + neighbour.getName());
		}
	}


	SubscriptionRequest postSubscriptionRequest(Interchange discoveringInterchange, Interchange neighbourDestination) {
		String url = getUrl(neighbourDestination) + subscriptionRequestPath;
		logger.info("Posting subscription request to {} on URL: ", neighbourDestination.getName(), url);
		logger.info("Representation of discovering interchange: \n" + discoveringInterchange.toString());

		// Post representation to neighbour
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Interchange> entity = new HttpEntity<>(discoveringInterchange, headers);
		ResponseEntity<SubscriptionRequest> response = restTemplate.exchange(url, HttpMethod.POST, entity, SubscriptionRequest.class);

		if (response.getBody() == null) {
			throw new SubscriptionRequestException("Subscription request failed. Post response from neighbour gave null object.");
		}

		logger.info("Response code: " + response.getStatusCodeValue());

		SubscriptionRequest returnedSubscriptionRequestWithStatus = response.getBody();
		logger.info("Response.getBody(): " + returnedSubscriptionRequestWithStatus.toString());

		HttpStatus statusCode = response.getStatusCode();

		// TODO: What if we post an empty subscription - should the server return something else than an empty list?
		// TODO: is empty list a legal or an illegal response?

		if (returnedSubscriptionRequestWithStatus.getSubscriptions().isEmpty()) {
			throw new SubscriptionRequestException("Subscription request failed. Post response from neighbour gave empty list of subscriptions.");
		} else if (statusCode != HttpStatus.ACCEPTED) {
			throw new SubscriptionRequestException("Subscription request failed. Neighbour returned bad status code:  " + response.getStatusCodeValue());
		} else {
			logger.info("Response code for POST to {} is {}", url, response.getStatusCodeValue());
			return returnedSubscriptionRequestWithStatus;
		}
	}

	Subscription pollSubscriptionStatus(Subscription subscription, Interchange neighbour) {

		// ask for update on status of subscription
		logger.info("Polling neighbour {} for status on subscription with path {}", neighbour.getName(), subscription.getPath());

		String url = getUrl(neighbour) + "/" + subscription.getPath();
		logger.info("URL: " + url);

		ResponseEntity<Subscription> response = restTemplate.getForEntity(url, Subscription.class);

		if(response.getBody() == null){
			throw new SubscriptionPollException("Polling subscription failed. Get response from neighbour was null.");
		}

		Subscription responseSubscription = response.getBody();
		HttpStatus statusCode = response.getStatusCode();
		logger.info("Response code: {}", statusCode);
		logger.info("Response body: {}", responseSubscription.toString());

		if (statusCode != HttpStatus.OK) {
			throw new SubscriptionPollException("Polling subscription failed. Neighbour returned bad status code.");
		} else {
			logger.info("Received response: " + responseSubscription.toString());
			return responseSubscription;
		}

	}


}
