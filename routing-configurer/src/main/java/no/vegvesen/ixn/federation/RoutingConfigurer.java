package no.vegvesen.ixn.federation;

/*-
 * #%L
 * routing-configurer
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import no.vegvesen.ixn.federation.api.v1_0.SubscriptionStatus;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.repository.NeighbourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static no.vegvesen.ixn.federation.qpid.QpidClient.FEDERATED_GROUP_NAME;

@Component
public class RoutingConfigurer {

	private static Logger logger = LoggerFactory.getLogger(RoutingConfigurer.class);

	private final NeighbourRepository neighbourRepository;
	private final QpidClient qpidClient;
	private final ServiceProviderRouter serviceProviderRouter;

	@Autowired
	public RoutingConfigurer(NeighbourRepository neighbourRepository, QpidClient qpidClient, ServiceProviderRouter serviceProviderRouter) {
		this.neighbourRepository = neighbourRepository;
		this.qpidClient = qpidClient;
		this.serviceProviderRouter = serviceProviderRouter;
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForNeighboursToSetupRoutingFor() {
		logger.debug("Checking for new neighbours to setup routing");
		List<Neighbour> readyToSetupRouting = neighbourRepository.findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(SubscriptionRequestStatus.REQUESTED, SubscriptionStatus.ACCEPTED);
		logger.debug("Found {} neighbours to set up routing for {}", readyToSetupRouting.size(), readyToSetupRouting);
		setupRouting(readyToSetupRouting);

		logger.debug("Checking for neighbours to tear down routing");
		List<Neighbour> readyToTearDownRouting = neighbourRepository.findBySubscriptionRequest_Status(SubscriptionRequestStatus.TEAR_DOWN);
		tearDownRouting(readyToTearDownRouting);
	}

	void tearDownRouting(List<Neighbour> readyToTearDownRouting) {
		for (Neighbour subscriber : readyToTearDownRouting) {
			tearDownNeighbourRouting(subscriber);
		}
	}

	void tearDownNeighbourRouting(Neighbour neighbour) {
		String name = neighbour.getName();
		try {
			logger.debug("Removing routing for neighbour {}", name);
			qpidClient.removeQueue(name);
			removeSubscriberFromGroup(FEDERATED_GROUP_NAME, name);
			logger.info("Removed routing for neighbour {}", name);
			neighbour.setSubscriptionRequestStatus(SubscriptionRequestStatus.EMPTY);
			neighbourRepository.save(neighbour);
			logger.debug("Saved neighbour {} with subscription request status EMPTY", name);
		} catch (Exception e) {
			logger.error("Could not remove routing for neighbour {}", name, e);
		}
	}

	//Both neighbour and service providers binds to nwEx to receive local messages
	//Service provider also binds to fedEx to receive messages from neighbours
	//This avoids loop of messages
	private void setupRouting(List<Neighbour> readyToSetupRouting) {
		for (Neighbour subscriber : readyToSetupRouting) {
			setupNeighbourRouting(subscriber);
		}
	}

	void setupNeighbourRouting(Neighbour neighbour) {
		try {
			logger.debug("Setting up routing for neighbour {}", neighbour.getName());
			createQueue(neighbour.getName());
			addSubscriberToGroup(FEDERATED_GROUP_NAME, neighbour.getName());
			bindSubscriptions("nwEx", neighbour);
			for (Subscription subscription : neighbour.getSubscriptionRequest().getSubscriptions()) {
				subscription.setSubscriptionStatus(SubscriptionStatus.CREATED);
			}
			logger.info("Set up routing for neighbour {}", neighbour.getName());
			neighbour.getSubscriptionRequest().setStatus(SubscriptionRequestStatus.ESTABLISHED);

			neighbourRepository.save(neighbour);
			logger.debug("Saved neighbour {} with subscription request status ESTABLISHED", neighbour.getName());
		} catch (Throwable e) {
			logger.error("Could not set up routing for neighbour {}", neighbour.getName(), e);
		}
	}

	private void bindSubscriptions(String exchange, Neighbour neighbour) {
		unbindOldUnwantedBindings(neighbour, exchange);
		for (Subscription subscription : neighbour.getSubscriptionRequest().getAcceptedSubscriptions()) {
			qpidClient.addBinding(subscription.getSelector(), neighbour.getName(), subscription.bindKey(), exchange);
		}
	}

	private void unbindOldUnwantedBindings(Neighbour neighbour, String exchangeName) {
		String name = neighbour.getName();
		Set<String> existingBindKeys = qpidClient.getQueueBindKeys(name);
		Set<String> unwantedBindKeys = neighbour.getUnwantedBindKeys(existingBindKeys);
		for (String unwantedBindKey : unwantedBindKeys) {
			qpidClient.unbindBindKey(name, unwantedBindKey, exchangeName);
		}
	}

	@Scheduled(fixedRateString = "${routing-configurer.interval}")
	public void checkForServiceProvidersToSetupRoutingFor() {
		logger.debug("Checking for new service providers to setup routing");
		Iterable<ServiceProvider> serviceProviders = serviceProviderRouter.findServiceProviders();
		serviceProviderRouter.syncServiceProviders(serviceProviders);
	}


	private void createQueue(String subscriberName) {
		qpidClient.createQueue(subscriberName);
		qpidClient.addReadAccess(subscriberName,subscriberName);

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

}
