package no.vegvesen.ixn.federation.repository;

import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
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

    List<Match> findAllBySubscriptionId(Integer subscriptionId);

    List<Match> findAllByLocalSubscriptionId(Integer subscriptionId);

    Match findBySubscriptionIdAndAndLocalSubscriptionId(Integer subscriptionId, Integer localSubscriptionId);

    List<Match> findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus... subscription_subscriptionStatus);

    List<Match> findBySubscription_ExchangeName(String exchangeName);

    List<Match> findAllByServiceProviderNameAndStatus(String serviceProviderName, MatchStatus status);

    List<Match> findAllByServiceProviderName(String serviceProviderName);
}
