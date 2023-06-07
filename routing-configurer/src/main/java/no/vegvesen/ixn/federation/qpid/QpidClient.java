package no.vegvesen.ixn.federation.qpid;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
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
@ConfigurationPropertiesScan
public class QpidClient {

	public static final String FEDERATED_GROUP_NAME = "federated-interchanges";
	public static final String SERVICE_PROVIDERS_GROUP_NAME = "service-providers";
	public static final String REMOTE_SERVICE_PROVIDERS_GROUP_NAME = "remote-service-providers";
	public static final String CLIENTS_PRIVATE_CHANNELS_GROUP_NAME = "clients-private-channels";
	public final static long MAX_TTL_8_DAYS = 691_200_000L;


	private final Logger logger = LoggerFactory.getLogger(QpidClient.class);
	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s";

	private static final String ALL_QUEUES_URL_PATTERN = "%s/api/latest/queue/default/";

	private static final String ALL_EXCHANGES_URL_PATTERN = "%s/api/latest/exchange/default/";
	private static final String QUEUES_URL_PATTERN = "%s/api/latest/queue/default/%s";
	private static final String PING_URL_PATTERN = "%s/api/latest/virtualhost/default/%s";
	private static final String GROUPS_URL_PATTERN = "%s/api/latest/groupmember/default/";
	private static final String ACL_RULE_PATTERN = "%s/api/latest/virtualhostaccesscontrolprovider/default/%s/default";

	private final String exchangesURL;
	private final String queuesURL;
	private final String pingURL;
	private final String groupsUrl;
	private final RestTemplate restTemplate;
	private final String aclRulesUrl;
	private final String allQueuesUrl;
	private final String allExchangesUrl;

