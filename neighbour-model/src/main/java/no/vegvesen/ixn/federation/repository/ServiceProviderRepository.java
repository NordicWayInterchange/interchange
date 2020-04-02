package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.SubscriptionRequestStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Integer> {

	ServiceProvider findByName(String name);

	List<ServiceProvider> findBySubscriptionRequest_Status(SubscriptionRequestStatus status);

	List<ServiceProvider> findBySubscriptionRequest_StatusIn(SubscriptionRequestStatus... statuses);

	//TODO test denne med flere som er CREATED og en som er REQUESTED. Får jeg alle?
	List<ServiceProvider> findBySubscriptions_StatusIn(LocalSubscriptionStatus ... statuses);

}
