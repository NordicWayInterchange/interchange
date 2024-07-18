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

	List<Neighbour> findAllByIgnoreIs(boolean ignore);

	List<Neighbour> findByCapabilities_StatusAndIgnoreIs(CapabilitiesStatus capabilitiesStatus, boolean ignore);

	List<Neighbour> findByIgnoreIsAndCapabilities_StatusIn(boolean ignore, CapabilitiesStatus... capabilitiesStatuses);

	List<Neighbour> findDistinctNeighboursByIgnoreIsAndOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(boolean ignore, SubscriptionStatus... subscriptionStatus);

	List<Neighbour> findDistinctNeighboursByIgnoreIsAndNeighbourRequestedSubscriptions_Subscription_SubscriptionStatusIn(boolean ignore, NeighbourSubscriptionStatus... neighbourSubscriptionStatus);


	List<Neighbour> findByControlConnection_ConnectionStatusAndIgnoreIs(ConnectionStatus connectionStatus, boolean ignore);

}
