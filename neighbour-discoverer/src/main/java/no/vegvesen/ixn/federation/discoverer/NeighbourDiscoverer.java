package no.vegvesen.ixn.federation.discoverer;

/*-
 * #%L
 * neighbour-discoverer
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

import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.service.NeighbourService;
import no.vegvesen.ixn.federation.utils.NeighbourMDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	private NeighbourService neighbourService;


	@Autowired
	NeighbourDiscoverer(@Value("${interchange.node-provider.name}") String myName,
						NeighbourService neighbourService) {
		this.neighbourService = neighbourService;
		NeighbourMDCUtil.setLogVariables(myName, null);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-poll-update-interval}", initialDelayString = "${discoverer.subscription-poll-initial-delay}")
	public void schedulePollSubscriptions() {
		neighbourService.pollSubscriptions();
	}

	@Scheduled(fixedRateString = "${graceful-backoff.check-interval}", initialDelayString = "${graceful-backoff.check-offset}")
	public void gracefulBackoffPostSubscriptionRequest() {
		List<Neighbour> neighboursWithFailedSubscriptionRequest = neighbourService.getNeighboursFailedSubscriptionRequest();
		neighbourService.evaluateAndPostSubscriptionRequest(neighboursWithFailedSubscriptionRequest);
	}

	@Scheduled(fixedRateString = "${discoverer.subscription-request-update-interval}", initialDelayString = "${discoverer.subscription-request-initial-delay}")
	public void performSubscriptionRequestWithKnownNeighbours() {
		// Perform subscription request with all neighbours with capabilities KNOWN
		logger.info("Checking for any Neighbours with KNOWN capabilities");
		List<Neighbour> neighboursForSubscriptionRequest = neighbourService.findNeighboursWithKnownCapabilities();
		neighbourService.evaluateAndPostSubscriptionRequest(neighboursForSubscriptionRequest);
	}

	@Scheduled(fixedRateString = "${discoverer.capabilities-update-interval}", initialDelayString = "${discoverer.capability-post-initial-delay}")
	public void scheduleCapabilityExchangeWithNeighbours() {
		// Perform capability exchange with all neighbours either found through the DNS, exchanged before, failed before
		neighbourService.capabilityExchangeWithNeighbours();
	}

	@Scheduled(fixedRateString = "${discoverer.dns-lookup-interval}", initialDelayString = "${discoverer.dns-initial-start-delay}")
	public void scheduleCheckForNewNeighbours() {
		neighbourService.checkForNewNeighbours();
	}
}
