package no.vegvesen.ixn.federation.qpid;

import tools.jackson.core.JsonProcessingException;
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

import java.util.Arrays;
import java.util.List;

@Service
@ConfigurationPropertiesScan
public class QpidClient {

	private static final String FEDERATED_GROUP_NAME = "federated-interchanges";

	private static final String SERVICE_PROVIDERS_GROUP_NAME = "service-providers";

	private static final String REMOTE_SERVICE_PROVIDERS_GROUP_NAME = "remote-service-providers";

	private static final String CLIENTS_PRIVATE_CHANNELS_GROUP_NAME = "clients-private-channels";

	private static final String BI_CONSUMERS_GROUP_NAME = "bi-consumers";

	public final static long MAX_TTL_15_MINUTES = 900_000L;

	private final Logger logger = LoggerFactory.getLogger(QpidClient.class);

	private static final String EXCHANGE_URL_PATTERN = "%s/api/latest/exchange/default/%s";

	//TODO find out what the difference between this and the queues URL is
	private static final String ALL_QUEUES_URL_PATTERN = "%s/api/latest/queue/default/";

	private static final String ALL_EXCHANGES_URL_PATTERN = "%s/api/latest/exchange/default/";

	public static final String QUEUES_URL_PATTERN = "%s/api/latest/queue/default/%s";

	private static final String PING_URL_PATTERN = "%s/api/latest/virtualhost/default/%s";

	private static final String GROUP_MEMBER_URL_PATTERN = "%s/api/latest/groupmember/default/";

	private static final String ACL_RULE_PATTERN = "%s/api/latest/virtualhostaccesscontrolprovider/default/%s/default";

	private static final String CONNECTION_URL_PATTERN = "%s/api/latest/connection";

	private static final String QUERY_ENGINE_API_PATTERN = "%s/api/latest/querybroker/broker";

	//TODO this might be more configurable, since the 'connection' part is a configured object
	private static final String QUERY_API_PATTERN = "%s/api/latest/querybroker";

	private final String exchangesURL;
	private final String queuesURL;
	private final String pingURL;
	private final String groupMembersURL;
	private final RestTemplate restTemplate;
	private final String aclRulesUrl;
	private final String allQueuesUrl;
	private final String allExchangesUrl;
	private final String queryEngineApiUrl;
	private final String connectionUrl;
	private final String queryApiUrl;


	public QpidClient(String baseUrl,
					  String vhostName,
					  RestTemplate restTemplate) {
		this.exchangesURL = String.format(EXCHANGE_URL_PATTERN, baseUrl, vhostName);
		this.queuesURL = String.format(QUEUES_URL_PATTERN, baseUrl, vhostName);
		this.pingURL = String.format(PING_URL_PATTERN, baseUrl, vhostName);
		this.groupMembersURL = String.format(GROUP_MEMBER_URL_PATTERN, baseUrl);
		this.aclRulesUrl = String.format(ACL_RULE_PATTERN, baseUrl, vhostName);
		this.allQueuesUrl = String.format(ALL_QUEUES_URL_PATTERN, baseUrl);
		this.allExchangesUrl = String.format(ALL_EXCHANGES_URL_PATTERN, baseUrl);
		this.queryEngineApiUrl = String.format(QUERY_ENGINE_API_PATTERN,baseUrl);
		this.connectionUrl = String.format(CONNECTION_URL_PATTERN,baseUrl);
		this.queryApiUrl = String.format(QUERY_API_PATTERN,baseUrl);
		this.restTemplate = restTemplate;
	}

	/**
	 * NOTE: This wiring means that the restTemplate from QpidClientConfig#qpidRestTemplate() is used.
	 * At the time of writing, this switches off host name verification in TLS.
	 * @param restTemplate the restTemplate used to contact the broker
	 * @param routingConfigurerProperties properties for the client to use (baseUrl, vhost)
	 */

	@Autowired
	public QpidClient(@Qualifier("qpidRestTemplate") RestTemplate restTemplate, RoutingConfigurerProperties routingConfigurerProperties) {
		this(routingConfigurerProperties.getBaseUrl(), routingConfigurerProperties.getVhost(), restTemplate);
	}

