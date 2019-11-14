package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.Self;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.CAPABILITIES_PATH;
import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.SUBSCRIPTION_PATH;

@Component
public class NeighbourRESTFacade {

	private Logger logger = LoggerFactory.getLogger(NeighbourRESTFacade.class);
	private RestTemplate restTemplate;
	private CapabilityTransformer capabilityTransformer;
	private SubscriptionTransformer subscriptionTransformer;
	private SubscriptionRequestTransformer subscriptionRequestTransformer;
	private ObjectMapper mapper;

	@Autowired
	public NeighbourRESTFacade(RestTemplate restTemplate,
							   CapabilityTransformer capabilityTransformer,
							   SubscriptionTransformer subscriptionTransformer,
							   SubscriptionRequestTransformer subscriptionRequestTransformer,
							   ObjectMapper mapper) {
		this.restTemplate = restTemplate;
		this.capabilityTransformer = capabilityTransformer;
		this.subscriptionTransformer = subscriptionTransformer;
		this.subscriptionRequestTransformer = subscriptionRequestTransformer;
		this.mapper = mapper;
	}

	Neighbour postCapabilities(Neighbour discoveringNeighbour, Neighbour neighbour) {

		String url = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		String neighbourName = neighbour.getName();
		return doPostCapabilities(discoveringNeighbour, url, neighbourName);
	}

	Neighbour postCapabilities(Self self, Neighbour neighbour) {
		return innerDoPostCapabilities(neighbour.getControlChannelUrl(CAPABILITIES_PATH),neighbour.getName(),capabilityTransformer.selfToCapabilityApi(self));
	}

	private Neighbour doPostCapabilities(Neighbour discoveringNeighbour, String url, String neighbourName) {
		logger.debug("Posting capabilities to {} on URL: {}", neighbourName, url);
		logger.debug("Representation of discovering Neighbour: {}", discoveringNeighbour.toString());
		CapabilityApi discoveringNeighbourCapabilityApi = capabilityTransformer.neighbourToCapabilityApi(discoveringNeighbour);

		return innerDoPostCapabilities(url, neighbourName, discoveringNeighbourCapabilityApi);
	}

	private Neighbour innerDoPostCapabilities(String url, String neighbourName, CapabilityApi discoveringNeighbourToCapabilityApi) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Convert discovering Neighbour to CapabilityApi object and post to neighbour
		HttpEntity<CapabilityApi> entity = new HttpEntity<>(discoveringNeighbourToCapabilityApi, headers);
		logger.debug("Posting capability api object: {}", discoveringNeighbourToCapabilityApi.toString());
		logger.debug("Posting HttpEntity: {}", entity.toString());
		logger.debug("Posting Headers: {}", headers.toString());

