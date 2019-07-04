package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Integer> {

	ServiceProvider findByName(String name);

}
