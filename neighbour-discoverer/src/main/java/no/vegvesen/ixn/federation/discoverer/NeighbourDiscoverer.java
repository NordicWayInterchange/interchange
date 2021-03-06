package no.vegvesen.ixn.federation.discoverer;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourRESTFacade;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.service.ServiceProviderService;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import no.vegvesen.ixn.onboard.SelfService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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


	@Autowired
	NeighbourDiscoverer(NeighbourService neighbourService,
						SelfService selfService,
						NeighbourRESTFacade neighbourFacade,
						ServiceProviderService serviceProviderService) {
		this.neighbourService = neighbourService;
		this.selfService = selfService;
		this.neighbourFacade = neighbourFacade;
		this.serviceProviderService = serviceProviderService;
		NeighbourMDCUtil.setLogVariables(selfService.getNodeProviderName(), null);
	}

	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void scheduleCheckForNewNeighbours() {
		neighbourService.checkForNewNeighbours();
	}

	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void scheduleCapabilityExchangeWithNeighbours() {
		// Perform capability exchange with all neighbours either found through the DNS, exchanged before, failed before
		neighbourService.capabilityExchangeWithNeighbours(selfService.fetchSelf(), neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.unreachable-retry-interval}")
	public void scheduleUnreachableRetry() {
		neighbourService.retryUnreachable(selfService.fetchSelf(), neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours() {
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.info("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourService.findNeighboursWithKnownCapabilities();
		neighbourService.evaluateAndPostSubscriptionRequest(neighboursForSubscriptionRequest, selfService.fetchSelf(), neighbourFacade);
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {
		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourService.getNeighboursFailedSubscriptionRequest();
		neighbourService.evaluateAndPostSubscriptionRequest(neighboursWithFailedSubscriptionRequest, selfService.fetchSelf(), neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void schedulePollSubscriptions() {
		neighbourService.pollSubscriptions(neighbourFacade);
	}

	@Scheduled(fixedRateString = "${discoverer.local-subscription-update-interval}", initialDelayString = "${discoverer.local-subscription-initial-delay}")
	public void updateLocalSubscriptions() {
		serviceProviderService.updateLocalSubscriptions();
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void deleteSubscriptionAtKnownNeighbours() {
		neighbourService.deleteSubscriptions(neighbourFacade);
	}
}
