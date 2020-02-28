package no.vegvesen.ixn.federation.qpid;

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import org.json.JSONArray;
import org.json.JSONObject;
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

	public static final String FEDERATED_GROUP_NAME = "federated-interchanges";
	public static final String SERVICE_PROVIDERS_GROUP_NAME = "service-providers";

	private final Logger logger = LoggerFactory.getLogger(QpidClient.class);
	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s";
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

	@Autowired
	public QpidClient(@Value("${qpid.rest.api.baseUrl}") String baseUrl,
					  @Value("${qpid.rest.api.vhost}") String vhostName,
					  RestTemplate restTemplate) {
		this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.groupsUrl = String.format(GROUPS_URL_PATTERN, baseUrl);
		this.aclRulesUrl = String.format(ACL_RULE_PATTERN, baseUrl, vhostName);
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

	private void updateBinding(String binding, String queueName, String bindingKey, String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", bindingKey);
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", binding);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		postQpid(exchangesURL + "/" + exchangeName, jsonString, "/bind");
	}

	void createQueue(Subscriber subscriber) {
		JSONObject json = new JSONObject();
		json.put("name", subscriber.getName());
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

	public SubscriptionRequest setupRouting(Subscriber toSetUp, String exchangeName) {
		if (queueExists(toSetUp.getName())) {
			unbindOldUnwantedBindings(toSetUp, exchangeName);
		} else {
			createQueue(toSetUp);
		}
		SubscriptionRequest subscriptionRequest = toSetUp.getSubscriptionRequest();
		for (Subscription subscription : subscriptionRequest.getSubscriptions()) {
			updateBinding(subscription.getSelector(), toSetUp.getName(), bindKey(toSetUp, subscription), exchangeName);
			subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
		}
		if (toSetUp instanceof ServiceProvider) {
			addReadAccess(toSetUp, toSetUp.getName());
		}
		subscriptionRequest.setStatus(SubscriptionRequestStatus.ESTABLISHED);
		return subscriptionRequest;
	}

	private void unbindOldUnwantedBindings(Subscriber interchange, String exchangeName) {
		Set<String> unwantedBindKeys = getUnwantedBindKeys(interchange);
		for (String unwantedBindKey : unwantedBindKeys) {
			unbindBindKey(interchange, unwantedBindKey, exchangeName);
		}
	}

	private void unbindBindKey(Subscriber interchange, String unwantedBindKey, String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("destination", interchange.getName());
		json.put("bindingKey", unwantedBindKey);
		String jsonString = json.toString();

		postQpid(exchangesURL + "/" + exchangeName, jsonString, "/unbind");
	}

	private Set<String> getUnwantedBindKeys(Subscriber interchange) {
		Set<String> existingBindKeys = getQueueBindKeys(interchange.getName());
		Set<String> wantedBindKeys = wantedBindings(interchange);
		Set<String> unwantedBindKeys = new HashSet<>(existingBindKeys);
		unwantedBindKeys.removeAll(wantedBindKeys);
		return unwantedBindKeys;
	}

	private Set<String> wantedBindings(Subscriber interchange) {
		Set<String> wantedBindings = new HashSet<>();
		for (Subscription subscription : interchange.getSubscriptionRequest().getSubscriptions()) {
			wantedBindings.add(bindKey(interchange, subscription));
		}
		return wantedBindings;
	}

	private String bindKey(Subscriber interchange, Subscription subscription) {
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

	public List<String> getInterchangesUserNames(String groupName) {
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

	public void removeInterchangeUserFromGroups(String groupName, String neighbourName) {
		String url = groupsUrl + groupName + "/" + neighbourName;
		restTemplate.delete(url);
	}

	public void addInterchangeUserToGroups(String name, String groupName) {
		JSONObject groupJsonObject = new JSONObject();
		groupJsonObject.put("name", name);
		String jsonString = groupJsonObject.toString();

		postQpid(groupsUrl, jsonString, groupName);
	}

	void addReadAccess(Subscriber subscriber, String queue) {
		List<String> aclRules = getACL();
		List<String> aclRules1 = addOneConsumeRuleBeforeLastRule(subscriber, queue, aclRules);

		StringBuilder newAclRules1 = new StringBuilder();
		for (String aclRule : aclRules1) {
			newAclRules1.append(aclRule).append("\r\n");
		}
		String newAclRules = newAclRules1.toString();

		JSONObject base64EncodedAcl = new JSONObject();
		base64EncodedAcl.put("path", "data:text/plain;base64," + Base64.getEncoder().encodeToString(newAclRules.getBytes()));
		logger.debug("sending new acl to qpid {}", base64EncodedAcl.toString());
		postQpid(aclRulesUrl, base64EncodedAcl.toString(), "/loadFromFile");
		logger.info("Added read access to {} for Subscriber {}", queue, subscriber.getName());
	}

	List<String> addOneConsumeRuleBeforeLastRule(Subscriber subscriber, String newConsumeQueue, List<String> aclRulesLegacyFormat) {
		LinkedList<String> aclRules = new LinkedList<>(aclRulesLegacyFormat);
		String newAclEntry = String.format("ACL ALLOW-LOG %s CONSUME QUEUE name = \"%s\"", subscriber.getName(), newConsumeQueue);
		aclRules.add(aclRules.size()-1, newAclEntry); // add the new rule before the last rule "DENY ALL"
		logger.debug("new acl rules {}", aclRules);
		return aclRules;
	}

	List<String> getACL() {
		ResponseEntity<String> aclRulesResponse = restTemplate.getForEntity(aclRulesUrl + "/extractRules", String.class);
		String aclRulesS = aclRulesResponse.getBody();
		logger.debug("acl extractRules return code {}, body {}", aclRulesResponse.getStatusCodeValue(), aclRulesS);
		if (aclRulesS == null) {
			return new LinkedList<>();
		}
		return Arrays.asList(aclRulesS.split("\\r?\\n"));
	}
}