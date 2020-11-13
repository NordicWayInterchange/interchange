package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.ConnectionStatus;
import no.vegvesen.ixn.federation.model.Neighbour;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NeighbourRepository extends CrudRepository<Neighbour, Integer> {

	Neighbour findByName(String name);

	@NonNull
	List<Neighbour> findAll();

	List<Neighbour> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);
	List<Neighbour> findByCapabilities_StatusIn(Capabilities.CapabilitiesStatus... capabilitiesStatuses);

	List<Neighbour> findByNeighbourRequestedSubscriptions_Status(SubscriptionRequestStatus status);

	List<Neighbour> findByNeighbourRequestedSubscriptions_StatusIn(SubscriptionRequestStatus... status);

	List<Neighbour> findByOurRequestedSubscriptions_StatusIn(SubscriptionRequestStatus... statuses);

	List<Neighbour> findNeighboursByOurRequestedSubscriptions_Subscription_SubscriptionStatusIn(SubscriptionStatus... subscriptionStatus);

	@Query(value = "select distinct i from Neighbour i join i.neighbourRequestedSubscriptions sr join sr.subscription s where sr.status = :subscriptionRequestStatus and s.subscriptionStatus = :subscriptionStatus")
	List<Neighbour> findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(
			@Param("subscriptionRequestStatus") SubscriptionRequestStatus subscriptionRequestStatus,
			@Param("subscriptionStatus") SubscriptionStatus subscriptionStatus
	);

	List<Neighbour> findByControlConnection_ConnectionStatus(ConnectionStatus connectionStatus);
}
