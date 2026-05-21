package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.capability.CapabilityCalculator;
import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.OutgoingMatch;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.repository.OutgoingMatchRepository;
import no.vegvesen.ixn.federation.service.NeigbourDiscoveryService;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.NeighbourSubscriptionDeleteService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.federation.subscription.SubscriptionCalculator;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
	private final NeighbourRESTFacade neighbourFacade;
	private final ServiceProviderService serviceProviderService;
	private final NeigbourDiscoveryService neigbourDiscoveryService;
	private final InterchangeNodeProperties interchangeNodeProperties;
	private final NeighbourSubscriptionDeleteService neighbourSubscriptionDeleteService;
	private final OutgoingMatchRepository outgoingMatchRepository;


	@Autowired
	NeighbourDiscoverer(NeighbourService neighbourService,
						NeighbourRESTFacade neighbourFacade,
						ServiceProviderService serviceProviderService,
						NeigbourDiscoveryService neigbourDiscoveryService,
						InterchangeNodeProperties interchangeNodeProperties,
						NeighbourSubscriptionDeleteService neighbourSubscriptionDeleteService,
						OutgoingMatchRepository outgoingMatchRepository) {
		this.neighbourService = neighbourService;
		this.neighbourFacade = neighbourFacade;
		this.serviceProviderService = serviceProviderService;
		this.neigbourDiscoveryService = neigbourDiscoveryService;
		this.interchangeNodeProperties = interchangeNodeProperties;
		this.neighbourSubscriptionDeleteService = neighbourSubscriptionDeleteService;
		this.outgoingMatchRepository = outgoingMatchRepository;
		NeighbourMDCUtil.setLogVariables(interchangeNodeProperties.getName(), null);
	}

	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void scheduleCheckForNewNeighbours() {
		neigbourDiscoveryService.checkForNewNeighbours();
	}

	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void scheduleCapabilityExchangeWithNeighbours() {
		// Perform capability exchange with all neighbours either found through the DNS, exchanged before, failed before
		logger.debug("CapabilityExchangeWithNeighbours");
		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
		Set<Capability> localCapabilities = outgoingMatchRepository.findAll().stream()
				.map(OutgoingMatch::getCapability)
				.collect(Collectors.toSet());
		Optional<LocalDateTime> lastUpdatedLocalCapabilities = CapabilityCalculator.calculateLastUpdatedCapabilitiesOptional(serviceProviders);
		neigbourDiscoveryService.capabilityExchangeWithNeighbours(neighbourFacade, localCapabilities, lastUpdatedLocalCapabilities);
	}

	@Scheduled(fixedRateString = "${discoverer.unreachable-retry-interval}")
	public void scheduleUnreachableRetry() {
		Set<Capability> localCapabilities = outgoingMatchRepository.findAll().stream()
				.map(OutgoingMatch::getCapability)
				.collect(Collectors.toSet());
		neigbourDiscoveryService.retryUnreachable(neighbourFacade, localCapabilities);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours() {
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.debug("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourService.findNeighboursWithKnownCapabilities();
		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
		Optional<LocalDateTime> lastUpdatedLocalSubscriptions = Optional.ofNullable(SubscriptionCalculator.calculateLastUpdatedSubscriptions(serviceProviders));
		Set<LocalSubscription> localSubscriptions = SubscriptionCalculator.calculateSelfSubscriptions(serviceProviders);
		neigbourDiscoveryService.evaluateAndPostSubscriptionRequest(neighboursForSubscriptionRequest, lastUpdatedLocalSubscriptions, localSubscriptions, neighbourFacade);
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {
		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourService.getNeighboursFailedSubscriptionRequest();
		List<ServiceProvider> serviceProviders = serviceProviderService.getServiceProviders();
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
	public void syncServiceProviders() {
		serviceProviderService.syncServiceProviders();
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void deleteSubscriptionAtKnownNeighbours() {
		neighbourSubscriptionDeleteService.deleteSubscriptions(neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void setGiveUpSubscriptionsToTearDownForRemoval(){
		neigbourDiscoveryService.setGiveUpSubscriptionsToTearDownForRemoval();
	}


	@Scheduled(fixedRateString = "10000", initialDelayString = "8000")
	public void tearDownListenerEndpointsForIgnoredNeighbours(){neigbourDiscoveryService.tearDownListenerEndpointsFromIgnoredNeighbours();}
}
