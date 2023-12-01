package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.*;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionPollResponseApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.exceptions.*;
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

@Component
public class NeighbourRESTClient {
    private Logger logger = LoggerFactory.getLogger(NeighbourRESTClient.class);

    RestTemplate restTemplate;
    ObjectMapper mapper;

    @Autowired
    public NeighbourRESTClient(RestTemplate template, ObjectMapper mapper) {
        this.restTemplate = template;
        this.mapper = mapper;
    }

    CapabilitiesSplitApi doPostCapabilities(String controlChannelUrl, String name, CapabilitiesSplitApi selfCapability) {
        CapabilitiesSplitApi result;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Convert discovering Neighbour to CapabilityApi object and post to neighbour
        HttpEntity<CapabilitiesSplitApi> entity = new HttpEntity<>(selfCapability, headers);
		logHttpEntity(entity, "Posting");

		try {
            ResponseEntity<CapabilitiesSplitApi> response = restTemplate.exchange(controlChannelUrl, HttpMethod.POST, entity, CapabilitiesSplitApi.class);
			logHttpEntity(response, "Received");

            if (response.getBody() != null) {
                result = response.getBody();
			} else {
                throw new CapabilityPostException(String.format("Server %s returned http code %s with null capability response", name, response.getStatusCodeValue()));
            }

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            logger.debug("Failed post of capabilities to neighbour with url {}\nRequest body: {} \nServer returned error code: {}", controlChannelUrl, entity, e.getStatusCode());

            byte[] errorResponse = e.getResponseBodyAsByteArray();

            try {
                ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
                logger.debug("Received error object from server: {}", errorDetails.toString());
                throw new CapabilityPostException(name, errorDetails);
            } catch (IOException ioe) {
                logger.debug("Unable to cast error response as ErrorDetails object.", ioe);
                throw new CapabilityPostException(name, e.getStatusCode().value(),e);
            }
        } catch (RestClientException e) {
            logger.debug("Failed post of capabilities to neighbour, network layer error", e);
            throw new CapabilityPostException("Error in posting capabilities to neighbour " + name + " due to exception", e);

        }
        return result;
    }

	private void logHttpEntity(HttpEntity<?> entity, String logPrefix) {
		if (entity.hasBody()) {
			Object body = entity.getBody();
			assert body != null;
			try {
				logger.debug("{} {} object: {}", logPrefix, body.getClass().getSimpleName(), mapper.writeValueAsString(body));
			} catch (JsonProcessingException e) {
				logger.warn("Could not convert {} to json string {}", body.getClass().getSimpleName(), body.toString(), e);
			}
		} else {
			logger.warn("{} Expected body not to be null {}", logPrefix, entity);
		}
		logger.debug("{} HttpEntity: {}", logPrefix, entity.toString());
		logger.debug("{} Headers: {}", logPrefix, entity.getHeaders().toString());
	}

	SubscriptionResponseApi doPostSubscriptionRequest(SubscriptionRequestApi subscriptionRequestApi, String controlChannelUrl, String neighbourName) {
        // Post representation to neighbour
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi, headers);
        logHttpEntity(entity, "Posting");

        // Posting and receiving response

        SubscriptionResponseApi responseApi;
        try {
            ResponseEntity<SubscriptionResponseApi> response = restTemplate.exchange(controlChannelUrl, HttpMethod.POST, entity, SubscriptionResponseApi.class);
            logHttpEntity(response, "Received");

            if (response.getBody() == null) {
                throw new SubscriptionRequestException(String.format("%s returned empty response from subscription request",neighbourName));
            }
            responseApi = response.getBody();
            logger.debug("Successfully posted a subscription request. Response code: {}", response.getStatusCodeValue());

            //TODO this might actually warrant an exception of its own. A requested subscription is not accepted
            //TODO or should it be handled at a higher level?
            if (!subscriptionRequestApi.getSubscriptions().isEmpty() && responseApi.getSubscriptions().isEmpty()) {
                // we posted a non empty subscription request, but received an empty subscription request.
                logger.debug("Posted non empty subscription request to neighbour but received empty subscription request.");
                throw new SubscriptionRequestException("Subscription request failed. Posted non-empty subscription request, but received response with empty subscription request from neighbour " + neighbourName + ".");
            }


        } catch (HttpClientErrorException | HttpServerErrorException e) {

            HttpStatusCode code = e.getStatusCode();
            logger.debug("Failed post of subscription request to neighbour with url {} \nRequest body: {} \nServer returned error code: {}", controlChannelUrl, entity, code);

            byte[] errorResponse = e.getResponseBodyAsByteArray();

            try {
                ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
                logger.debug("Received error object from server: {}", errorDetails.toString());
                throw new SubscriptionRequestException(String.format("Subscription request to %s failed. Received error object from server: %s",neighbourName,errorDetails),e);
            } catch (IOException ioe) {
                logger.debug("Unable to cast response as ErrorDetails object.", ioe);
                throw new SubscriptionRequestException(String.format("Subscription request to %s failed.",neighbourName),e);
            }
        } catch (RestClientException e) {
            logger.debug("Received network layer error", e);
            throw new SubscriptionRequestException("Error in posting capabilities to neighbour " + neighbourName + " due to exception", e);
        }
        return responseApi;
    }

    SubscriptionPollResponseApi doPollSubscriptionStatus(String url, String name) {
        SubscriptionPollResponseApi subscriptionApi;
        try {
            ResponseEntity<SubscriptionPollResponseApi> response = restTemplate.getForEntity(url, SubscriptionPollResponseApi.class);
            logHttpEntity(response, "Poll subscription");
            subscriptionApi = response.getBody();


        } catch (HttpClientErrorException | HttpServerErrorException e) {

            HttpStatusCode status = e.getStatusCode();

            logger.debug("Failed polling subscription with url {}. Server returned error code: {}", url, status);


            byte[] errorResponse = e.getResponseBodyAsByteArray();
            logger.debug(String.format("Response has length %d",errorResponse.length));
            if (errorResponse.length > 0 ) {
                try {
                    ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);

                    logger.debug("Received error object from server: {}", errorDetails.toString());
                } catch (IOException ioe) {
                    logger.error("Unable to cast response as ErrorDetails object.", ioe);
                }
            }
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                throw new SubscriptionNotFoundException(String.format("Subscription not found when polling URL %s", url));
            }
            throw new SubscriptionPollException("Error in polling " + url + " for subscription status. Received error response from server: " + status.toString());
        } catch (RestClientException e) {
            logger.debug("Received network layer error", e);
            throw new SubscriptionPollException("Error in posting capabilities to neighbour " + name + " due to exception", e);
        }
        return subscriptionApi;
    }

    public void deleteSubscriptions(String url, String name) {
        try {
            restTemplate.delete(url);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            HttpStatusCode status = e.getStatusCode();
            logger.debug("Failed deleting subscription with url {}. Server returned error code {}", url, status);
            if (HttpStatus.NOT_FOUND.equals(status)) {
                throw new SubscriptionNotFoundException("Error in deleting subscription to neighbour " + name + " due to exception", e);
            }
            throw new SubscriptionDeleteException("Error in deleting subscription to neighbour " + name + " due to exception", e);
        } catch (RestClientException e) {
            logger.error("Failed deleting subscription with url {}. ", url, e);
            throw new SubscriptionDeleteException("Network layer exception caught", e);
        }
    }
}