package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.federation.model.*;

import java.util.Set;

public interface NeighbourFacade {
	Capabilities postCapabilitiesToCapabilities(Neighbour neighbour, String selfName, Set<Capability> localCapabilities);

	Set<Subscription> postSubscriptionRequest(Neighbour neighbour, Set<Subscription> subscriptions, String selfName);

	Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour);

	void deleteSubscription (Neighbour neighbour, Subscription subscription);
}
