package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.capability.CapabilityMatcher;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.BrokerClient;
import no.vegvesen.ixn.federation.qpid.QpidClient;
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
	private final BrokerClient qpidClient;
	private final ServiceProviderRouter serviceProviderRouter;

	@Autowired
	public RoutingConfigurer(NeighbourService neighbourService, BrokerClient qpidClient, ServiceProviderRouter serviceProviderRouter) {
		this.neighbourService = neighbourService;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForNeighboursToSetupRoutingFor() {
		logger.debug("Checking for new neighbours to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourService.findNeighboursToSetupRoutingFor();
		setupRouting(readyToSetupRouting);

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
		Set<NeighbourSubscription> subscriptions = neighbour.getNeighbourRequestedSubscriptions().getTearDownSubscriptions();
		try {
			for (NeighbourSubscription sub : subscriptions) {
				if (qpidClient.queueExists(sub.getQueueName())) {
					qpidClient.removeQueue(sub.getQueueName());
				}
			}
			neighbourService.saveDeleteSubscriptions(neighbour.getName(), subscriptions);

			if (neighbour.getNeighbourRequestedSubscriptions().getStatus().equals(NeighbourSubscriptionRequestStatus.EMPTY)) {
				removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
				logger.info("Removed routing for neighbour {}", name);
				//neighbourService.saveTearDownRouting(neighbour, name);
			}
		} catch (Exception e) {
			logger.error("Could not remove routing for neighbour {}", name, e);
		}
	}

	//Both neighbour and service providers binds to outgoingExchange to receive local messages
	//Service provider also binds to incomingExchange to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<Neighbour> readyToSetupRouting) {
		for (Neighbour subscriber : readyToSetupRouting) {
			setupNeighbourRouting(subscriber);
		}
	}

	void setupNeighbourRouting(Neighbour neighbour) {
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
			Set<Capability> capabilities = CapabilityCalculator.allCreatedServiceProviderCapabilities(serviceProviders);
			if(neighbour.getNeighbourRequestedSubscriptions().hasOtherConsumerCommonName(neighbour.getName())){
				Set<NeighbourSubscription> allAcceptedSubscriptions = new HashSet<>(neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptions());
				Set<NeighbourSubscription> acceptedRedirectSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptionsWithOtherConsumerCommonName(neighbour.getName());

				setUpRedirectedRouting(acceptedRedirectSubscriptions, capabilities);
				allAcceptedSubscriptions.removeAll(acceptedRedirectSubscriptions);

				if(!allAcceptedSubscriptions.isEmpty()){
					setUpRegularRouting(allAcceptedSubscriptions, capabilities, neighbour.getName());
				}
				neighbourService.saveSetupRouting(neighbour);
			} else {
				Set<NeighbourSubscription> acceptedSubscriptions = neighbour.getNeighbourRequestedSubscriptions().getAcceptedSubscriptions();
				setUpRegularRouting(acceptedSubscriptions, capabilities, neighbour.getName());
				neighbourService.saveSetupRouting(neighbour);
			}
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	public void setUpRegularRouting(Set<NeighbourSubscription> allAcceptedSubscriptions, Set<Capability> capabilities, String neighbourName) {
		for(NeighbourSubscription subscription : allAcceptedSubscriptions){
			Set<Capability> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getRedirect().equals(RedirectStatus.MANDATORY)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				String queueName = "sub-" + UUID.randomUUID();
				createQueue(queueName, neighbourName);
				subscription.setQueueName(queueName);
				addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbourName);
				for (Capability cap : matchingCaps) {
					if (cap.exchangeExists()) {
						if (qpidClient.exchangeExists(cap.getCapabilityExchangeName())) {
							bindSubscriptionQueue(cap.getCapabilityExchangeName(), subscription);
						}
					}
				}
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

	private void setUpRedirectedRouting(Set<NeighbourSubscription> redirectSubscriptions, Set<Capability> capabilities) {
		for(NeighbourSubscription subscription : redirectSubscriptions){
			Set<Capability> matchingCaps = CapabilityMatcher.matchCapabilitiesToSelector(capabilities, subscription.getSelector()).stream().filter(s -> !s.getRedirect().equals(RedirectStatus.NOT_AVAILABLE)).collect(Collectors.toSet());
			if (!matchingCaps.isEmpty()) {
				String redirectQueue = "re-" + UUID.randomUUID();
				createQueue(redirectQueue, subscription.getConsumerCommonName());
				subscription.setQueueName(redirectQueue);
				for (Capability cap : matchingCaps) {
					if (cap.exchangeExists()) {
						addSubscriberToGroup(REMOTE_SERVICE_PROVIDERS_GROUP_NAME, subscription.getConsumerCommonName());
						bindRemoteServiceProvider(cap.getCapabilityExchangeName(), redirectQueue, subscription);
					}
				}
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

	private void bindSubscriptionQueue(String exchange, NeighbourSubscription subscription) {
		qpidClient.bindDirectExchange(subscription.getSelector(), exchange, subscription.getQueueName());
	}

	private void bindRemoteServiceProvider(String exchange, String commonName, NeighbourSubscription acceptedSubscription) {
		qpidClient.bindTopicExchange(acceptedSubscription.getSelector(),exchange,commonName);
	}

	@Scheduled(fixedRateString = "${service-provider-router.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		serviceProviderRouter.syncServiceProviders(serviceProviders);
	}

	private void createQueue(String queueName, String subscriberName) {
		qpidClient.createQueue(queueName);
		qpidClient.addReadAccess(subscriberName,queueName);
	}

	private void addSubscriberToGroup(String groupName, String subscriberName) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		logger.debug("Attempting to add subscriber {} to the group {}", subscriberName, groupName);
		logger.debug("Group {} contains the following members: {}", groupName, Arrays.toString(existingGroupMembers.toArray()));
		if (!existingGroupMembers.contains(subscriberName)) {
			logger.debug("Subscriber {} did not exist in the group {}. Adding...", subscriberName, groupName);
			qpidClient.addMemberToGroup(subscriberName, groupName);
			logger.info("Added subscriber {} to Qpid group {}", subscriberName, groupName);
		} else {
			logger.warn("Subscriber {} already exists in the group {}", subscriberName, groupName);
		}
	}

	private void removeSubscriberFromGroup(String groupName, String subscriberName) {
		List<String> existingGroupMembers = qpidClient.getGroupMemberNames(groupName);
		if (existingGroupMembers.contains(subscriberName)) {
			logger.debug("Subscriber {} found in the groups {} Removing...", subscriberName, groupName);
			qpidClient.removeMemberFromGroup(subscriberName, groupName);
			logger.info("Removed subscriber {} from Qpid group {}", subscriberName, groupName);
		} else {
			logger.warn("Subscriber {} does not exist in the group {} and cannot be removed.", subscriberName, groupName);
		}
	}

	private NeighbourEndpoint createEndpoint(String host, String port, String source) {
		return new NeighbourEndpoint(source, host, Integer.parseInt(port));
	}
}
