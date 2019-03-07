package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.IxnServiceProvider;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceProviderRepository extends CrudRepository<IxnServiceProvider, Integer> {

	@Query(value = "select * from service_providers where name = ?", nativeQuery = true)
	IxnServiceProvider findByName(String name);

}
