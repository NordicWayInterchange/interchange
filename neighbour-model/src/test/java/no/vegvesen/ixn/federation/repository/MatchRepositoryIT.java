package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
public class MatchRepositoryIT {

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;

    @Test
    @Transactional
    @Disabled
    public void saveMatch() {
        Match match = new Match(new LocalSubscription(), new Subscription(), MatchStatus.CREATED);
        matchRepository.save(match);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    @Transactional
    @Disabled
    public void saveNeighbourAndServiceProviderBeforeSavingMatch() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub, sub, MatchStatus.REQUESTED);
        matchRepository.save(match);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    @Transactional
    @Disabled
    public void saveServiceProviderBeforeSavingMatch() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));


        Match match = new Match(locSub, sub, MatchStatus.REQUESTED);
        matchRepository.save(match);

        //Have we saved the neighbour?
        Neighbour fromDb = neighbourRepository.findByName("neighbour");
        //No.
        assertThat(fromDb).isNull();

        //Have we saved the subscription?
        assertThat(sub.getId()).isNotNull(); //Yep! And the original object is updated :-)

        neighbourRepository.save(neighbour);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);

    }

    @Test
    @Transactional
    @Disabled
    public void deletingMatchFromDatabase() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub,sub,MatchStatus.REQUESTED);
        matchRepository.save(match);

        matchRepository.delete(match);

        ServiceProvider serviceProvider = serviceProviderRepository.findByName("my-sp");
        assertThat(serviceProvider.getSubscriptions()).isNotEmpty();

        Neighbour neighbourFromDb = neighbourRepository.findByName("neighbour");
        assertThat(neighbourFromDb.getOurRequestedSubscriptions().getSubscriptions()).isNotEmpty();

    }

    @Test
    @Transactional
    @Disabled
    public void saveNeighbourBeforeSavingMatch() {
        LocalSubscription locSub = new LocalSubscription();

        Subscription sub = new Subscription(SubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "path/1", "my-interchange");
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub, sub, MatchStatus.REQUESTED);
        matchRepository.save(match);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    @Transactional
    @Disabled
    public void saveNeighbourAfterSavingMatch() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription(SubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "path/1", "my-interchange");
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        Match match = new Match(locSub, sub, MatchStatus.REQUESTED);
        matchRepository.save(match);

        neighbourRepository.save(neighbour);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    public void findOnUnsavedMatch() {
        Match noMatch = matchRepository.findBySubscriptionId(1);
        assertThat(noMatch).isNull();
    }

    @Test
    @Transactional
    @Disabled
    public void deleteSubscriptionAndLocalSubscriptionBeforeDeletingMatch() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription(SubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "path/1", "my-interchange");
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub, sub, MatchStatus.REQUESTED);
        matchRepository.save(match);

        matchRepository.deleteAll();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Set<Subscription> requestedSubscriptions = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions();

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        Set<LocalSubscription> localSubscriptions = savedServiceProvider.getSubscriptions();

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(0);
        assertThat(requestedSubscriptions).hasSize(1);
        assertThat(localSubscriptions).hasSize(1);
    }

    @Test
    public void tryAddingTwoMatchesWithSameLocalSubscription() {
        LocalSubscription locSub = new LocalSubscription();
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        Subscription sub1 = new Subscription();
        Neighbour neighbour1 = new Neighbour("neighbour1", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub1)));

        neighbourRepository.save(neighbour);
        neighbourRepository.save(neighbour1);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour1 = neighbourRepository.findByName("neighbour1");
        Subscription savedSubscription1 = savedNeighbour1.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();


        Match match = new Match(savedLocalSubscription, savedSubscription, MatchStatus.REQUESTED);
        Match match1 = new Match(savedLocalSubscription, savedSubscription1, MatchStatus.REQUESTED);
        matchRepository.save(match);
        matchRepository.save(match1);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(2);
        //clean-up
        matchRepository.deleteAll();
        neighbourRepository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void tryAddingMatchWithLocalSubscriptionFromBase() {
        LocalSubscription locSub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-neighbour");
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());
        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));
        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, MatchStatus.REQUESTED);

        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);

        //clean-up
        matchRepository.deleteAll();
        neighbourRepository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void findMatchByOneSubscriptionStatus() {
        LocalSubscription locSub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-neighbour");
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());
        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));
        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, MatchStatus.REQUESTED);

        matchRepository.save(match);

        assertThat(matchRepository.findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED)).hasSize(1);

        //clean-up
        matchRepository.deleteAll();
        neighbourRepository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

    @Test
    public void findMatchByTwoSubscriptionStatuses() {
        LocalSubscription locSub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b", "my-neighbour");
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub)));

        Subscription sub1 = new Subscription("a=b", SubscriptionStatus.CREATED);
        Neighbour neighbour1 = new Neighbour("neighbour1", new Capabilities(), new SubscriptionRequest(), new SubscriptionRequest(SubscriptionRequestStatus.REQUESTED, Collections.singleton(sub1)));

        neighbourRepository.save(neighbour);
        neighbourRepository.save(neighbour1);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour1 = neighbourRepository.findByName("neighbour1");
        Subscription savedSubscription1 = savedNeighbour1.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();


        Match match = new Match(savedLocalSubscription, savedSubscription, MatchStatus.REQUESTED);
        Match match1 = new Match(savedLocalSubscription, savedSubscription1, MatchStatus.REQUESTED);
        matchRepository.save(match);
        matchRepository.save(match1);

        assertThat(matchRepository.findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.CREATED)).hasSize(2);
        //clean-up
        matchRepository.deleteAll();
        neighbourRepository.deleteAll();
        serviceProviderRepository.deleteAll();
    }

}