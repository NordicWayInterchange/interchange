package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
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
		logger.info("URL: " + url);

		ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);
		logger.debug("Resonse code for POST to {} with payload {} is {}", url, message, response.getStatusCodeValue());
		if (response.getStatusCode().isError()) {
			throw new RoutingConfigurerException(String.format("could not post to %s with payload %s", url, message));
		}
	}

	// Updates the binding of a queue to a new binding.
	private void updateBinding(String binding, String queueName, String bindingKey) {
		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", bindingKey);
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", binding);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		logger.info("Json string: " + jsonString);
		postQpid(exchangeURL, jsonString, "/bind");
	}// Creates a new queue for a neighbouring Interchange.

	void createQueue(Interchange interchange) {
		JSONObject json = new JSONObject();
		json.put("name", interchange.getName());
		json.put("durable", true);
		String jsonString = json.toString();
		logger.info("Creating queue:" + jsonString);
		postQpid(queuesURL, jsonString, "/");
	}

	boolean queueExists(String queueName) {
		return queueId(queueName) != null;
	}

	private String queueId(String queueName) {
		String queueQueryUrl = queuesURL + "/" + queueName;
		logger.info("quering for queue {} with url {}", queueName, queueQueryUrl);
		ResponseEntity<HashMap> response;
		try {
			response = restTemplate.getForEntity(new URI(queueQueryUrl), HashMap.class);
		} catch (HttpClientErrorException.NotFound notFound) {
			return null;
		} catch (URISyntaxException e) {
			throw new RoutingConfigurerException(e);
		}
		if (response.getStatusCode().is2xxSuccessful()) {
			if (response.getBody() != null) {
				return (String) response.getBody().get("id");
			}
		}
		return null;
	}

	public void setupRouting(Interchange interchange) {
		if (queueExists(interchange.getName())) {
			unbindOldUnwantedBindings(interchange);
		} else {
			createQueue(interchange);
		}
		for (Subscription subscription : interchange.getSubscriptionRequest().getSubscriptions()) {
			updateBinding(subscription.getSelector(), interchange.getName(), bindKey(interchange, subscription));
		}
	}

	private void unbindOldUnwantedBindings(Interchange interchange) {
		Set<String> unwantedBindKeys = getUnwantedBindKeys(interchange);
		for (String unwantedBindKey : unwantedBindKeys) {
			unbindBindKey(interchange, unwantedBindKey);
		}
	}

	private void unbindBindKey(Interchange interchange, String unwantedBindKey) {
		JSONObject json = new JSONObject();
		json.put("destination", interchange.getName());
		json.put("bindingKey", unwantedBindKey);
		String jsonString = json.toString();

		logger.info("Json string: " + jsonString);
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
		String queueId = queueId(queueName);
		restTemplate.delete(queuesURL + "?id=" + queueId);
	}
}