	int ping() {
		ResponseEntity<String> response = restTemplate.getForEntity(pingURL, String.class);
		logger.debug(response.getBody());
		return response.getStatusCode().value();
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

	public Queue createNonDestructiveQueue(String name) {
		return createQueue(new CreateQueueRequest(name, MAX_TTL_15_MINUTES, true));
	}

	public Exchange createHeadersExchange(String name) {
		return createExchange(new CreateExchangeRequest(name,"headers"));
	}

	public Exchange createHeadersExchangeWithDlq(String name, String dlqName) {
		return createExchange(new CreateExchangeRequest(name,"headers",new AlternateBinding(dlqName)));
	}

	private Queue createQueue(CreateQueueRequest request) {
		logger.info("Create queue {}", request.getName());
		String url = queuesURL + "/";
		logger.debug("POSTin {} to {}", request,url);
        return restTemplate.postForEntity(url, request, Queue.class).getBody();
	}

	private Exchange createExchange(CreateExchangeRequest request) {
		logger.info("Create exchange {} of type {}", request.getName(), request.getType());
		String url = exchangesURL + "/";
		logger.debug("POSTing {} to {}",request,url);
		ResponseEntity<Exchange> response = restTemplate.postForEntity(url, request, Exchange.class);
		return response.getBody();
	}

	public boolean queueExists(String queueName) {
		return getQueue(queueName) != null;
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
		return getExchange(exchangeName) != null;
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

	public ServiceProviderMember getServiceProviderMember(String memberName) {
		try {
			String url = groupMembersURL + SERVICE_PROVIDERS_GROUP_NAME + "/" + memberName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, ServiceProviderMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public List<ServiceProviderMember> getServiceProviderMembers() {
		String url = groupMembersURL + SERVICE_PROVIDERS_GROUP_NAME;
		logger.debug("GETting from {}", url);
		ResponseEntity<ServiceProviderMember[]> response = restTemplate.getForEntity(url, ServiceProviderMember[].class);
		return Arrays.asList(response.getBody());
	}

	public ServiceProviderMember addServiceProviderMemberToGroup(String memberName) {
		ServiceProviderMember member = new ServiceProviderMember(memberName);
		logger.info("Adding service provider member '{}' to group", memberName);
		String url = groupMembersURL + SERVICE_PROVIDERS_GROUP_NAME;
		logger.debug("POSTin to {}", url);
		return restTemplate.postForEntity(url, member, ServiceProviderMember.class).getBody();
	}

	public void removeServiceProviderMemberFromGroup(ServiceProviderMember member) {
		String url = groupMembersURL + SERVICE_PROVIDERS_GROUP_NAME + "/" + member.getName();
		logger.debug("DELETE to URL {}",url);
		logger.info("Removing service provider member '{}' from group", member.getName());
		restTemplate.delete(url);
	}

	public PrivateChannelMember getPrivateChannelGroupMember(String memberName) {
		try {
			String url = groupMembersURL + "/" + CLIENTS_PRIVATE_CHANNELS_GROUP_NAME + "/" + memberName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, PrivateChannelMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public List<PrivateChannelMember> getPrivateChannelGroupMembers() {
			String url = groupMembersURL + CLIENTS_PRIVATE_CHANNELS_GROUP_NAME;
			logger.debug("Getting from URL {}", url);
			ResponseEntity<PrivateChannelMember[]> response = restTemplate.getForEntity(url, PrivateChannelMember[].class);
			return Arrays.asList(response.getBody());
	}

	public PrivateChannelMember addPrivateChannelMemberToGroup(String memberName) {
		PrivateChannelMember privateChannelMember = new PrivateChannelMember(memberName);
		logger.info("Adding private channel member '{}' to group",memberName);
		String url = groupMembersURL + CLIENTS_PRIVATE_CHANNELS_GROUP_NAME;
		return restTemplate.postForEntity(url,privateChannelMember,PrivateChannelMember.class).getBody();
	}

	public void removePrivateChannelMemberFromGroup(PrivateChannelMember member) {
		String url = groupMembersURL + CLIENTS_PRIVATE_CHANNELS_GROUP_NAME + "/" + member.name();
		logger.debug("DELETE to URL {}",url);
		logger.info("Removing private channel member '{}' from group", member.name());
		restTemplate.delete(url);
	}

	public BiConsumerMember getBiConsumerMember(String memberName) {
		try {
			String url = groupMembersURL + "/" + BI_CONSUMERS_GROUP_NAME + "/" + memberName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, BiConsumerMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public List<BiConsumerMember> getBiConsumerMembers() {
		String url = groupMembersURL + BI_CONSUMERS_GROUP_NAME;
		logger.debug("Getting from URL {}", url);
		ResponseEntity<BiConsumerMember[]> response = restTemplate.getForEntity(url, BiConsumerMember[].class);
		return Arrays.asList(response.getBody());
	}

	public BiConsumerMember addBiConsumerMemberToGroup(String memberName) {
		BiConsumerMember biConsumerMember = new BiConsumerMember(memberName);
		logger.info("Adding bi consumer member '{}' to group",memberName);
		String url = groupMembersURL + BI_CONSUMERS_GROUP_NAME;
		return restTemplate.postForEntity(url,biConsumerMember, BiConsumerMember.class).getBody();
	}

	public void removeBiConsumerMemberFromGroup(BiConsumerMember member) {
		String url = groupMembersURL + BI_CONSUMERS_GROUP_NAME + "/" + member.name();
		logger.debug("DELETE to URL {}",url);
		logger.info("Removing bi consumer member '{}' from group", member.name());
		restTemplate.delete(url);
	}

	public RemoteServiceProviderMember getRemoteServiceProviderMember(String memberName) {
		try {
			String url = groupMembersURL + "/" + REMOTE_SERVICE_PROVIDERS_GROUP_NAME + "/" + memberName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, RemoteServiceProviderMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public RemoteServiceProviderMember addRemoteServiceProvicerMemberToGroup(String memberName) {
		RemoteServiceProviderMember member = new RemoteServiceProviderMember(memberName);
		logger.info("Adding remote service provider member '{}' to group",memberName);
		String url = groupMembersURL + REMOTE_SERVICE_PROVIDERS_GROUP_NAME;
		return restTemplate.postForEntity(url,member,RemoteServiceProviderMember.class).getBody();
	}

	public void removeRemoteServiceProviderMemberFromGroup(RemoteServiceProviderMember member) {
		String url = groupMembersURL + REMOTE_SERVICE_PROVIDERS_GROUP_NAME + "/" + member.getName();
		logger.info("Removing remote service provider member '{}' from group",member.getName());
		restTemplate.delete(url);
	}

	public NeighbourMember getNeighbourMember(String memberName) {
		try {
			String url = groupMembersURL + FEDERATED_GROUP_NAME + "/" + memberName;
			logger.debug("GETting from {}", url);
			return restTemplate.getForEntity(url, NeighbourMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public void removeNeighbourMemberFromGroup(NeighbourMember member) {
		String url = groupMembersURL + FEDERATED_GROUP_NAME + "/" + member.name();
		logger.debug("DELETE to URL {}",url);
		logger.info("Removing neighbour member '{}' from group",member.name());
		restTemplate.delete(url);
	}

	public NeighbourMember addNeighbourMemberToGroup(String memberName) {
		NeighbourMember member = new NeighbourMember(memberName);
		logger.info("Adding neighbour member '{}' to group",memberName);
		String url = groupMembersURL + FEDERATED_GROUP_NAME;
		return restTemplate.postForEntity(url,member,NeighbourMember.class).getBody();
	}

	public GroupMember getGroupMember(String memberName, String groupName) {
		try {
			String url = groupMembersURL + groupName + "/" + memberName;
			logger.debug("GETting from URL {}", url);
			return restTemplate.getForEntity(url, GroupMember.class).getBody();
		} catch (HttpClientErrorException.NotFound e) {
			return null;
		}
	}

	public GroupMember addMemberToGroup(String memberName, String groupName) {
		GroupMember groupMember = new GroupMember(memberName);
		logger.info("Adding member {} to group {}",memberName,groupName);
		String url = groupMembersURL + groupName;
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

	public VirtualHostAccessController getQpidAcl() {
		ResponseEntity<VirtualHostAccessController> response = restTemplate.getForEntity(aclRulesUrl, VirtualHostAccessController.class);
		logger.debug("acl extractRules return code {}", response.getStatusCode());
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
			List<PrivateChannelMember> privateChannelUsers = getPrivateChannelGroupMembers();
			List<BiConsumerMember> biConsumerMembers = getBiConsumerMembers();
			List<ServiceProviderMember> serviceProviderMembers = getServiceProviderMembers();
			return new QpidDelta(allExchanges,allQueues, privateChannelUsers, biConsumerMembers, serviceProviderMembers);

		} catch (JsonProcessingException e) {
			logger.error("Could not parse qpid delta");
			throw new RuntimeException(e);
		}
	}
}