		try {
			ResponseEntity<CapabilityApi> response = restTemplate.exchange(url, HttpMethod.POST, entity, CapabilityApi.class);
			logger.debug("Received capability api: {}", response.getBody());
			logger.debug("Received response entity: {}", response.toString());
			logger.debug("Received headers: {}", response.getHeaders().toString());

			if (response.getBody() != null) {
				CapabilityApi capabilityApi = response.getBody();
				logger.debug("Successful post of capabilities to neighbour. Response from server is: {}", capabilityApi == null ? "null" : capabilityApi.toString());
				return capabilityTransformer.capabilityApiToNeighbour(capabilityApi);
			} else {
				throw new CapabilityPostException(String.format("Server returned http code %s with null capability response", response.getStatusCodeValue()));
			}

		}catch(HttpServerErrorException | HttpClientErrorException e){


			logger.error("Failed post of capabilities to neighbour. Server returned error code: {}", e.getStatusCode().toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new CapabilityPostException("Error in posting capabilities to neighbour " + neighbourName +". Received error response: " + errorDetails.toString());
			} catch (IOException ioe) {
				logger.error("Unable to cast error response as ErrorDetails object.", ioe);
				throw new CapabilityPostException("Error in posting capabilities to neighbour " + neighbourName +". Could not map server response to ErrorDetailsobject.");
			}
		} catch (RestClientException e) {
			logger.error("Failed post of capabilities to neighbour, network layer error",e);
			throw new CapabilityPostException("Error in posting capabilities to neighbour " + neighbourName + " due to exception",e);

		}
	}


	SubscriptionRequest postSubscriptionRequest(Neighbour discoveringNeighbour, Neighbour neighbour) {

		String url = neighbour.getControlChannelUrl(SUBSCRIPTION_PATH);
		logger.debug("Posting subscription request to {} on URL: {}", neighbour.getName(), url);

		// Post representation to neighbour
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.neighbourToSubscriptionRequestApi(discoveringNeighbour);
		HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi, headers);
		logger.debug("Posting Subscription request api object: {}", subscriptionRequestApi.toString());
		logger.debug("Posting HttpEntity: {}", entity.toString());
		logger.debug("Posting Headers: {}", headers.toString());

		// Posting and receiving response

		try {
			ResponseEntity<SubscriptionRequestApi> response = restTemplate.exchange(url, HttpMethod.POST, entity, SubscriptionRequestApi.class);
			logger.debug("Received subscription request api: {}", response.getBody());
			logger.debug("Received response entity: {}", response.toString());
			logger.debug("Received headers: {}", response.getHeaders().toString());

			if (response.getBody() == null) {
				throw new SubscriptionRequestException("Returned empty response from subscription request");
			}
			SubscriptionRequestApi responseApi = response.getBody();
			logger.debug("Received response object: {}", responseApi.toString());
			Neighbour responseNeighbour = subscriptionRequestTransformer.subscriptionRequestApiToNeighbour(responseApi);

			if (!discoveringNeighbour.getSubscriptionRequest().getSubscriptions().isEmpty() && responseNeighbour.getSubscriptionRequest().getSubscriptions().isEmpty()) {
				// we posted a non empty subscription request, but received an empty subscription request.
				logger.error("Posted non empty subscription request to neighbour but received empty subscription request.");
				throw new SubscriptionRequestException("Subscription request failed. Posted non-empty subscription request, but received response with empty subscription request from neighbour.");
			}

			responseNeighbour.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

			logger.debug("Successfully posted a subscription request. Response code: {}", response.getStatusCodeValue());

			return responseNeighbour.getSubscriptionRequest();

		}catch(HttpClientErrorException | HttpServerErrorException e){

			HttpStatus status = e.getStatusCode();
			logger.error("Failed post of subscription request to neighbour. Server returned error code: {}", status.toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new SubscriptionRequestException("Subscription request failed. Received error object from server: " + errorDetails.toString());
			} catch (IOException ioe) {
				logger.error("Unable to cast response as ErrorDetails object.", ioe);
				throw new SubscriptionRequestException("Subscription request failed. Could not map server response to Error object." );
			}
		} catch (RestClientException e) {
			logger.error("Received network layer error",e);
			throw new SubscriptionRequestException("Error in posting capabilities to neighbour " + neighbour.getName() + " due to exception",e);
		}
	}

	Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour) {

		String url = neighbour.getControlChannelUrl(subscription.getPath());

		logger.debug("Polling subscription to {} with URL: {}", neighbour.getName(), url);

		try {
			ResponseEntity<SubscriptionApi> response = restTemplate.getForEntity(url, SubscriptionApi.class);
			SubscriptionApi subscriptionApi = response.getBody();
			Subscription returnSubscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);

			logger.debug("Successfully polled subscription. Response code: {}", response.getStatusCodeValue());
			logger.debug("Received response object: {}", returnSubscription.toString());

			return returnSubscription;

		} catch (HttpClientErrorException | HttpServerErrorException e) {

			HttpStatus status = e.getStatusCode();
			logger.error("Failed polling subscription with url {}. Server returned error code: {}", url, status.toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);

				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new SubscriptionPollException("Error in polling " + url + " for subscription status. Received error response from server: " + status.toString());
			} catch (IOException ioe) {
				logger.error("Unable to cast response as ErrorDetails object.", ioe);
				throw new SubscriptionPollException("Received response with status code :" + status.toString() + ". Error in parsing server response as Error Details object. ");
			}
		} catch (RestClientException e) {
			logger.error("Received network layer error",e);
			throw new SubscriptionPollException("Error in posting capabilities to neighbour " + neighbour.getName() + " due to exception",e);
		}
	}
}
