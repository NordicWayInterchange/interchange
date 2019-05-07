package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class QpidClient {
	private final Logger logger = LoggerFactory.getLogger(QpidClient.class);
	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s/nwEx";
	private static final String QUEUES_URL_PATTERN = "%s/api/latest/queue/default/%s";
	private static final String PING_URL_PATTERN = "%s/api/latest/virtualhost/default/%s";

	private final String exchangeURL;
	private final String queuesURL;
	private final String pingURL;

	private final RestTemplate restTemplate;

	@Autowired
	public QpidClient(@Value("${qpid.rest.api.baseUrl}") String baseUrl,
					  @Value("${qpid.rest.api.vhost}") String vhostName,
					  RestTemplate restTemplate) {
		this.exchangeURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.restTemplate = restTemplate;
	}

	int ping() {
		ResponseEntity<String> response = restTemplate.getForEntity(pingURL, String.class);
		logger.debug(response.getBody());
		return response.getStatusCodeValue();
	}

	// A method that posts a json object to the Qpid REST api, using a given URI and a given command.
	private void postQpid(String urlString, String message, String command) {
		String url = urlString + command;
		logger.debug("POST to QPID URL {} with message {} ", url, message);

		ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);
		logger.debug("Resonse code for POST to {} with payload {} is {}", url, message, response.getStatusCodeValue());
		if (response.getStatusCode().isError()) {
			String errorMessage = String.format("Error posting to QPID REST API %s with message %s, cause: %s",
					url,
					message,
					response.getStatusCode().getReasonPhrase());
			logger.error(errorMessage);
			throw new RoutingConfigurerException(errorMessage);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateBinding(String binding, String queueName, String bindingKey) {
		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", bindingKey);
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", binding);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		postQpid(exchangeURL, jsonString, "/bind");
	}

	@SuppressWarnings("unchecked")
	void createQueue(Interchange interchange) {
		JSONObject json = new JSONObject();
		json.put("name", interchange.getName());
		json.put("durable", true);
		String jsonString = json.toString();
		postQpid(queuesURL, jsonString, "/");
	}

	boolean queueExists(String queueName) {
		return lookupQueueId(queueName) != null;
	}

	private String lookupQueueId(String queueName) {
		String queueQueryUrl = queuesURL + "/" + queueName;
		logger.debug("quering for queue {} with url {}", queueName, queueQueryUrl);
		ResponseEntity<HashMap> response;
		try {
			response = restTemplate.getForEntity(new URI(queueQueryUrl), HashMap.class);
		} catch (HttpClientErrorException.NotFound notFound) {
			return null;
		} catch (Throwable e) {
			throw new RoutingConfigurerException(String.format("Could not query for QPID queue %s", queueName), e);
		}
		HttpStatus statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful()) {
			if (response.getBody() != null) {
				return (String) response.getBody().get("id");
			}
		} else {
			logger.error("Status code {} querying for QPID queue {}", statusCode.value(), queueName);
		}
		return null;
	}

	public SubscriptionRequest setupRouting(Interchange toSetUp) {
		if (queueExists(toSetUp.getName())) {
			unbindOldUnwantedBindings(toSetUp);
		} else {
			createQueue(toSetUp);
		}
		SubscriptionRequest subscriptionRequest = toSetUp.getSubscriptionRequest();
		for (Subscription subscription : subscriptionRequest.getSubscriptions()) {
			updateBinding(subscription.getSelector(), toSetUp.getName(), bindKey(toSetUp, subscription));
			subscription.setSubscriptionStatus(Subscription.SubscriptionStatus.CREATED);
		}
		subscriptionRequest.setStatus(SubscriptionRequest.SubscriptionRequestStatus.ESTABLISHED);
		return subscriptionRequest;
	}

	private void unbindOldUnwantedBindings(Interchange interchange) {
		Set<String> unwantedBindKeys = getUnwantedBindKeys(interchange);
		for (String unwantedBindKey : unwantedBindKeys) {
			unbindBindKey(interchange, unwantedBindKey);
		}
	}

	@SuppressWarnings("unchecked")
	private void unbindBindKey(Interchange interchange, String unwantedBindKey) {
		JSONObject json = new JSONObject();
		json.put("destination", interchange.getName());
		json.put("bindingKey", unwantedBindKey);
		String jsonString = json.toString();

		postQpid(exchangeURL, jsonString, "/unbind");
	}

	private Set<String> getUnwantedBindKeys(Interchange interchange) {
		Set<String> existingBindKeys = getQueueBindKeys(interchange.getName());
		Set<String> wantedBindKeys = wantedBindings(interchange);
		Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
	}

	private Set<String> wantedBindings(Interchange interchange) {
		Set<String> wantedBindings = new HashSet<>();
		for (Subscription subscription : interchange.getSubscriptionRequest().getSubscriptions()) {
			wantedBindings.add(bindKey(interchange, subscription));
		}
		return wantedBindings;
	}

	private String bindKey(Interchange interchange, Subscription subscription) {
		return interchange.getName() + "-" + subscription.getSelector().hashCode();
	}

	Set<String> getQueueBindKeys(String queueName) {
		HashSet<String> existingBindKeys = new HashSet<>();
		String url = queuesURL + "/" + queueName + "/getPublishingLinks";

		ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
				url,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Map<String, Object>>>() {
				});
		List<Map<String, Object>> queueBindings = response.getBody();
		if (queueBindings != null) {
			for (Map<String, Object> binding : queueBindings) {
				Object bindingKey = binding.get("bindingKey");
				if (bindingKey instanceof String) {
					existingBindKeys.add((String) bindingKey);
				}
			}
		}
		return existingBindKeys;
	}

	public void removeQueue(String queueName) {
		String queueId = lookupQueueId(queueName);
		restTemplate.delete(queuesURL + "?id=" + queueId);
	}
}