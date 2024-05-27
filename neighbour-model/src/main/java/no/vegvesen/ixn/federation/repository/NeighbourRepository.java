package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourRepository extends CrudRepository<Neighbour, Integer> {

	Neighbour findByName(String name);

	@NonNull
	List<Neighbour> findAll();

	List<Neighbour> findByCapabilities_Status(CapabilitiesStatus capabilitiesStatus);
	List<Neighbour> findByCapabilities_StatusIn(CapabilitiesStatus... capabilitiesStatuses);

	List<Neighbour> findDistinctNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus... subscriptionStatus);

	List<Neighbour> findDistinctNeighboursByNeighbourRequestedSubscriptions_Subscription_SubscriptionStatusIn(NeighbourSubscriptionStatus... neighbourSubscriptionStatus);

	List<Neighbour> findByControlConnection_ConnectionStatus(ConnectionStatus connectionStatus);
}
