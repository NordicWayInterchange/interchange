package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Self;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SelfRepository extends CrudRepository<Self, Integer> {

	Self findByName(String name);
}
