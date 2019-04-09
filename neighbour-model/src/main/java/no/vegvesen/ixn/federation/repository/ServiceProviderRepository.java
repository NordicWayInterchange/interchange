package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Integer> {

	@Query(value = "select * from service_providers where name = ?", nativeQuery = true)
	ServiceProvider findByName(String name);

}
