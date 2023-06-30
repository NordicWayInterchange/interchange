package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.Collectors;

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

	public void addBinding(String selector, String source, String destination, String bindingKey) {
		AddBindingRequest request = new AddBindingRequest(destination,bindingKey,selector);
		logger.info("Add binding from {} to {} with binding key {} and selector {}", source,destination,bindingKey,selector);
		restTemplate.postForEntity(exchangesURL + "/" + source + "/bind",request,String.class);

	}

	public void createQueue(String queueName) {
		if (!queueExists(queueName)) {
			logger.info("Creating queue {}", queueName);
			_createQueue(queueName);
		}
	}

	public void createTopicExchange(String exchangeName) {
		if (!exchangeExists(exchangeName)){
			logger.info("Creating topic exchange {}", exchangeName);
			_createTopicExchange(exchangeName);
		}
	}

	public void createDirectExchange(String exchangeName) {
		if (!exchangeExists(exchangeName)){
			logger.info("Creating direct exchange {}", exchangeName);
			_createDirectExchange(exchangeName);
		}
	}

	void _createQueue(String queueName) {
		CreateQueueRequest request = new CreateQueueRequest(queueName,MAX_TTL_8_DAYS);
		restTemplate.postForEntity(queuesURL + "/",request,String.class);
	}
	public void _createDirectExchange(String exchangeName) {
		CreateExchangeRequest request = new CreateExchangeRequest(exchangeName,"direct");
		restTemplate.postForEntity(exchangesURL + "/",request, String.class);
		logger.debug("Created exchange {}", exchangeName);
	}

	public void _createTopicExchange(String exchangeName) {
		CreateExchangeRequest request = new CreateExchangeRequest(exchangeName,"headers");
		restTemplate.postForEntity(exchangesURL + "/",request,String.class);
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


	public Set<String> getQueueBindKeys(String queueName) {
		HashSet<String> existingBindKeys = new HashSet<>();
		String url = queuesURL + "/" + queueName + "/getPublishingLinks";
		System.out.println("Get publishing links for Queue on URL " + url);

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
		logger.info("Removing queue {}", queueName);
		restTemplate.delete(queuesURL + "?id=" + queueId);
	}

	public void removeExchange(String exchangeName) {
		String exchangeId = lookupExchangeId(exchangeName);
		logger.info("Removing exchange {}", exchangeName);
		restTemplate.delete(exchangesURL + "?id=" + exchangeId);
	}

	public List<String> getGroupMemberNames(String groupName) {
		String url = groupsUrl + groupName;
		ResponseEntity<List<GroupMember>> groupMembers = restTemplate.exchange(
				url,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		List<String> groupMemberNames = groupMembers.getBody().stream().map(GroupMember::getName).collect(Collectors.toList());
		return groupMemberNames;
	}

	public void removeMemberFromGroup(String memberName, String groupName) {
		String url = groupsUrl + groupName + "/" + memberName;
		logger.info("Removing user {} from group {}",memberName,groupName);
		restTemplate.delete(url);
	}

	public void addMemberToGroup(String memberName, String groupName) {
		GroupMember groupMember = new GroupMember(memberName);
		logger.info("Adding member {} to group {}",memberName,groupName);
		restTemplate.postForEntity(groupsUrl + groupName,groupMember,String.class);
	}

	public void addReadAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addQueueReadAccess(subscriberName, queue);
		logger.info("Adding read access for {} to queue {}",subscriberName,queue);
        postQpidAcl(provider);
	}

	public void addWriteAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addQueueWriteAccess(subscriberName, queue);
		logger.info("Adding write access for {} to queue {}",subscriberName,queue);
        postQpidAcl(provider);
	}

	public void removeReadAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.removeQueueReadAccess(subscriberName,queue);
		logger.info("Removing read access for {} to queue {}", subscriberName, queue);
		postQpidAcl(provider);
	}

	public void removeWriteAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.removeQueueWriteAccess(subscriberName,queue);
		logger.info("Removing write access for {} to queue {}", subscriberName,queue);
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

	public Set<Queue> getAllQueues() throws JsonProcessingException {
		ResponseEntity<Set<Queue>> allQueuesResponse  = restTemplate.exchange(
				allQueuesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allQueuesResponse.getBody();
	}

	public Set<Exchange> getAllExchanges() throws JsonProcessingException {
		ResponseEntity<Set<Exchange>> allExchangesResponse = restTemplate.exchange(
				allExchangesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allExchangesResponse.getBody();
	}

	public QpidDelta getQpidDelta() {
		QpidDelta qpidDelta = new QpidDelta();
		try {
			Set<Queue> allQueues = getAllQueues();
			Set<Exchange> allExchanges = getAllExchanges();
			qpidDelta.setExchanges(allExchanges);
			qpidDelta.setQueues(allQueues);
		} catch (JsonProcessingException e) {
			logger.error("Could not parse qpid delta");
			throw new RuntimeException(e);
		}
		return qpidDelta;
	}
}