	public QpidClient(String baseUrl,
					  String vhostName,
					  RestTemplate restTemplate) {
		this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.groupsUrl = String.format(GROUPS_URL_PATTERN, baseUrl);
		this.aclRulesUrl = String.format(ACL_RULE_PATTERN, baseUrl, vhostName);
		this.restTemplate = restTemplate;
		this.allQueuesUrl = String.format(ALL_QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.allExchangesUrl = String.format(ALL_EXCHANGES_URL_PATTERN, baseUrl, vhostName);
	}

	/**
	 * NOTE: This wiring means that the restTemplate from QpidClientConfig#qpidRestTemplate() is used.
	 * At the time of writing, this switches off host name verification in TLS.
	 * @param restTemplate
	 * @param routingConfigurerProperties
	 */
	@Autowired
	public QpidClient(@Qualifier("qpidRestTemplate") RestTemplate restTemplate, RoutingConfigurerProperties routingConfigurerProperties) {
		this(routingConfigurerProperties.getBaseUrl(), routingConfigurerProperties.getVhost(), restTemplate);
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

	public void bindDirectExchange(String selector, String source, String destination) {
		addBinding(selector, source, destination,source);
	}

	public void bindSubscriptionExchange(String selector, String source, String destination) {
		addBinding(selector, source, destination, createBindKey(source, destination));
	}

	public String createBindKey(String source, String destination) {
		return source + "_" + destination;
	}

	public void bindTopicExchange(String selector, String source, String destination) {
		addBinding(selector,source,destination,Integer.toString(selector.hashCode()));
	}

	public void bindToBiQueue(String selector, String source) {
		addBinding(selector,source,"bi-queue",source);
	}

	private void addBinding(String selector, String source, String destination, String bindingKey) {
		JSONObject json = new JSONObject();
		json.put("destination", destination);
		json.put("bindingKey", bindingKey);
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", selector);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		postQpid(exchangesURL + "/" + source, jsonString, "/bind");
	}

	public void createQueue(String queueName) {
		if (!queueExists(queueName)) {
			_createQueue(queueName);
		}
	}

	public void createTopicExchange(String exchangeName) {
		if (!exchangeExists(exchangeName)){
			_createTopicExchange(exchangeName);
			logger.info("Created exchange with name {}", exchangeName);
		}
	}

	public void createDirectExchange(String exchangeName) {
		if (!exchangeExists(exchangeName)){
			_createDirectExchange(exchangeName);
			logger.info("Created exchange with name {}", exchangeName);
		}
	}

	void _createQueue(String queueName) {
		JSONObject json = new JSONObject();
		json.put("name", queueName);
		json.put("durable", true);
		json.put("maximumMessageTtl", MAX_TTL_8_DAYS);
		String jsonString = json.toString();
		postQpid(queuesURL, jsonString, "/");
	}
	public void _createDirectExchange(String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("name", exchangeName);
		json.put("durable", true);
		json.put("type","direct");
		String jsonString = json.toString();
		postQpid(exchangesURL, jsonString, "/");
		logger.debug("Created exchange {}", exchangeName);
	}

	public void _createTopicExchange(String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("name", exchangeName);
		json.put("durable", true);
		json.put("type","headers");
		String jsonString = json.toString();
		postQpid(exchangesURL, jsonString, "/");
		logger.debug("Created exchange {}", exchangeName);
	}

	public boolean queueExists(String queueName) {
		return lookupQueueId(queueName) != null;
	}

	private String lookupQueueId(String queueName) {
		String queueQueryUrl = queuesURL + "/" + queueName;
		logger.debug("quering for queue {} with url {}", queueName, queueQueryUrl);
		@SuppressWarnings("rawtypes")
		ResponseEntity<HashMap> response;
		try {
			response = restTemplate.getForEntity(new URI(queueQueryUrl), HashMap.class);
		} catch (HttpClientErrorException.NotFound notFound) {
			return null;
		} catch (Throwable e) {
			logger.error("Caught exception {}", e);
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

	public boolean exchangeExists(String exchangeName) {
		return lookupExchangeId(exchangeName) != null;
	}

	private String lookupExchangeId(String exchangeName) {
		String exchangeQueryUrl = exchangesURL + "/" + exchangeName;
		logger.info("quering for exchange {} with url {}", exchangeName, exchangeQueryUrl);
		@SuppressWarnings("rawtypes")
		ResponseEntity<HashMap> response;
		try {
			response = restTemplate.getForEntity(new URI(exchangeQueryUrl), HashMap.class);
		} catch (HttpClientErrorException.NotFound notFound) {
			return null;
		} catch (Throwable e) {
			logger.error("Caught exception {}", e);
			throw new RoutingConfigurerException(String.format("Could not query for QPID exchange %s", exchangeName), e);
		}
		HttpStatus statusCode = response.getStatusCode();
		if (statusCode.is2xxSuccessful()) {
			if (response.getBody() != null) {
				return (String) response.getBody().get("id");
			}
		} else {
			logger.error("Status code {} querying for QPID exchange {}", statusCode.value(), exchangeName);
		}
		return null;
	}

	public void unbindBindKey(String interchange, String unwantedBindKey, String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("destination", interchange);
		json.put("bindingKey", unwantedBindKey);
		String jsonString = json.toString();

		postQpid(exchangesURL + "/" + exchangeName, jsonString, "/unbind");
	}


	public Set<String> getQueueBindKeys(String queueName) {
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

	public void removeExchange(String exchangeName) {
		String exchangeId = lookupExchangeId(exchangeName);
		restTemplate.delete(exchangesURL + "?id=" + exchangeId);
	}

	public List<String> getGroupMemberNames(String groupName) {
		String url = groupsUrl + groupName;
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		logger.debug("Received members of group {} from Qpid: {}", groupName, response.getBody());

		JSONArray jsonArray = new JSONArray(response.getBody());
		ArrayList<String> groupMemberNames = new ArrayList<>();

		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject ob = jsonArray.getJSONObject(i);
			String userName = ob.getString("name");
			groupMemberNames.add(userName);
		}

		logger.debug("Returning list of group members: {}", groupMemberNames.toString());
		return groupMemberNames;
	}

	public void removeMemberFromGroup(String memberName, String groupName) {
		String url = groupsUrl + groupName + "/" + memberName;
		restTemplate.delete(url);
	}

	public void addMemberToGroup(String memberName, String groupName) {
		JSONObject groupJsonObject = new JSONObject();
		groupJsonObject.put("name", memberName);
		String jsonString = groupJsonObject.toString();

		postQpid(groupsUrl, jsonString, groupName);
	}

	public void addReadAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addQueueReadAccess(subscriberName,queue);
		postQpidAcl(provider);

	}

	public void addWriteAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addQueueWriteAccess(subscriberName, queue);
		postQpidAcl(provider);
	}

	public void removeReadAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.removeQueueReadAccess(subscriberName,queue);
		postQpidAcl(provider);
	}

	public void removeWriteAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.removeQueueWriteAccess(subscriberName,queue);
		postQpidAcl(provider);

	}

	public VirtualHostAccessController getQpidAcl() {
		ResponseEntity<VirtualHostAccessController> response = restTemplate.getForEntity(aclRulesUrl, VirtualHostAccessController.class);
		logger.debug("acl extractRules return code {}", response.getStatusCodeValue());
		return response.getBody();
	}

	public void postQpidAcl(VirtualHostAccessController provider) {
		ResponseEntity<String> response = restTemplate.postForEntity(aclRulesUrl, provider, String.class);
		logger.debug("Resonse code for POST to {} with is {}", aclRulesUrl,response.getStatusCodeValue());
		if (response.getStatusCode().isError()) {
			String errorMessage = String.format("Error posting to QPID REST API %s, cause: %s",
					aclRulesUrl,
					response.getStatusCode().getReasonPhrase());
			logger.error(errorMessage);
			throw new RoutingConfigurerException(errorMessage);
		}
	}

	public String getAllQueues() {
		ResponseEntity<String> allQueuesResponse = restTemplate.getForEntity(allQueuesUrl, String.class);
		return allQueuesResponse.getBody();
	}

	public String getAllExchanges() {
		ResponseEntity<String> allExchangesResponse = restTemplate.getForEntity(allExchangesUrl, String.class);
		return allExchangesResponse.getBody();
	}
}