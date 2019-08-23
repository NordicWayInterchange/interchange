package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.model.SubscriptionRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Integer> {

	ServiceProvider findByName(String name);

	List<ServiceProvider> findBySubscriptionRequest_Status(SubscriptionRequest.SubscriptionRequestStatus status);

	List<ServiceProvider> findBySubscriptionRequest_StatusIn(SubscriptionRequest.SubscriptionRequestStatus... statuses);

}
