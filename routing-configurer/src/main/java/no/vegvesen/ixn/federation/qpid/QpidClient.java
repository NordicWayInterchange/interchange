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
import org.springframework.web.client.RestTemplate;

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

	int ping(){
		ResponseEntity<String> response = restTemplate.getForEntity(pingURL, String.class);
		logger.debug(response.getBody());
		return response.getStatusCodeValue();
	}

	// A method that posts a json object to the Qpid REST api, using a given URI and a given command.
	private void callQpid(String urlString, String message, String command) {

		String url = urlString + command;
		logger.info("URL: " + url);
		ResponseEntity<String> response = restTemplate.postForEntity(url, message, String.class);
		if (response.getStatusCode().isError()) {
			//TODO: introduce our own runtime exception
			throw new RuntimeException(String.format("could not post to %s with payload %s", url, message));
		}
		logger.debug("Resonse code for POST to {} with payload {} is {}", url, message, response.getStatusCodeValue());
	}

	// Updates the binding of a queue to a new binding.
	public void updateBinding(String binding, String queueName) throws Exception {
		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", "where");
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", binding);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		logger.info("Json string: " + jsonString);
		callQpid(exchangeURL, jsonString, "/bind");
	}// Creates a new queue for a neighbouring Interchange.

	public void createQueue(Interchange interchange) throws Exception {
		JSONObject json = new JSONObject();
		json.put("name", interchange.getName());
		json.put("durable", true);

		String jsonString = json.toString();
		logger.info("Creating queue:" + jsonString);

		callQpid(queueURL, jsonString, "/");

		logger.info("Creating binding for queue..");
		String binding = createBinding(interchange.getSubscriptionRequest().getSubscriptions());
		updateBinding(binding, interchange.getName());
	}// Creates a binding based on the subscriptions of a neighbouring Interchange.

	String createBinding(Set<Subscription> subscriptions) {
		StringBuilder binding = new StringBuilder();

		Iterator<Subscription> subscriptionIterator = subscriptions.iterator();
		while (subscriptionIterator.hasNext()) {
			Subscription subscription =  subscriptionIterator.next();
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

}