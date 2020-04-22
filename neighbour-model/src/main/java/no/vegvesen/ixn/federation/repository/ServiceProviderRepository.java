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

	List<ServiceProvider> findBySubscriptions_StatusIn(LocalSubscriptionStatus ... statuses);

}
