package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.NeighbourSubscription;
import no.vegvesen.ixn.federation.model.SubscriptionCapabilityMatch;
import no.vegvesen.ixn.federation.model.capability.Capability;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionCapabilityMatchRepository extends CrudRepository<SubscriptionCapabilityMatch, Integer> {
    List<SubscriptionCapabilityMatch> findAllByNeighbourSubscriptionId(Integer id);

    List<SubscriptionCapabilityMatch> findAll();

    SubscriptionCapabilityMatch findSubscriptionCapabilityMatchByCapabilityAndNeighbourSubscription(Capability capability, NeighbourSubscription neighbourSubscription);
}