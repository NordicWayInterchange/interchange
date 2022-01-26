package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.Match;
import no.vegvesen.ixn.federation.model.MatchStatus;
import no.vegvesen.ixn.federation.model.SubscriptionStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends CrudRepository<Match, Integer> {

    List<Match> findAll();

    List<Match> findAllByStatus(MatchStatus status);

    Match findBySubscriptionId(Integer subscriptionId);

    Match findByLocalSubscriptionId(Integer subscriptionId);

    List<Match> findAllByLocalSubscription_Selector(String selector);

    List<Match> findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus... subscription_subscriptionStatus);

    Match findBySubscription_ExchangeName(String exchangeName);
}
