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

	public QpidClient(String baseUrl,
					  String vhostName,
					  RestTemplate restTemplate) {
		this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.groupsUrl = String.format(GROUPS_URL_PATTERN, baseUrl);
		this.aclRulesUrl = String.format(ACL_RULE_PATTERN, baseUrl, vhostName);
		this.restTemplate = restTemplate;
	}

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

	public void addBinding(String selector, String queueName, String bindingKey, String exchangeName) {
		JSONObject json = new JSONObject();
		json.put("destination", queueName);
		json.put("bindingKey", bindingKey);
		json.put("replaceExistingArguments", true);

		JSONObject innerjson = new JSONObject();
		innerjson.put("x-filter-jms-selector", selector);

		json.put("arguments", innerjson);
		String jsonString = json.toString();

		postQpid(exchangesURL + "/" + exchangeName, jsonString, "/bind");
	}

	public void createQueue(String queueName) {
		if (!queueExists(queueName)) {
			_createQueue(queueName);
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
		List<String> aclRules = getACL();
		String newAclEntry = String.format("ACL ALLOW-LOG %s CONSUME QUEUE name = \"%s\"", subscriberName, queue);
		List<String> aclRules1 = addOneConsumeRuleBeforeLastRule(aclRules, newAclEntry);

		StringBuilder newAclRules1 = new StringBuilder();
		for (String aclRule : aclRules1) {
			newAclRules1.append(aclRule).append("\r\n");
		}
		String newAclRules = newAclRules1.toString();

		JSONObject base64EncodedAcl = new JSONObject();
		base64EncodedAcl.put("path", "data:text/plain;base64," + Base64.getEncoder().encodeToString(newAclRules.getBytes()));
		logger.debug("sending new acl to qpid {}", base64EncodedAcl.toString());
		postQpid(aclRulesUrl, base64EncodedAcl.toString(), "/loadFromFile");
		logger.info("Added read access to {} for Subscriber {}", queue, subscriberName);
	}

	public void addWriteAccess(String subscriberName, String queue) {
		List<String> aclRules = getACL();
		String newAclEntry = String.format("ACL ALLOW-LOG %s PUBLISH EXCHANGE name = \"\" routingkey = \"%s\"", subscriberName, queue);
		List<String> aclRules1 = addOneConsumeRuleBeforeLastRule(aclRules, newAclEntry);

		StringBuilder newAclRules1 = new StringBuilder();
		for (String aclRule : aclRules1) {
			newAclRules1.append(aclRule).append("\r\n");
		}
		String newAclRules = newAclRules1.toString();

		JSONObject base64EncodedAcl = new JSONObject();
		base64EncodedAcl.put("path", "data:text/plain;base64," + Base64.getEncoder().encodeToString(newAclRules.getBytes()));
		logger.debug("sending new acl to qpid {}", base64EncodedAcl.toString());
		postQpid(aclRulesUrl, base64EncodedAcl.toString(), "/loadFromFile");
		logger.info("Added write access to {} for Subscriber {}", queue, subscriberName);
	}

	List<String> addOneConsumeRuleBeforeLastRule(List<String> aclRulesLegacyFormat, String newAclEntry) {
		LinkedList<String> aclRules = new LinkedList<>(aclRulesLegacyFormat);
		aclRules.add(aclRules.size()-1, newAclEntry); // add the new rule before the last rule "DENY ALL"
		logger.debug("new acl rules {}", aclRules);
		return aclRules;
	}

	public void removeReadAccess(String subscriberName, String queue) {
		List<String> aclRules = getACL();
		String aclEntry = String.format("ACL ALLOW-LOG %s CONSUME QUEUE name = \"%s\"", subscriberName, queue);

		StringBuilder newAclRules1 = new StringBuilder();
		for (String aclRule : aclRules) {
			if(!aclRule.equals(aclEntry)) {
				newAclRules1.append(aclRule).append("\r\n");
			}
		}
		String newAclRules = newAclRules1.toString();

		JSONObject base64EncodedAcl = new JSONObject();
		base64EncodedAcl.put("path", "data:text/plain;base64," + Base64.getEncoder().encodeToString(newAclRules.getBytes()));
		logger.debug("sending new acl to qpid {}", base64EncodedAcl.toString());
		postQpid(aclRulesUrl, base64EncodedAcl.toString(), "/loadFromFile");
		logger.info("Removed read access to {} for Subscriber {}", queue, subscriberName);
	}

	public void removeWriteAccess(String subscriberName, String queue) {
		List<String> aclRules = getACL();
		//String aclEntry = String.format("ACL ALLOW-LOG %s PUBLISH EXCHANGE name = \"\" routingkey = \"%s\"", subscriberName, queue);

		StringBuilder newAclRules1 = new StringBuilder();
		for (String aclRule : aclRules) {
			if(!matchWriteAcl(aclRule, subscriberName, queue)) {
				newAclRules1.append(aclRule).append("\r\n");
			}
		}
		String newAclRules = newAclRules1.toString();

		JSONObject base64EncodedAcl = new JSONObject();
		base64EncodedAcl.put("path", "data:text/plain;base64," + Base64.getEncoder().encodeToString(newAclRules.getBytes()));
		logger.debug("sending new acl to qpid {}", base64EncodedAcl.toString());
		postQpid(aclRulesUrl, base64EncodedAcl.toString(), "/loadFromFile");
		logger.info("Removed write access to {} for Subscriber {}", queue, subscriberName);
	}

	public boolean matchWriteAcl(String aclRule, String subscriberName, String queue) {
		if(aclRule.startsWith(String.format("ACL ALLOW-LOG %s PUBLISH EXCHANGE", subscriberName)) &&
			aclRule.contains("name = \"\"") &&
			aclRule.contains(String.format("routingkey = \"%s\"", queue))){
			return true;
		}
		return false;
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