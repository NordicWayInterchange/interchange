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

	public final static long MAX_TTL_15_MINUTES = 900_000L;


	private final Logger logger = LoggerFactory.getLogger(QpidClient.class);
	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s";

	private static final String ALL_QUEUES_URL_PATTERN = "%s/api/latest/queue/default/";

	private static final String ALL_EXCHANGES_URL_PATTERN = "%s/api/latest/exchange/default/";
	public static final String QUEUES_URL_PATTERN = "%s/api/latest/queue/default/%s";
	private static final String PING_URL_PATTERN = "%s/api/latest/virtualhost/default/%s";
	private static final String GROUPS_URL_PATTERN = "%s/api/latest/groupmember/default/";
	private static final String ACL_RULE_PATTERN = "%s/api/latest/virtualhostaccesscontrolprovider/default/%s/default";

	private static final String CONNECTION_URL_PATTERN = "%s/api/latest/connection";

	private static final String QUERY_ENGINE_API_PATTERN = "%s/api/latest/querybroker/broker";

	//TODO this might be more configurable, since the 'connection' part is a configured object
	private static final String QUERY_API_PATTERN = "%s/api/latest/querybroker";

	private final String exchangesURL;
	private final String queuesURL;
	private final String pingURL;
	private final String groupsUrl;
	private final RestTemplate restTemplate;
	private final String aclRulesUrl;
	private final String allQueuesUrl;
	private final String allExchangesUrl;

	private final String queryEngineApiUrl;

	private final String connectionUrl;
	private final String queryApiUrl;
	private final String messageCollectorUser;


	public QpidClient(String baseUrl,
					  String vhostName,
					  RestTemplate restTemplate,
					  String messageCollectorUser) {
        this.messageCollectorUser = messageCollectorUser;
        this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.groupsUrl = String.format(GROUPS_URL_PATTERN, baseUrl);
		this.aclRulesUrl = String.format(ACL_RULE_PATTERN, baseUrl, vhostName);
		this.restTemplate = restTemplate;
		this.allQueuesUrl = String.format(ALL_QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.allExchangesUrl = String.format(ALL_EXCHANGES_URL_PATTERN, baseUrl, vhostName);
		this.queryEngineApiUrl = String.format(QUERY_ENGINE_API_PATTERN,baseUrl);
		this.connectionUrl = String.format(CONNECTION_URL_PATTERN,baseUrl);
		this.queryApiUrl = String.format(QUERY_API_PATTERN,baseUrl);
	}

	/**
	 * NOTE: This wiring means that the restTemplate from QpidClientConfig#qpidRestTemplate() is used.
	 * At the time of writing, this switches off host name verification in TLS.
	 * @param restTemplate
	 * @param routingConfigurerProperties
	 */
	@Autowired
	public QpidClient(@Qualifier("qpidRestTemplate") RestTemplate restTemplate, RoutingConfigurerProperties routingConfigurerProperties) {
		this(routingConfigurerProperties.getBaseUrl(), routingConfigurerProperties.getVhost(), restTemplate, routingConfigurerProperties.getCollectorUser());
	}

	int ping() {
		ResponseEntity<String> response = restTemplate.getForEntity(pingURL, String.class);
		logger.debug(response.getBody());
		return response.getStatusCodeValue();
	}

	public boolean addBinding(String source, Binding binding) {
		AddBindingRequest request = new AddBindingRequest(binding);
		logger.info("Add binding {} from {} ", binding, source);
		String url = exchangesURL + "/" + source + "/bind";
		logger.debug("POSTint {} to URL {}",request,url);
		Boolean result = restTemplate.postForEntity(url, request, Boolean.class).getBody();
		return result.booleanValue();

	}


	public Queue createQueue(String name) {
		return createQueue(new CreateQueueRequest(name, MAX_TTL_15_MINUTES));
	}

	public Exchange createHeadersExchange(String name) {
		return createExchange(new CreateExchangeRequest(name,"headers"));
	}


	public Exchange createDirectExchange(String exchangeName) {
		return createExchange(new CreateExchangeRequest(exchangeName,"direct"));
	}

	private Queue createQueue(CreateQueueRequest request) {
		logger.info("Create queue {}", request.getName());
		String url = queuesURL + "/";
		logger.debug("POSTin {} to {}", request,url);
		Queue result = restTemplate.postForEntity(url, request, Queue.class).getBody();
		return result;
	}

	private Exchange createExchange(CreateExchangeRequest request) {
		logger.info("Create exchange {} of type {}", request.getName(), request.getType());
		String url = exchangesURL + "/";
		logger.debug("POSTing {} to {}",request,url);
		ResponseEntity<Exchange> response = restTemplate.postForEntity(url, request, Exchange.class);
		return response.getBody();

	}

	public boolean queueExists(String queueName) {
		boolean exists = getQueue(queueName) != null;
		logger.debug("Queue '{}' exist? {}", queueName,exists);
		return exists;
	}

	public Queue getQueue(String queueName) {
		try {
			String url = queuesURL + "/" + queueName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, Queue.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public Exchange getExchange(String exchangeName) {
		try {
			String url = exchangesURL + "/" + exchangeName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, Exchange.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}


	public boolean exchangeExists(String exchangeName) {
		boolean exist = getExchange(exchangeName) != null;
		logger.debug("Exchange '{}' exist? {}", exchangeName,exist);
		return exist;
	}


	public List<Binding> getQueuePublishingLinks(String queueName) {
		String url = queuesURL + "/" + queueName + "/getPublishingLinks";
		logger.debug("GETting from {}", url);
		return restTemplate.exchange(
				url,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<Binding>>() {
				}).getBody();
	}

	public void removeQueue(Queue queue) {
		String url = queuesURL + "/" + queue.getName();
		logger.debug("DELETE to URL {}",url );
		restTemplate.delete(url);
		logger.info("Removed queue {}", queue.getName());
	}


	public void removeExchange(Exchange exchange) {
		String url = exchangesURL + "/" + exchange.getName();
		logger.debug("DELETE to URL {}",url);
		restTemplate.delete(url);
		logger.info("Removed exchange {}", exchange.getName());
	}

	public GroupMember getGroupMember(String memberName, String groupName) {
		logger.debug("Getting member named '{}' from group '{}'",memberName,groupName);
		for (GroupMember groupMember : getMembersInGroup(groupName)) {
			if (groupMember.getName().equals(memberName)) {
				return groupMember;
			}
		}
		return null;
	}


	public List<GroupMember> getMembersInGroup(String groupName) {
		logger.debug("Getting members from group '{}'",groupName);
		return restTemplate.exchange(
				groupsUrl + "/" + groupName,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<GroupMember>>() {
				}).getBody();
	}


	public void removeMemberFromGroup(GroupMember member, String groupName) {
		String url = groupsUrl + groupName + "/" + member.getName();
		logger.debug("DELETE to URL {}",url);
		logger.info("Removing user {} from group {}",member.getName(),groupName);
		restTemplate.delete(url);
	}


	//TODO complete the debug logging
	public GroupMember addMemberToGroup(String memberName, String groupName) {
		GroupMember groupMember = new GroupMember(memberName);
		logger.info("Adding member {} to group {}",memberName,groupName);
		String url = groupsUrl + groupName;
		return restTemplate.postForEntity(url,groupMember,GroupMember.class).getBody();
	}

	public void addReadAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addQueueReadAccess(subscriberName, queue);
		logger.info("Adding read access for {} to queue {}",subscriberName,queue);
        postQpidAcl(provider);
	}

	public void addWriteAccess(String subscriberName, String queue) {
		VirtualHostAccessController provider = getQpidAcl();
		provider.addExchangeWriteAccess(subscriberName, queue);
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

	public void addMessageCollectorWriteAccess(String exchangeName) {
		addWriteAccess(messageCollectorUser,exchangeName);
	}

	public String getMessageCollectorUser() {
		return messageCollectorUser;
	}

	public VirtualHostAccessController getQpidAcl() {
		ResponseEntity<VirtualHostAccessController> response = restTemplate.getForEntity(aclRulesUrl, VirtualHostAccessController.class);
		logger.debug("acl extractRules return code {}", response.getStatusCodeValue());
		return response.getBody();
	}

	public void postQpidAcl(VirtualHostAccessController provider) {
		logger.info("Posting updated ACL");
		restTemplate.postForEntity(aclRulesUrl, provider, String.class);
	}


	public ConnectionQueryResult executeConnectionQuery(String select, String where, String orderBy, String domain) {
		return restTemplate.getForEntity(queryApiUrl  +"/" + domain + "?select={query}&where={where}&orderBy={orderBy}",ConnectionQueryResult.class,select,where,orderBy).getBody();
	}

	public ConnectionQueryResult executeConnectionQuery(String select, String where, String domain) {
		return restTemplate.getForEntity(queryApiUrl  +"/" + domain + "?select={query}&where={where}",ConnectionQueryResult.class,select,where).getBody();
	}

	public ConnectionQueryResult executeConnectionQuery(String select, String domain) {
		return restTemplate.getForEntity(queryApiUrl  +"/" + domain + "?select={query}",ConnectionQueryResult.class,select).getBody();
	}


	public QueryResult executeQuery(Query query) {
		return restTemplate.postForEntity(queryEngineApiUrl,query,QueryResult.class).getBody();
	}


	public String getConnection(String port, String connectionName) {
		return restTemplate.getForEntity(connectionUrl + "/" + port + "/" + connectionName,String.class).getBody();
	}

	public void deleteConnection(String connectionName) {
		logger.info("Deleting connection {}", connectionName);
		restTemplate.delete(connectionUrl + "/AMQPS/" + connectionName);
	}

	public List<Queue> getAllQueues() throws JsonProcessingException {
		logger.debug("Getting all queues");
		ResponseEntity<List<Queue>> allQueuesResponse  = restTemplate.exchange(
				allQueuesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allQueuesResponse.getBody();
	}

	public List<Exchange> getAllExchanges() throws JsonProcessingException {
		logger.debug("Getting all exchanges");
		ResponseEntity<List<Exchange>> allExchangesResponse = restTemplate.exchange(
				allExchangesUrl,
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		return allExchangesResponse.getBody();
	}

	public QpidDelta getQpidDelta() {
		logger.debug("Getting qpidDelta");
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
