package no.vegvesen.ixn.federation.discoverer.facade;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.ErrorDetails;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionApi;
import no.vegvesen.ixn.federation.api.v1_0.SubscriptionRequestApi;
import no.vegvesen.ixn.federation.exceptions.CapabilityPostException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionPollException;
import no.vegvesen.ixn.federation.exceptions.SubscriptionRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    public NeighbourRESTClient(@Qualifier(value = "neighbourRestTemplate") RestTemplate template, ObjectMapper mapper) {
        this.restTemplate = template;
        this.mapper = mapper;
    }

    CapabilityApi doPostCapabilities(String controlChannelUrl, String name, CapabilityApi selfCapability) {
        CapabilityApi result;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Convert discovering Neighbour to CapabilityApi object and post to neighbour
        HttpEntity<CapabilityApi> entity = new HttpEntity<>(selfCapability, headers);
        logger.debug("Posting capability api object: {}", selfCapability.toString());
        logger.debug("Posting HttpEntity: {}", entity.toString());
        logger.debug("Posting Headers: {}", headers.toString());

        try {
            ResponseEntity<CapabilityApi> response = restTemplate.exchange(controlChannelUrl, HttpMethod.POST, entity, CapabilityApi.class);
            logger.debug("Received capability api: {}", response.getBody());
            logger.debug("Received response entity: {}", response.toString());
            logger.debug("Received headers: {}", response.getHeaders().toString());

            if (response.getBody() != null) {
                result = response.getBody();
                logger.debug("Successful post of capabilities to neighbour. Response from server is: {}", result == null ? "null" : result.toString());
            } else {
                throw new CapabilityPostException(String.format("Server returned http code %s with null capability response", response.getStatusCodeValue()));
            }

        } catch (HttpServerErrorException | HttpClientErrorException e) {
            logger.error("Failed post of capabilities to neighbour with url {}\nRequest body: {} \nServer returned error code: {}", controlChannelUrl, entity.toString(), e.getStatusCode().toString());

            byte[] errorResponse = e.getResponseBodyAsByteArray();

            try {
                ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
                logger.error("Received error object from server: {}", errorDetails.toString());
                throw new CapabilityPostException("Error in posting capabilities to neighbour " + name + ". Received error response: " + errorDetails.toString());
            } catch (IOException ioe) {
                logger.error("Unable to cast error response as ErrorDetails object.", ioe);
                throw new CapabilityPostException("Error in posting capabilities to neighbour " + name + ". Could not map server response to ErrorDetails object.");
            }
        } catch (RestClientException e) {
            logger.error("Failed post of capabilities to neighbour, network layer error", e);
            throw new CapabilityPostException("Error in posting capabilities to neighbour " + name + " due to exception", e);

        }
        return result;
    }

    SubscriptionRequestApi doPostSubscriptionRequest(SubscriptionRequestApi subscriptionRequestApi, String controlChannelUrl, String neighbourName) {
        // Post representation to neighbour
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<SubscriptionRequestApi> entity = new HttpEntity<>(subscriptionRequestApi, headers);
        logger.debug("Posting Subscription request api object: {}", subscriptionRequestApi.toString());
        logger.debug("Posting HttpEntity: {}", entity.toString());
        logger.debug("Posting Headers: {}", headers.toString());

        // Posting and receiving response

        SubscriptionRequestApi responseApi;
        try {
            ResponseEntity<SubscriptionRequestApi> response = restTemplate.exchange(controlChannelUrl, HttpMethod.POST, entity, SubscriptionRequestApi.class);
            logger.debug("Received subscription request api: {}", response.getBody());
            logger.debug("Received response entity: {}", response.toString());
            logger.debug("Received headers: {}", response.getHeaders().toString());

            if (response.getBody() == null) {
                throw new SubscriptionRequestException("Returned empty response from subscription request");
            }
            responseApi = response.getBody();
            logger.debug("Received response object: {}", responseApi.toString());
            logger.debug("Successfully posted a subscription request. Response code: {}", response.getStatusCodeValue());

            if (!subscriptionRequestApi.getSubscriptions().isEmpty() && responseApi.getSubscriptions().isEmpty()) {
                // we posted a non empty subscription request, but received an empty subscription request.
                logger.error("Posted non empty subscription request to neighbour but received empty subscription request.");
                throw new SubscriptionRequestException("Subscription request failed. Posted non-empty subscription request, but received response with empty subscription request from neighbour.");
            }


        } catch (HttpClientErrorException | HttpServerErrorException e) {

            HttpStatus status = e.getStatusCode();
            logger.error("Failed post of subscription request to neighbour with url {} \nRequest body: {} \nServer returned error code: {}", controlChannelUrl, entity.toString(), status.toString());

            byte[] errorResponse = e.getResponseBodyAsByteArray();

            try {
                ErrorDetails errorDetails = mapper.readValue(errorResponse, ErrorDetails.class);
                logger.error("Received error object from server: {}", errorDetails.toString());
                throw new SubscriptionRequestException("Subscription request failed. Received error object from server: " + errorDetails.toString());
            } catch (IOException ioe) {
                logger.error("Unable to cast response as ErrorDetails object.", ioe);
                throw new SubscriptionRequestException("Subscription request failed. Could not map server response to Error object.");
            }
        } catch (RestClientException e) {
            logger.error("Received network layer error", e);
            throw new SubscriptionRequestException("Error in posting capabilities to neighbour " + neighbourName + " due to exception", e);
        }
        return responseApi;
    }

    SubscriptionApi doPollSubscriptionStatus(String url, String name) {
        SubscriptionApi subscriptionApi;
        try {
            ResponseEntity<SubscriptionApi> response = restTemplate.getForEntity(url, SubscriptionApi.class);
            subscriptionApi = response.getBody();

            logger.debug("Successfully polled subscription. Response code: {}", response.getStatusCodeValue());


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
            logger.error("Received network layer error", e);
            throw new SubscriptionPollException("Error in posting capabilities to neighbour " + name + " due to exception", e);
        }
        return subscriptionApi;
    }
}