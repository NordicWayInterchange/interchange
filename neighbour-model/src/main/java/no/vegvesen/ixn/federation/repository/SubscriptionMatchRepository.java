package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.SubscriptionMatch;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionMatchRepository extends CrudRepository<SubscriptionMatch, Integer> {

    List<SubscriptionMatch> findAll();

    List<SubscriptionMatch> findAllBySubscriptionId(Integer subscriptionId);

    SubscriptionMatch findBySubscriptionIdAndLocalSubscriptionId(Integer subscriptionId, Integer localSubscriptionId);

    List<SubscriptionMatch> findAllByLocalSubscriptionId(Integer subscriptionId);

    SubscriptionMatch findBySubscriptionIdAndAndLocalSubscriptionId(Integer subscriptionId, Integer localSubscriptionId);

    List<SubscriptionMatch> findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus... subscription_subscriptionStatus);
}
