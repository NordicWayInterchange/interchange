package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.Shard;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;
import static no.vegvesen.ixn.federation.qpid.QpidClient.REMOTE_SERVICE_PROVIDERS_GROUP_NAME;

@Component
@ConfigurationPropertiesScan("no.vegvesen.ixn")
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourService neighbourService;
	private final QpidClient qpidClient;
	private final ServiceProviderRouter serviceProviderRouter;

	private final InterchangeNodeProperties interchangeNodeProperties;

	private final ListenerEndpointRepository listenerEndpointRepository;

	@Autowired
	public RoutingConfigurer(NeighbourService neighbourService, QpidClient qpidClient, ServiceProviderRouter serviceProviderRouter, InterchangeNodeProperties interchangeNodeProperties, ListenerEndpointRepository listenerEndpointRepository) {
		this.neighbourService = neighbourService;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.listenerEndpointRepository = listenerEndpointRepository;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForNeighboursToSetupRoutingFor() {
		logger.debug("Checking for new neighbours to setup routing");
		QpidDelta delta = qpidClient.getQpidDelta();
		List<Neighbour> readyToSetupRouting = neighbourService.findNeighboursToSetupRoutingFor();
		setupRouting(readyToSetupRouting, delta);

		logger.debug("Checking for neighbours to tear down routing");
		Set<Neighbour> readyToTearDownRouting = neighbourService.findNeighboursToTearDownRoutingFor();
		tearDownRouting(readyToTearDownRouting);
	}

	void tearDownRouting(Set<Neighbour> readyToTearDownRouting) {
		for (Neighbour subscriber : readyToTearDownRouting) {
			tearDownNeighbourRouting(subscriber);
		}
	}

	void tearDownNeighbourRouting(Neighbour neighbour) {
		String name = neighbour.getName();
		Set<NeighbourSubscription> subscriptions = neighbour.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.TEAR_DOWN);
		if(neighbour.isIgnore()){
			Set<NeighbourSubscription> neighbourSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getSubscriptions();
			if(neighbourSubscriptions.isEmpty()){
				return;
			}
			else{
				neighbourSubscriptions.forEach(s->s.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN));
				subscriptions.addAll(neighbour.getNeighbourRequestedSubscriptions().getSubscriptions());
			}
		}
		Set<String> redirectedServiceProviders = new HashSet<>();
		try {
			for (NeighbourSubscription sub : subscriptions) {
				for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
					Queue queue = qpidClient.getQueue(endpoint.getSource());
					if (queue != null) {
						qpidClient.removeQueue(queue);
					}

					String consumerCommonName = sub.getConsumerCommonName();
					qpidClient.removeReadAccess(consumerCommonName, queue.getName());
					if (! consumerCommonName.equals(neighbour.getName())) {
						redirectedServiceProviders.add(consumerCommonName);
					}
				}
			}

			//If it is the last subscription of the SP, we need to remove them from the remote_sp group
			neighbour.getNeighbourRequestedSubscriptions().deleteSubscriptions(subscriptions);
			neighbourService.saveNeighbour(neighbour);
			if (neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().isEmpty()) {
				removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
				logger.info("Removed routing for neighbour {}", name);
			}
			for (String redirectedSpName : redirectedServiceProviders) {
				Set<NeighbourSubscription> subscriptionsWithConsumerCommonName = neighbour
						.getNeighbourRequestedSubscriptions()
						.getSubscriptions()
						.stream()
						.filter(s -> s.getConsumerCommonName().equals(redirectedSpName))
						.collect(Collectors.toSet());
				if (subscriptionsWithConsumerCommonName.isEmpty()) {
					removeSubscriberFromGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, redirectedSpName);
				}
			}
		} catch (Exception e) {
			logger.error("Could not remove routing for neighbour {}", name, e);
		}
	}

	//Both neighbour and service providers binds to outgoingExchange to receive local messages
	//Service provider also binds to incomingExchange to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<Neighbour> readyToSetupRouting, QpidDelta delta) {
		for (Neighbour subscriber : readyToSetupRouting) {
			setupNeighbourRouting(subscriber, delta);
		}
	}

	void setupNeighbourRouting(Neighbour neighbour, QpidDelta delta) {
		//try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
			Set<Capability> capabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
			Set<NeighbourSubscription> allAcceptedSubscriptions = new HashSet<>(neighbour.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.ACCEPTED));
			Set<NeighbourSubscription> acceptedRedirectSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithOtherConsumerCommonName(neighbour.getName());

			setUpRedirectedRouting(acceptedRedirectSubscriptions, capabilities, delta);
			allAcceptedSubscriptions.removeAll(acceptedRedirectSubscriptions);

			if(!allAcceptedSubscriptions.isEmpty()){
				setUpRegularRouting(allAcceptedSubscriptions, capabilities, neighbour.getName(), delta);
			}
			neighbourService.saveSetupRouting(neighbour);
		//} catch (Throwable e) {
		//	logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		//}
	}

	public void setUpRegularRouting(Set<NeighbourSubscription> allAcceptedSubscriptions, Set<Capability> capabilities, String neighbourName, QpidDelta delta) {
		for(NeighbourSubscription subscription : allAcceptedSubscriptions){
			logger.debug("Checking subscription {}", subscription);
			Set<Capability> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getMetadata().getRedirectPolicy().equals(RedirectStatus.MANDATORY)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				//if any of the matching caps does not have the shards set
				logger.debug("Subscription matches {} caps", matchingCaps.size());
				long numberOfCapsWithoutShardsSet = matchingCaps.stream().filter(m -> m.getMetadata().getShards().isEmpty()).count();
				logger.debug("We have {} capabilities without shards set",numberOfCapsWithoutShardsSet);
				if (numberOfCapsWithoutShardsSet == 0) {
					if (subscription.getEndpoints().isEmpty()) {
						String queueName = "sub-" + UUID.randomUUID();
						logger.debug("Creating endpoint {} for subscription {}", queueName,subscription);
						NeighbourEndpoint endpoint = createEndpoint(neighbourService.getNodeName(), neighbourService.getMessagePort(), queueName);
						subscription.setEndpoints(Collections.singleton(endpoint));
					}
					addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbourName);

					for (NeighbourEndpoint endpoint : subscription.getEndpoints()) {
						createQueue(endpoint.getSource(), neighbourName, delta);
						for (Capability cap : matchingCaps) {
							if (cap.isSharded()) {
							//TODO: if capability is sharded, check is the subscription contains chardId as well.
						} else {
							Shard shard = cap.getMetadata().getShards().get(0);
								qpidClient.addBinding(shard.getExchangeName(), new Binding(shard.getExchangeName(), endpoint.getSource(), new Filter(subscription.getSelector())));
							}
						}
					}

					subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
				}
			} else {
				logger.info("Subscription {} does not match any Service Provider Capability", subscription);
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NO_OVERLAP);
			}
			subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
		}
		logger.debug("Set up routing for neighbour {}", neighbourName);
	}

	private void setUpRedirectedRouting(Set<NeighbourSubscription> redirectSubscriptions, Set<Capability> capabilities, QpidDelta delta) {
		for(NeighbourSubscription subscription : redirectSubscriptions){
			Set<Capability> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getMetadata().getRedirectPolicy().equals(RedirectStatus.NOT_AVAILABLE)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				if (matchingCaps.stream().filter(m -> ! m.getMetadata().hasShards()).count() == 0) {
					if (subscription.getEndpoints().isEmpty()) {
						String redirectQueue = "re-" + UUID.randomUUID();
						NeighbourEndpoint endpoint = createEndpoint(neighbourService.getNodeName(), neighbourService.getMessagePort(), redirectQueue);
						subscription.setEndpoints(Collections.singleton(endpoint));
					}

					for (NeighbourEndpoint endpoint : subscription.getEndpoints()) {
						createQueue(endpoint.getSource(), subscription.getConsumerCommonName(), delta);
						for (Capability cap : matchingCaps) {
							if (cap.isSharded()) {
								//TODO: if capability is sharded, check is the subscription contains chardId as well.
							} else {
								Shard shard = cap.getMetadata().getShards().get(0);
								addSubscriberToGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, subscription.getConsumerCommonName());
								bindRemoteServiceProvider(shard.getExchangeName(), endpoint.getSource(), subscription);
							}
						}
					}
					subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
					logger.info("Set up routing for service provider {}", subscription.getConsumerCommonName());
				}
			} else {
				logger.info("Subscription {} does not match any Service Provider Capability", subscription);
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NO_OVERLAP);
			}
			subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
		}
	}

	@Scheduled(fixedRateString = "${create-subscriptions-exchange.interval}")
	public void setUpSubscriptionExchanges() {
		logger.debug("Looking for new subscriptions to set up exchanges for");
		List<Neighbour> neighbours = neighbourService.findAllNeighboursByIgnoreIs(false);
		for (Neighbour neighbour : neighbours) {
			if (!neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
				Set<Subscription> ourSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.CREATED);
				for (Subscription subscription : ourSubscriptions) {
					if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
						if (!subscription.getEndpoints().isEmpty()) {
							for (Endpoint endpoint : subscription.getEndpoints()) {
								if (!endpoint.hasShard()) {
									String exchangeName = "sub-" + UUID.randomUUID();
									endpoint.setShard(
											new SubscriptionShard(exchangeName)
									);
									qpidClient.createHeadersExchange(exchangeName);
									logger.debug("Set up exchange for subscription with id {}", subscription.getId());
									createListenerEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getSource(), exchangeName, neighbour.getName());
								}
							}
						}
					}
				}
			}
			neighbourService.saveNeighbour(neighbour);
		}
	}

	public void createListenerEndpoint(String host, Integer port, String source, String exchangeName, String neighbourName) {
		if(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(exchangeName, source, neighbourName) == null){
			ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbourName, source, host, port, new Connection(), exchangeName));
			logger.info("ListenerEndpoint was saved: {}", savedListenerEndpoint);
		}
	}

	@Scheduled(fixedRateString = "${tear-down-subscriptions-exchange.interval}")
	public void tearDownSubscriptionExchanges() {
		logger.debug("Looking for new subscriptions to tear down exchanges for");
		List<Neighbour> neighbours = neighbourService.findAllNeighbours();
		for (Neighbour neighbour : neighbours) {
			if (!neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
				Set<Subscription> ourSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.TEAR_DOWN);
				if(neighbour.isIgnore()){
					neighbour.getOurRequestedSubscriptions().getSubscriptions().forEach(s->s.setSubscriptionStatus(SubscriptionStatus.TEAR_DOWN));
					ourSubscriptions.addAll(neighbour.getOurRequestedSubscriptions().getSubscriptions());
				}
				for (Subscription subscription : ourSubscriptions) {
					if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
						Set<Endpoint> endpointsToRemove = new HashSet<>();
						for (Endpoint endpoint : subscription.getEndpoints()) {
							if (endpoint.hasShard()) {
								SubscriptionShard shard = endpoint.getShard();
								Exchange exchange = qpidClient.getExchange(shard.getExchangeName());
								if (exchange != null) {
									qpidClient.removeExchange(exchange);
									logger.debug("Removed exchange for subscription with id {}", subscription.getId());
								}
								endpoint.removeShard();
							}
							endpointsToRemove.add(endpoint);
						}
						subscription.getEndpoints().removeAll(endpointsToRemove);
					}
				}
				Set<Subscription> ourFailedSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.FAILED);
				for (Subscription subscription : ourFailedSubscriptions) {
					if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
						for (Endpoint endpoint : subscription.getEndpoints()) {
							if (endpoint.hasShard()) {
								SubscriptionShard shard = endpoint.getShard();
								if (listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(shard.getExchangeName(), endpoint.getSource(), neighbour.getName()) == null) {
									Exchange exchange = qpidClient.getExchange(shard.getExchangeName());
									if (exchange != null) {
										qpidClient.removeExchange(exchange);
										logger.debug("Removed exchange for subscription with id {}", subscription.getId());
									}
									endpoint.removeShard();
								}
							}
						}
					}
				}
			}
			neighbourService.saveNeighbour(neighbour);
		}
	}

	private void bindRemoteServiceProvider(String exchange, String queueName, NeighbourSubscription acceptedSubscription) {
		qpidClient.addBinding(exchange, new Binding(exchange, queueName, new Filter(acceptedSubscription.getSelector())));
	}

	@Scheduled(fixedRateString = "${service-provider-router.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		serviceProviderRouter.syncServiceProviders(serviceProviders, qpidClient.getQpidDelta());
	}

	private void createQueue(String queueName, String subscriberName, QpidDelta delta) {
		if (!delta.queueExists(queueName)) {
			Queue queue = qpidClient.createQueue(queueName);
			qpidClient.addReadAccess(subscriberName, queueName);
			delta.addQueue(queue);
		}
	}

	private void addSubscriberToGroup(String groupName, String subscriberName) {
		logger.debug("Attempting to add subscriber {} to the group {}", subscriberName, groupName);
		GroupMember groupMember = qpidClient.getGroupMember(subscriberName,groupName);
		if (groupMember == null) {
			logger.debug("Subscriber {} did not exist in the group {}. Adding...", subscriberName, groupName);
			qpidClient.addMemberToGroup(subscriberName,groupName);
			logger.info("Added subscriber {} to Qpid group {}", subscriberName, groupName);
		} else {
			logger.warn("Subscriber {} already exists in the group {}", subscriberName, groupName);
		}
	}

	private void removeSubscriberFromGroup(String groupName, String subscriberName) {
		GroupMember groupMember = qpidClient.getGroupMember(subscriberName,groupName);
		if (groupMember != null) {
			logger.debug("Subscriber {} found in the groups {} Removing...", subscriberName, groupName);
			qpidClient.removeMemberFromGroup(groupMember, groupName);
		} else {
			logger.warn("Subscriber {} does not exist in the group {} and cannot be removed.", subscriberName, groupName);
		}
	}

	private NeighbourEndpoint createEndpoint(String host, String port, String source) {
		return new NeighbourEndpoint(source, host, Integer.parseInt(port));
	}
}
