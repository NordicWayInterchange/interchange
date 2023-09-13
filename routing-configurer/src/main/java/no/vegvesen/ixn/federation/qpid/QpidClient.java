package no.vegvesen.ixn.federation.qpid;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

	public boolean addBinding(String source, Binding binding) {
		AddBindingRequest request = new AddBindingRequest(binding);
		logger.info("Add binding {} from {} ", binding, source);
		Boolean result = restTemplate.postForEntity(exchangesURL + "/" + source + "/bind", request, Boolean.class).getBody();
		return result.booleanValue();

	}


	public Queue createQueue(String name) {
		return createQueue(new CreateQueueRequest(name));
	}

	public Queue createQueue(String queueName, long maximumMessageTtl) {
		return createQueue(new CreateQueueRequest(queueName,maximumMessageTtl));
	}

	public Exchange createHeadersExchange(String name) {
		return createExchange(new CreateExchangeRequest(name,"headers"));
	}


	public Exchange createDirectExchange(String exchangeName) {
		return createExchange(new CreateExchangeRequest(exchangeName,"direct"));
	}

	private Queue createQueue(CreateQueueRequest request) {
		Queue result = restTemplate.postForEntity(queuesURL + "/", request, Queue.class).getBody();
		return result;
	}

	private Exchange createExchange(CreateExchangeRequest request) {
		ResponseEntity<Exchange> response = restTemplate.postForEntity(exchangesURL + "/", request, Exchange.class);
		return response.getBody();

	}

	public boolean queueExists(String queueName) {
		return getQueue(queueName) != null;
	}

	public Queue getQueue(String queueName) {
		try {
			return restTemplate.getForEntity(queuesURL + "/" + queueName, Queue.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public Exchange getExchange(String exchangeName) {
		try {
			return restTemplate.getForEntity(exchangesURL + "/" + exchangeName, Exchange.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}


	public boolean exchangeExists(String exchangeName) {
		return getExchange(exchangeName) != null;
	}


	public List<Binding> getQueuePublishingLinks(String queueName) {
		return restTemplate.exchange(
				queuesURL + "/" + queueName + "/getPublishingLinks",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Binding>>() {
				}).getBody();
	}

	public void removeQueue(Queue queue) {
		restTemplate.delete(queuesURL + "/" + queue.getName());
		logger.info("Removed queue {}", queue.getName());
	}


	public void removeExchange(Exchange exchange) {
		restTemplate.delete(exchangesURL + "/" + exchange.getName());
		logger.info("Removed exchange {}", exchange.getName());
	}

	public GroupMember getGroupMember(String memberName, String groupName) {
		try {
			return restTemplate.getForEntity(groupsUrl + groupName + "/" + memberName, GroupMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}


	public void removeMemberFromGroup(GroupMember member, String groupName) {
		String url = groupsUrl + groupName + "/" + member.getName();
		logger.info("Removing user {} from group {}",member.getName(),groupName);
		restTemplate.delete(url);
	}


	public GroupMember addMemberToGroup(String memberName, String groupName) {
		GroupMember groupMember = new GroupMember(memberName);
		logger.info("Adding member {} to group {}",memberName,groupName);
		return restTemplate.postForEntity(groupsUrl + groupName,groupMember,GroupMember.class).getBody();
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

	public List<Queue> getAllQueues() throws JsonProcessingException {
		ResponseEntity<List<Queue>> allQueuesResponse  = restTemplate.exchange(
				allQueuesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allQueuesResponse.getBody();
	}

	public List<Exchange> getAllExchanges() throws JsonProcessingException {
		ResponseEntity<List<Exchange>> allExchangesResponse = restTemplate.exchange(
				allExchangesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allExchangesResponse.getBody();
	}

	public QpidDelta getQpidDelta() {
		try {
			List<Queue> allQueues = getAllQueues();
			List<Exchange> allExchanges = getAllExchanges();
			return new QpidDelta(allExchanges,allQueues);

		} catch (JsonProcessingException e) {
			logger.error("Could not parse qpid delta");
			throw new RuntimeException(e);
		}
	}
}