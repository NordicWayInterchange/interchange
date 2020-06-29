package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.discoverer.facade.NeighbourFacade;
import no.vegvesen.ixn.federation.model.*;

import java.util.Set;

public class DummyNeighbourFacade implements NeighbourFacade {

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
