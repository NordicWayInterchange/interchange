package no.vegvesen.ixn.federation.discoverer.facade;

import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.federation.model.capability.Capability;
import no.vegvesen.ixn.federation.model.capability.NeighbourCapability;

import java.util.Set;

public interface NeighbourFacade {
	Set<NeighbourCapability> postCapabilitiesToCapabilities(Neighbour neighbour, String selfName, Set<Capability> localCapabilities);

	Set<Subscription> postSubscriptionRequest(Neighbour neighbour, Set<Subscription> subscriptions, String selfName);

	Subscription pollSubscriptionStatus(Subscription subscription, Neighbour neighbour);

	void deleteSubscription (Neighbour neighbour, Subscription subscription);
}
