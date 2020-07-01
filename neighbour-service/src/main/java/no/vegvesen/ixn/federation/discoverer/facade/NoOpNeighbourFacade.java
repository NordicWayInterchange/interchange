package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.federation.model.*;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NoOpNeighbourFacade implements NeighbourFacade {

	@Override
	public Capabilities postCapabilitiesToCapabilities(Neighbour neighbour, Self self) {
		return null;
	}

	@Override
	public SubscriptionRequest postSubscriptionRequest(Neighbour neighbour, Set<Subscription> subscriptions, String selfName) {
		return null;
	}

	@Override
	public Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour) {
		return null;
	}
}
