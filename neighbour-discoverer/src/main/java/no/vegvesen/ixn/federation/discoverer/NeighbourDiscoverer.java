package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.service.NeigbourDiscoveryService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.federation.subscription.SubscriptionCalculator;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import no.vegvesen.ixn.onboard.SelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/***
 * Functionality:
 *  - Check database for recent changes in neighbour capabilities.
 *    Compute custom subscription to each neighbour when the neighbour capability is updated.
 *    Post calculated subscription to neighbour.
 *    Receive Post response with paths to each subscription.
 *    Poll each paths until subscription status = CREATED
 *
 * - Check DNS for neighbours that we have not seen and that is not ourselves (new neighbours).
 */

@Component
public class NeighbourDiscoverer {

	private Logger logger = LoggerFactory.getLogger(NeighbourDiscoverer.class);

	private final NeighbourService neighbourService;
	private final SelfService selfService;
	private final NeighbourRESTFacade neighbourFacade;
	private final ServiceProviderService serviceProviderService;
	private final NeigbourDiscoveryService neigbourDiscoveryService;


	@Autowired
	NeighbourDiscoverer(NeighbourService neighbourService,
						SelfService selfService,
						NeighbourRESTFacade neighbourFacade,
						ServiceProviderService serviceProviderService,
						NeigbourDiscoveryService neigbourDiscoveryService) {
		this.neighbourService = neighbourService;
		this.selfService = selfService;
		this.neighbourFacade = neighbourFacade;
		this.serviceProviderService = serviceProviderService;
		this.neigbourDiscoveryService = neigbourDiscoveryService;
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), null);
	}

	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void scheduleCheckForNewNeighbours() {
		neigbourDiscoveryService.checkForNewNeighbours();
	}

	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void scheduleCapabilityExchangeWithNeighbours() {
		// Perform capability exchange with all neighbours either found through the DNS, exchanged before, failed before
		List<ServiceProvider> serviceProviders = selfService.getServiceProviders();
		Set<Capability> localCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);
		Optional<LocalDateTime> lastUpdatedLocalCapabilities = CapabilityCalculator.calculateLastUpdatedCapabilitiesOptional(serviceProviders);
		neigbourDiscoveryService.capabilityExchangeWithNeighbours(neighbourFacade, localCapabilities, lastUpdatedLocalCapabilities);
	}

	@Scheduled(fixedRateString = "${discoverer.unreachable-retry-interval}")
	public void scheduleUnreachableRetry() {
		List<ServiceProvider> serviceProviders = selfService.getServiceProviders();
		Set<Capability> localCapabilities = CapabilityCalculator.allServiceProviderCapabilities(serviceProviders);
		neigbourDiscoveryService.retryUnreachable(neighbourFacade, localCapabilities);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours() {
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.info("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourService.findNeighboursWithKnownCapabilities();
		List<ServiceProvider> serviceProviders = selfService.getServiceProviders();
		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.ofNullable(SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProviders));
		Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(serviceProviders);
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(neighboursForSubscriptionRequest, lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {
		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourService.getNeighboursFailedSubscriptionRequest();
		List<ServiceProvider> serviceProviders = selfService.getServiceProviders();
		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.ofNullable(SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProviders));
		Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(serviceProviders);
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(neighboursWithFailedSubscriptionRequest, lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void schedulePollSubscriptions() {
		neigbourDiscoveryService.pollSubscriptions(neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void schedulePollSubscriptionsWithStatusCreated() {
		neigbourDiscoveryService.pollSubscriptionsWithStatusCreated(neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.local-subscription-update-interval}", initialDelayString = "${discoverer.local-subscription-initial-delay}")
	public void updateLocalSubscriptions() {
		serviceProviderService.updateLocalSubscriptions(selfService.getMessageChannelUrl());
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void deleteSubscriptionAtKnownNeighbours() {
		neigbourDiscoveryService.deleteSubscriptions(neighbourFacade);
	}
}
