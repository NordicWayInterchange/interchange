package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

@Service
public class QpidClient {
	private Logger logger = LoggerFactory.getLogger(QpidClient.class);
	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s/nwEx";
	private static final String QUEUE_URL_PATTERN = "%s/api/latest/queue/default/%s";
	private static final String PING_URL_PATTERN = "%s/api/latest/virtualhost/default/%s";

	private final String exchangeURL;
	private final String queueURL;
	private final String pingURL;

	private final RestTemplate restTemplate;

	@Autowired
	public QpidClient(@Value("${qpid.rest.api.baseUrl}") String baseUrl,
					  @Value("${qpid.rest.api.vhost}") String vhostName,
					  RestTemplate restTemplate) {
		this.exchangeURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queueURL = String.format(QUEUE_URL_PATTERN, baseUrl, vhostName);
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
	private void updateBinding(String binding, String queueName) {
		try {
			JSONObject json = new JSONObject();
			json.put("destination", queueName);
			json.put("bindingKey", "where1");
			json.put("replaceExistingArguments", true);

			JSONObject innerjson = new JSONObject();
			innerjson.put("x-filter-jms-selector", binding);

			json.put("arguments", innerjson);
			String jsonString = json.toString();

			logger.info("Json string: " + jsonString);
			postQpid(exchangeURL, jsonString, "/bind");
		} catch (JSONException e) {
			throw new RoutingConfigurerException(e);
		}
	}// Creates a new queue for a neighbouring Interchange.

	public void createQueue(Interchange interchange) {
		try {
			JSONObject json = new JSONObject();
			json.put("name", interchange.getName());
			json.put("durable", true);

			String jsonString = json.toString();
			logger.info("Creating queue:" + jsonString);

			postQpid(queueURL, jsonString, "/");

		} catch (JSONException e) {
			throw new RoutingConfigurerException(e);
		}
	}

	/**
	 * Creates a binding based on the subscriptions of a neighbouring Interchange.
	 */
	String createBinding(Set<Subscription> subscriptions) {
		StringBuilder binding = new StringBuilder();

		Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
		while (subscriptionIterator.hasNext()) {
			Subscription subscription = subscriptionIterator.next();
			binding.append("(");
			binding.append(subscription.getSelector());
			binding.append(")");
			if (subscriptionIterator.hasNext()) {
				binding.append(" OR ");
			}
		}
		logger.info("Queue binding:" + binding);
		return binding.toString();
	}

	public boolean queueExists(String queueName) {
		String queueQueryUrl = queueURL + "/" + queueName;
		logger.info("quering for queue {} with url {}", queueName, queueQueryUrl);
		ResponseEntity<Object> response;
		try {
			response = restTemplate.getForEntity(new URI(queueQueryUrl), Object.class);
		} catch (HttpClientErrorException.NotFound notFound) {
			return false;
		} catch (URISyntaxException e) {
			throw new RoutingConfigurerException(e);
		}
		return response.getStatusCode().is2xxSuccessful();
	}
}