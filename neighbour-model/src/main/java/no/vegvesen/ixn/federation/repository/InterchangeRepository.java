package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.Interchange;
import no.vegvesen.ixn.federation.model.Subscription;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterchangeRepository extends CrudRepository<Interchange, Integer> {

	Interchange findByName(String name);

	List<Interchange> findByCapabilities_Status(Capabilities.CapabilitiesStatus capabilitiesStatus);

	List<Interchange> findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus status);

	List<Interchange> findByFedIn_StatusIn(SubscriptionRequest.SubscriptionRequestStatus... statuses);

	@SuppressWarnings("SpringDataRepositoryMethodParametersInspection")
	List<Interchange> findInterchangesByFedIn_Subscription_SubscriptionStatusIn(Subscription.SubscriptionStatus... subscriptionStatus);

	List<Interchange> findInterchangesByCapabilities_Status_AndFedIn_Status(Capabilities.CapabilitiesStatus capabilitiesStatus, SubscriptionRequest.SubscriptionRequestStatus requestStatus);

	@Query(value = "select distinct i from Interchange i join i.subscriptionRequest sr join sr.subscription s where sr.status = :subscriptionRequestStatus and s.subscriptionStatus = :subscriptionStatus")
	List<Interchange> findInterchangesBySubscriptionRequest_Status_And_SubscriptionStatus(
			@Param("subscriptionRequestStatus") SubscriptionRequest.SubscriptionRequestStatus subscriptionRequestStatus,
			@Param("subscriptionStatus") Subscription.SubscriptionStatus subscriptionStatus
	);
}
