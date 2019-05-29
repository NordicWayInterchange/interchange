package no.vegvesen.ixn.federation.discoverer;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;

import static no.vegvesen.ixn.federation.api.v1_0.RESTEndpointPaths.*;

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

	Interchange postCapabilities(Interchange discoveringInterchange, Interchange neighbour) {

		String url = neighbour.getControlChannelUrl(CAPABILITIES_PATH);
		logger.debug("Posting capabilities to {} on URL: {}", neighbour.getName(), url);
		logger.debug("Representation of discovering interchange: {}", discoveringInterchange.toString());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		// Convert discovering interchange to CapabilityApi object and post to neighbour
		CapabilityApi discoveringInterchangeToCapabilityApi = capabilityTransformer.interchangeToCapabilityApi(discoveringInterchange);
		HttpEntity<CapabilityApi> entity = new HttpEntity<>(discoveringInterchangeToCapabilityApi, headers);

		try {
			ResponseEntity<CapabilityApi> response = restTemplate.exchange(url, HttpMethod.POST, entity, CapabilityApi.class);
			CapabilityApi capabilityApi = response.getBody();

			logger.debug("Successful post of capabilities to neighbour. Response from server is: {}", capabilityApi.toString());

			return capabilityTransformer.capabilityApiToInterchange(capabilityApi);

		}catch(HttpServerErrorException | HttpClientErrorException e){


			logger.error("Failed post of capabilities to neighbour. Server returned error code: {}", e.getStatusCode().toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new CapabilityPostException("Error in posting capabilities to neighbour " + neighbour.getName() +". Received error response: " + errorDetails.toString());
			} catch (JsonMappingException jme) {
				logger.error("Unable to cast error response as ErrorDetails object.");
				throw new CapabilityPostException("Error in posting capabilities to neighbour " + neighbour.getName() +". Could not map server response to ErrorDetailsobject.");
			} catch (IOException ioe) {
				logger.error("Unable to cast error response as ErrorDetails object.");
				throw new CapabilityPostException("Unable to post capabilities to neighbour " + neighbour.getName() +". Could not map server response to ErrorDetails object.");
			}
		}
	}


	SubscriptionRequest postSubscriptionRequest(Interchange discoveringInterchange, Interchange neighbour) {

		String url = neighbour.getControlChannelUrl(SUBSCRIPTION_PATH);
		logger.debug("Posting subscription request to {} on URL: {}", neighbour.getName(), url);
		logger.debug("Representation of discovering interchange: {}", discoveringInterchange.toString());

		// Post representation to neighbour
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		SubscriptionRequestApi subscriptionRequestApi = subscriptionRequestTransformer.interchangeToSubscriptionRequestApi(discoveringInterchange);
		HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi, headers);

		// Posting and receiving response

		try {
			ResponseEntity<SubscriptionRequestApi> response = restTemplate.exchange(url, HttpMethod.POST, entity, SubscriptionRequestApi.class);
			SubscriptionRequestApi responseApi = response.getBody();
			Interchange responseInterchange = subscriptionRequestTransformer.subscriptionRequestApiToInterchange(responseApi);

			if (!discoveringInterchange.getSubscriptionRequest().getSubscriptions().isEmpty() && responseInterchange.getSubscriptionRequest().getSubscriptions().isEmpty()) {
				// we posted a non empty subscription request, but received an empty subscription request.
				logger.error("Posted non empty subscription request to neighbour but received empty subscription request.");
				throw new SubscriptionRequestException("Subscription request failed. Posted non-empty subscription request, but received response with empty subscription request from neighbour.");
			}

			responseInterchange.getSubscriptionRequest().setStatus(SubscriptionRequest.SubscriptionRequestStatus.REQUESTED);

			logger.info("Response code for posting subscription request to {} is {}", url, response.getStatusCodeValue());
			logger.debug("Successful post of subscription request to neighbour. Response is: {}", responseApi.toString());

			return responseInterchange.getSubscriptionRequest();

		}catch(HttpClientErrorException | HttpServerErrorException e){

			HttpStatus status = e.getStatusCode();
			logger.error("Failed post of subscription request to neighbour. Server returned error code: {}", status.toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new SubscriptionRequestException("Subscription request failed. Received error object from server: " + errorDetails.toString());
			} catch (JsonMappingException jme) {
				logger.error("Unable to cast response as ErrorDetails object. ");
				throw new SubscriptionRequestException("Subscription request failed. Could not map server response to Error object.");
			} catch (IOException ioe) {
				logger.error("Unable to cast response as ErrorDetails object. ");
				throw new SubscriptionRequestException("Subscription request failed. Could not map server response to Error object." );
			}
		}
	}

	Subscription pollSubscriptionStatus(Subscription subscription, Interchange neighbour) {

		String url = neighbour.getControlChannelUrl(subscription.getPath());

		try {
			ResponseEntity<SubscriptionApi> response = restTemplate.getForEntity(url, SubscriptionApi.class);
			SubscriptionApi subscriptionApi = response.getBody();
			Subscription returnSubscription = subscriptionTransformer.subscriptionApiToSubscription(subscriptionApi);

			logger.info("Successfully polled subscription with url: {}.", url);
			logger.info("Response code: {}", response.getStatusCodeValue());
			logger.info("Received subscription poll response: {}", returnSubscription.toString());

			return returnSubscription;

		} catch (HttpClientErrorException | HttpServerErrorException e) {

			HttpStatus status = e.getStatusCode();
			logger.error("Failed polling subscription {}. Server returned error code: {}", url, status.toString());

			byte[] errorResponse = e.getResponseBodyAsByteArray();

			try {
				ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);

				logger.error("Received error object from server: {}", errorDetails.toString());
				throw new SubscriptionPollException("Error in polling " + url + " for subscription status. Received error response from server: " + status.toString());
			} catch (JsonMappingException jme) { // TODO: subclass of IOException - should we catch both?
				logger.error("Unable to cast response as ErrorDetails object. ");
				throw new SubscriptionPollException("Received response with status code :" + status.toString() + ". Error in parsing server response as Error Details object. ");
			} catch (IOException ioe) {
				logger.error("Unable to cast response as ErrorDetails object. ");
				throw new SubscriptionPollException("Received response with status code :" + status.toString() + ". Error in parsing server response as Error Details object. ");
			}
		}
	}
}
