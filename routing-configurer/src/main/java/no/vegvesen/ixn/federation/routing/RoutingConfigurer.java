package no.vegvesen.ixn.federation.routing;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.CapabilityShard;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.MatchDiscoveryService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ConfigurationPropertiesScan("no.vegvesen.ixn")
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourService neighbourService;

	private final QpidClient qpidClient;

	private final ServiceProviderRouter serviceProviderRouter;

	private final InterchangeNodeProperties interchangeNodeProperties;

	private final ListenerEndpointRepository listenerEndpointRepository;

	private final MatchDiscoveryService matchDiscoveryService;
	private final OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

	@Autowired
	public RoutingConfigurer(NeighbourService neighbourService, QpidClient qpidClient, ServiceProviderRouter serviceProviderRouter, InterchangeNodeProperties interchangeNodeProperties, ListenerEndpointRepository listenerEndpointRepository, MatchDiscoveryService matchDiscoveryService,
							 OutgoingMatchDiscoveryService outgoingMatchDiscoveryService) {
		this.neighbourService = neighbourService;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.listenerEndpointRepository = listenerEndpointRepository;
        this.matchDiscoveryService = matchDiscoveryService;
		this.outgoingMatchDiscoveryService = outgoingMatchDiscoveryService;
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
				neighbour.getControlConnection().unreachable();
				neighbourService.saveNeighbour(neighbour);
				return;
			}
			else{
				neighbourSubscriptions.forEach(s->s.setSubscriptionStatus(NeighbourSubscriptionStatus.TEAR_DOWN));
				neighbour.getControlConnection().unreachable();
				subscriptions.addAll(neighbour.getNeighbourRequestedSubscriptions().getSubscriptions());
			}
		}
		Set<String> redirectedServiceProviders = new HashSet<>();
			for (NeighbourSubscription sub : subscriptions) {
                String consumerCommonName = sub.getConsumerCommonName();
                for (NeighbourEndpoint endpoint : sub.getEndpoints()) {
                    Queue queue = qpidClient.getQueue(endpoint.getSource());
                    if (queue != null) {
                        qpidClient.removeQueue(queue);
                        qpidClient.removeReadAccess(consumerCommonName, queue.getName());
                    }

					if (! consumerCommonName.equals(neighbour.getName())) {
						redirectedServiceProviders.add(consumerCommonName);
					}
				}
			}

			//If it is the last subscription of the SP, we need to remove them from the remote_sp group
			neighbour.getNeighbourRequestedSubscriptions().deleteSubscriptions(subscriptions);
			neighbourService.saveNeighbour(neighbour);
			if (neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().isEmpty()) {
				NeighbourMember groupMember = qpidClient.getNeighbourMember(name);
				if (groupMember != null) {
					logger.debug("Neighbour member '{}' found in the group", name);
					qpidClient.removeNeighbourMemberFromGroup(groupMember);
				} else {
					logger.warn("Neighbour member '{}' does not exist in the group.", name);
				}
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
					RemoteServiceProviderMember groupMember = qpidClient.getRemoteServiceProviderMember(redirectedSpName);
					if (groupMember != null) {
						logger.debug("Remote service provider '{}' found in group. Removing...", redirectedSpName);
						qpidClient.removeRemoteServiceProviderMemberFromGroup(groupMember);
					} else {
						logger.warn("Remote service provider '{}' does not exist in the group and cannot be removed.", redirectedSpName);
					}
				}
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
		logger.debug("Setting up routing for neighbour {}", neighbour.getName());
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		Set<Capability> capabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
		Set<NeighbourSubscription> allAcceptedSubscriptions = new HashSet<>(neighbour.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatusIn(NeighbourSubscriptionStatus.ACCEPTED, NeighbourSubscriptionStatus.CREATED));
		Set<NeighbourSubscription> acceptedRedirectSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithOtherConsumerCommonName(neighbour.getName());

		setUpRedirectedRouting(acceptedRedirectSubscriptions, capabilities, delta);
		allAcceptedSubscriptions.removeAll(acceptedRedirectSubscriptions);
		if(!allAcceptedSubscriptions.isEmpty()){
			setUpRegularRouting(allAcceptedSubscriptions, capabilities, neighbour.getName(), delta);
		}
		neighbourService.saveSetupRouting(neighbour);
	}

	public void setUpRegularRouting(Set<NeighbourSubscription> allAcceptedSubscriptions, Set<Capability> capabilities, String neighbourName, QpidDelta delta) {
		for(NeighbourSubscription subscription : allAcceptedSubscriptions){
			logger.debug("Checking subscription {}", subscription);
			Set<Capability> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getMetadata().getRedirectPolicy().equals(RedirectStatus.MANDATORY)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				logger.debug("Subscription matches {} caps", matchingCaps.size());

				NeighbourEndpoint endpoint = subscription.getEndpoints().stream().findFirst().orElse(null);
				if(endpoint == null) {
					String queueName = "sub-" + UUID.randomUUID();
					logger.debug("Creating endpoint {} for subscription with id {}", queueName, subscription.getId());
					endpoint = createEndpoint(neighbourService.getBrokerExternalName(), neighbourService.getMessagePort(), queueName);
					subscription.setEndpoints(Collections.singleton(endpoint));
				}
				logger.debug("Attempting to add neighbour member {} to the group", neighbourName);
				NeighbourMember groupMember = qpidClient.getNeighbourMember(neighbourName);
				if (groupMember == null) {
					logger.debug("Neighbour '{}' did not exist in the group.", neighbourName);
					qpidClient.addNeighbourMemberToGroup(neighbourName);
					logger.debug("Added neighbour member '{}' to group", neighbourName);
				} else {
					logger.debug("Neighbour member '{}' already exists in the group", neighbourName);
				}
				createQueue(endpoint.getSource(), neighbourName, delta);

				for (Capability capability : matchingCaps) {
					for (CapabilityShard shard : capability.getShards()) {
						if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), subscription.getSelector())) {
							Exchange exchange = delta.findByExchangeName(shard.getExchangeName());
							if (exchange != null) {
								if (! exchange.isBoundTo(endpoint.getSource())) {
									Binding binding = new Binding(shard.getExchangeName(), endpoint.getSource(), new Filter(subscription.getSelector()));
									exchange.addBinding(binding);
									qpidClient.addBinding(shard.getExchangeName(), binding);
								} else {
									logger.debug("Exchange '{}' already bound to '{}'", shard.getExchangeName(), endpoint.getSource());
								}
							} else {
								logger.debug("Exchange '{}' does not exist", shard.getExchangeName());
							}
						}
					}
				}

				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
			} else {
				logger.debug("Subscription {} does not match any Service Provider Capability", subscription);
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
				logger.debug("Subscription matches {} caps", matchingCaps.size());

				String redirectQueue = "re-" + UUID.randomUUID();
				logger.debug("Creating endpoint {} for subscription with id {}", redirectQueue, subscription.getId());
				NeighbourEndpoint endpoint = createEndpoint(neighbourService.getBrokerExternalName(), neighbourService.getMessagePort(), redirectQueue);
				subscription.setEndpoints(Collections.singleton(endpoint));

				createQueue(endpoint.getSource(), subscription.getConsumerCommonName(), delta);
				String subscriberName = subscription.getConsumerCommonName();
				logger.debug("Attempting to add remote service provider '{}' to group", subscriberName);
				RemoteServiceProviderMember member = qpidClient.getRemoteServiceProviderMember(subscriberName);
				if (member == null) {
					logger.debug("remote service provider '{}' did not exist in group", subscriberName);
					 qpidClient.addRemoteServiceProvicerMemberToGroup(subscriberName);
					logger.info("Added remote service provider '{}' to group", subscriberName);
				} else {
					logger.debug("Remote service provider '{}' already exists in the group", subscriberName);
				}

				for (Capability capability : matchingCaps) {
					for (CapabilityShard shard : capability.getShards()) {
						if (subscription.isSharded()) {
							if (CapabilityMatcher.matchCapabilityApplicationWithShardToSelector(capability.getApplication(), shard.getShardId(), subscription.getSelector())) {
								qpidClient.addBinding(shard.getExchangeName(), new Binding(shard.getExchangeName(), endpoint.getSource(), new Filter(subscription.getSelector())));
							}
						} else {
							qpidClient.addBinding(shard.getExchangeName(), new Binding(shard.getExchangeName(), endpoint.getSource(), new Filter(subscription.getSelector())));
						}
					}
				}
				subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
			} else {
				logger.debug("Subscription {} does not match any Service Provider Capability", subscription);
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NO_OVERLAP);
			}
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
									logger.info("Set up exchange for subscription with id {}", subscription.getId());
									createListenerEndpoint(endpoint.getHost(), endpoint.getPort(), endpoint.getSource(), exchangeName, neighbour.getName(), endpoint.getDynamicFilter());
								}
								else{
									Exchange exchange = qpidClient.getExchange(endpoint.getShard().getExchangeName());
									if(exchange == null){
										qpidClient.createHeadersExchange(endpoint.getShard().getExchangeName());
										logger.info("Set up exchange for subscription with id {}", subscription.getId());
									}
								}
							}
						}
					}
				}
			}
			neighbourService.saveNeighbour(neighbour);
		}
	}

	public void createListenerEndpoint(String host, Integer port, String source, String exchangeName, String neighbourName, String dynamicFilter) {
		if(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(exchangeName, source, neighbourName) == null){
			ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbourName, source, host, port, new Connection(), exchangeName, dynamicFilter));
			logger.info("ListenerEndpoint was created: {}", savedListenerEndpoint);
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
									logger.info("Removed exchange for subscription with id {}", subscription.getId());
								}
								endpoint.removeShard();
							}
							endpointsToRemove.add(endpoint);
						}
						subscription.getEndpoints().removeAll(endpointsToRemove);
					} else {
						//NOTE also have to remove the endpoints for redirect subscriptions here
						subscription.getEndpoints().clear();
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
										logger.info("Removed exchange for subscription with id {}", subscription.getId());
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

	@Scheduled(fixedRateString = "${service-provider-router.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		serviceProviderRouter.syncServiceProviders(serviceProviders, qpidClient.getQpidDelta());
	}

	@Scheduled(fixedRateString = "${routing-configurer.match-update-interval}", initialDelayString = "${routing-configurer.local-subscription-initial-delay}")
	public void createMatches() {
		matchDiscoveryService.syncLocalSubscriptionAndSubscriptionsToCreateMatch(serviceProviderRouter.findServiceProvidersAsList(), neighbourService.findAllNeighboursByIgnoreIs(false));
	}

	@Scheduled(fixedRateString = "${routing-configurer.match-update-interval}", initialDelayString = "${routing-configurer.local-subscription-initial-delay}")
	public void syncMatchesToDelete() {
		matchDiscoveryService.syncMatchesToDelete();
	}

	@Scheduled(fixedRateString = "${discoverer.match-update-interval}", initialDelayString = "${discoverer.local-subscription-initial-delay}")
	public void createOutgoingMatches() {
		outgoingMatchDiscoveryService.syncLocalDeliveryAndCapabilityToCreateOutgoingMatch(serviceProviderRouter.findServiceProvidersAsList());
	}

	@Scheduled(fixedRateString = "${discoverer.match-update-interval}", initialDelayString = "${discoverer.local-subscription-initial-delay}")
	public void updateOutgoingMatchesToTearDown() {
		outgoingMatchDiscoveryService.syncOutgoingMatchesToDelete();
	}

	private void createQueue(String queueName, String subscriberName, QpidDelta delta) {
		Queue queue = delta.findByQueueName(queueName);
		if (queue == null) {
			queue = qpidClient.createQueue(queueName);
			qpidClient.addReadAccess(subscriberName, queueName);
            logger.info("Created queue {} for user {}", queueName, subscriberName);
			delta.addQueue(queue);
		}
	}

	private NeighbourEndpoint createEndpoint(String host, String port, String source) {
		return new NeighbourEndpoint(source, host, Integer.parseInt(port));
	}
}
