package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.*;
import no.vegvesen.ixn.federation.qpid.Queue;
import no.vegvesen.ixn.federation.repository.ListenerEndpointRepository;
import no.vegvesen.ixn.federation.service.NeighbourService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
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
		try {
			for (NeighbourSubscription sub : subscriptions) {
				Queue queue = qpidClient.getQueue(sub.getQueueName());
				if (queue != null) {
					qpidClient.removeQueue(queue);
				}
			}
			neighbourService.saveDeleteSubscriptions(neighbour.getName(), subscriptions);

			if (neighbour.getNeighbourRequestedSubscriptions().getSubscriptions().isEmpty()) {
				removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
				logger.info("Removed routing for neighbour {}", name);
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
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
			Set<CapabilitySplit> capabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
			if(neighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())){
				Set<NeighbourSubscription> allAcceptedSubscriptions = new HashSet<>(neighbour.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.ACCEPTED));
				Set<NeighbourSubscription> acceptedRedirectSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithOtherConsumerCommonName(neighbour.getName());

				setUpRedirectedRouting(acceptedRedirectSubscriptions, capabilities, delta);
				allAcceptedSubscriptions.removeAll(acceptedRedirectSubscriptions);

				if(!allAcceptedSubscriptions.isEmpty()){
					setUpRegularRouting(allAcceptedSubscriptions, capabilities, neighbour.getName(), delta);
				}
				neighbourService.saveSetupRouting(neighbour);
			} else {
				Set<NeighbourSubscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getNeighbourSubscriptionsByStatus(NeighbourSubscriptionStatus.ACCEPTED);
				setUpRegularRouting(acceptedSubscriptions, capabilities, neighbour.getName(), delta);
				neighbourService.saveSetupRouting(neighbour);
			}
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	public void setUpRegularRouting(Set<NeighbourSubscription> allAcceptedSubscriptions, Set<CapabilitySplit> capabilities, String neighbourName, QpidDelta delta) {
		for(NeighbourSubscription subscription : allAcceptedSubscriptions){
			Set<CapabilitySplit> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getMetadata().getRedirectPolicy().equals(RedirectStatus.MANDATORY)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				String queueName = "sub-" + UUID.randomUUID();
				createQueue(queueName, neighbourName, delta);
				subscription.setQueueName(queueName);
				addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbourName);
				for (CapabilitySplit cap : matchingCaps) {
					if (cap.exchangeExists()) {
						if (delta.exchangeExists(cap.getCapabilityExchangeName())) {
							qpidClient.addBinding(cap.getCapabilityExchangeName(), new Binding(cap.getCapabilityExchangeName(), queueName, new Filter(subscription.getSelector())));
						}
					}
				}
				//TODO:We have to move this up!
				NeighbourEndpoint endpoint = createEndpoint(neighbourService.getNodeName(), neighbourService.getMessagePort(), queueName);
				subscription.setEndpoints(Collections.singleton(endpoint));
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
			} else {
				logger.info("Subscription {} does not match any Service Provider Capability", subscription);
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NO_OVERLAP);
			}
			subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
		}
		logger.info("Set up routing for neighbour {}", neighbourName);
	}

	private void setUpRedirectedRouting(Set<NeighbourSubscription> redirectSubscriptions, Set<CapabilitySplit> capabilities, QpidDelta delta) {
		for(NeighbourSubscription subscription : redirectSubscriptions){
			Set<CapabilitySplit> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getMetadata().getRedirectPolicy().equals(RedirectStatus.NOT_AVAILABLE)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				String redirectQueue = "re-" + UUID.randomUUID();
				createQueue(redirectQueue, subscription.getConsumerCommonName(), delta);
				subscription.setQueueName(redirectQueue);
				for (CapabilitySplit cap : matchingCaps) {
					if (cap.exchangeExists()) {
						addSubscriberToGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, subscription.getConsumerCommonName());
						bindRemoteServiceProvider(cap.getCapabilityExchangeName(), redirectQueue, subscription);
					}
				}
				//TODO: Move 3 lines inside if over here
				NeighbourEndpoint endpoint = createEndpoint(neighbourService.getNodeName(), neighbourService.getMessagePort(), redirectQueue);
				subscription.setEndpoints(Collections.singleton(endpoint));
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.CREATED);
				logger.info("Set up routing for service provider {}", subscription.getConsumerCommonName());
			} else {
				logger.info("Subscription {} does not match any Service Provider Capability", subscription);
				subscription.setSubscriptionStatus(NeighbourSubscriptionStatus.NO_OVERLAP);
			}
			subscription.setLastUpdatedTimestamp(Instant.now().toEpochMilli());
		}
	}

	//TODO: Write tests to assure that listenerEndpoints are created
	@Scheduled(fixedRateString = "${create-subscriptions-exchange.interval}")
	public void setUpSubscriptionExchanges() {
		logger.debug("Looking for new subscriptions to set up exchanges for");
		List<Neighbour> neighbours = neighbourService.findAllNeighbours();
		for (Neighbour neighbour : neighbours) {
			if (!neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
				Set<Subscription> ourSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.CREATED);
				for (Subscription subscription : ourSubscriptions) {
					if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
						if (!subscription.exchangeIsCreated()) {
							String exchangeName = "sub-" + UUID.randomUUID().toString();
							subscription.setExchangeName(exchangeName);
							qpidClient.createHeadersExchange(exchangeName);  //TODO need to take the delta in here
							logger.debug("Set up exchange for subscription {}", subscription.toString());
						}
						createListenerEndpointFromEndpointsList(neighbour, subscription.getEndpoints(), subscription.getExchangeName());
					}
				}
			}
			neighbourService.saveNeighbour(neighbour);
		}
	}

	public void createListenerEndpointFromEndpointsList(Neighbour neighbour, Set<Endpoint> endpoints, String exchangeName) {
		for(Endpoint endpoint : endpoints) {
			createListenerEndpoint(endpoint.getHost(),endpoint.getPort(), endpoint.getSource(), exchangeName, neighbour);
		}
	}

	public void createListenerEndpoint(String host, Integer port, String source, String exchangeName, Neighbour neighbour) {
		if(listenerEndpointRepository.findByTargetAndAndSourceAndNeighbourName(exchangeName, source, neighbour.getName()) == null){
			ListenerEndpoint savedListenerEndpoint = listenerEndpointRepository.save(new ListenerEndpoint(neighbour.getName(), source, host, port, new Connection(), exchangeName));
			logger.info("ListenerEndpoint was saved: {}", savedListenerEndpoint.toString());
		}
	}

	@Scheduled(fixedRateString = "${tear-down-subscriptions-exchange.interval}")
	public void tearDownSubscriptionExchanges() {
		logger.debug("Looking for new subscriptions to set up exchanges for");
		List<Neighbour> neighbours = neighbourService.findAllNeighbours();
		for (Neighbour neighbour : neighbours) {
			if (!neighbour.getOurRequestedSubscriptions().getSubscriptions().isEmpty()) {
				Set<Subscription> ourSubscriptions = neighbour.getOurRequestedSubscriptions().getSubscriptionsByStatus(SubscriptionStatus.TEAR_DOWN);
				for (Subscription subscription : ourSubscriptions) {
					if (subscription.getConsumerCommonName().equals(interchangeNodeProperties.getName())) {
						if (!subscription.exchangeIsRemoved()) {
							Exchange exchange = qpidClient.getExchange(subscription.getExchangeName());
							if (exchange != null) {
								qpidClient.removeExchange(exchange);
							}
							subscription.removeExchangeName();
							logger.debug("Removed exchange for subscription {}", subscription.toString());
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
			Queue queue = qpidClient.createQueue(queueName, QpidClient.MAX_TTL_8_DAYS);
			qpidClient.addReadAccess(subscriberName, queueName);
			delta.addQueue(queue);
		}
	}

	private void addSubscriberToGroup(String groupName, String subscriberName) {
		logger.debug("Attempting to add subscriber {} to the group {}", subscriberName, groupName);
		GroupMember groupMember = qpidClient.getGroupMember(subscriberName,groupName);
		if (groupMember != null) {
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
