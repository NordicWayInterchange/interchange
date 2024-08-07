package no.vegvesen.ixn.federation.repository;


import no.vegvesen.ixn.federation.model.*;
import no.vegvesen.ixn.postgresinit.ContainerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
public class MatchRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = ContainerConfig.postgreSQLContainer();

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", ()-> "create-drop");
    }

    @Autowired
    MatchRepository matchRepository;

    @Autowired
    NeighbourRepository neighbourRepository;

    @Autowired
    ServiceProviderRepository serviceProviderRepository;
    private LocalSubscription locSub = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, "a=b","consumer");

    @AfterEach
    public void tearDown() {
        matchRepository.deleteAll();
        serviceProviderRepository.deleteAll();
        neighbourRepository.deleteAll();
    }


    @BeforeEach
    public void setUp() {
        assertThat(matchRepository.findAll()).isEmpty();
        assertThat(serviceProviderRepository.findAll()).isEmpty();
        assertThat(neighbourRepository.findAllByIgnoreIs(false)).isEmpty();
    }

    @Test
    public void saveNeighbourAndServiceProviderBeforeSavingMatch() {
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub, sub, "my-sp");
        matchRepository.save(match);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(1);
    }

    @Test
    public void deletingMatchFromDatabase() {
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub,sub,"my-sp");
        matchRepository.save(match);

        matchRepository.delete(match);

        ServiceProvider serviceProvider = serviceProviderRepository.findByName("my-sp");
        assertThat(serviceProvider.getSubscriptions()).isNotEmpty();

        Neighbour neighbourFromDb = neighbourRepository.findByName("neighbour");
        assertThat(neighbourFromDb.getOurRequestedSubscriptions().getSubscriptions()).isNotEmpty();
    }

    @Test
    public void findOnUnsavedMatch() {
        List<Match> noMatch = matchRepository.findAllBySubscriptionId(1);
        assertThat(noMatch).isEmpty();
    }

    @Test
    public void deleteSubscriptionAndLocalSubscriptionBeforeDeletingMatch() {
        ServiceProvider sp = new ServiceProvider("my-sp");
        sp.addLocalSubscription(locSub);

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription(SubscriptionStatus.REQUESTED, "originatingCountry = 'NO'", "path/1", "my-interchange");
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match = new Match(locSub, sub, "my-sp");
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
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription();
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        Subscription sub1 = new Subscription();
        Neighbour neighbour1 = new Neighbour("neighbour1", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub1)));

        neighbourRepository.save(neighbour);
        neighbourRepository.save(neighbour1);

        assertThat(locSub.getId()).isNotNull(); //in other words, no need to assign it to a new variable, the original one has been updated.

        Match match = new Match(locSub, sub, "my-sp");
        Match match1 = new Match(locSub, sub1, "my-sp");
        matchRepository.save(match);
        matchRepository.save(match1);

        List<Match> allMatches = matchRepository.findAll();
        assertThat(allMatches).hasSize(2);

        List<Match> byLocalSubscriptionId = matchRepository.findAllByLocalSubscriptionId(locSub.getId());
        assertThat(byLocalSubscriptionId).hasSize(2);
    }

    @Test
    public void tryAddingMatchWithLocalSubscriptionFromBase() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());
        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));
        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");

        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);
    }

    @Test
    public void findMatchByOneSubscriptionStatus() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());
        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));
        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");

        matchRepository.save(match);

        assertThat(matchRepository.findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED)).hasSize(1);

    }

    @Test
    public void findMatchByTwoSubscriptionStatuses() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        Subscription sub1 = new Subscription("a=b", SubscriptionStatus.CREATED);
        Neighbour neighbour1 = new Neighbour("neighbour1", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub1)));

        neighbourRepository.save(neighbour);
        neighbourRepository.save(neighbour1);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour1 = neighbourRepository.findByName("neighbour1");
        Subscription savedSubscription1 = savedNeighbour1.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();


        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");
        Match match1 = new Match(savedLocalSubscription, savedSubscription1, "my-sp");
        matchRepository.save(match);
        matchRepository.save(match1);

        assertThat(matchRepository.findAllBySubscription_SubscriptionStatusIn(SubscriptionStatus.REQUESTED, SubscriptionStatus.CREATED)).hasSize(2);
    }

    @Test
    public void localSubscriptionIsNotRemovedWhenMatchIsRemoved() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");
        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);

        Match savedMatch = matchRepository.findAll().get(0);

        matchRepository.delete(savedMatch);

        ServiceProvider getServiceProvider = serviceProviderRepository.findByName("my-sp");

        assertThat(getServiceProvider.getSubscriptions()).hasSize(1);
    }

    @Test
    public void subscriptionIsNotRemovedWhenMatchIsRemoved() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");
        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);

        Match savedMatch = matchRepository.findAll().get(0);

        matchRepository.delete(savedMatch);

        Neighbour getNeighbour = neighbourRepository.findByName("neighbour");

        assertThat(getNeighbour.getOurRequestedSubscriptions().getSubscriptions()).hasSize(1);
    }

    @Test
    public void matchIsNotRemovedWhenLocalSubscriptionIsRemoved() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED);
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");
        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);

        ServiceProvider getServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription getLocalSubscription = getServiceProvider.getSubscriptions().stream().findFirst().get();

        getServiceProvider.removeLocalSubscription(getLocalSubscription.getId());

        serviceProviderRepository.save(getServiceProvider);

        Match savedMatch = matchRepository.findAll().get(0);

        assertThat(savedMatch).isNotNull();
    }

    @Test
    public void matchIsNotRemovedWhenSubscriptionIsRemoved() {
        ServiceProvider sp = new ServiceProvider("my-sp", new Capabilities(), Collections.singleton(locSub), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp);

        Subscription sub = new Subscription("a=b", SubscriptionStatus.REQUESTED, "");
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        ServiceProvider savedServiceProvider = serviceProviderRepository.findByName("my-sp");
        LocalSubscription savedLocalSubscription = savedServiceProvider.getSubscriptions().stream().findFirst().get();

        Neighbour savedNeighbour = neighbourRepository.findByName("neighbour");
        Subscription savedSubscription = savedNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        Match match = new Match(savedLocalSubscription, savedSubscription, "my-sp");
        matchRepository.save(match);

        assertThat(matchRepository.findAll()).hasSize(1);

        Neighbour getNeighbour = neighbourRepository.findByName("neighbour");
        Subscription getSubscription = getNeighbour.getOurRequestedSubscriptions().getSubscriptions().stream().findFirst().get();

        getNeighbour.getOurRequestedSubscriptions().getSubscriptions().remove(getSubscription);

        //TODO: Have to do something with this test, it fails in database when saved to neighbourRepository
        //neighbourRepository.save(getNeighbour);

        Match savedMatch = matchRepository.findAll().get(0);

        assertThat(savedMatch).isNotNull();
    }

    @Test
    public void testMultipleMatchesWithSameExchangeName() {
        String selector = "originatingCountry = 'NO' AND messageType = 'DENM'";
        String serviceProviderName1 = "sp-1";
        String serviceProviderName2 = "sp-2";

        String consumerCommonName = "consumer";
        LocalSubscription locSub1 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector, consumerCommonName);
        ServiceProvider sp1 = new ServiceProvider(serviceProviderName1, new Capabilities(), Collections.singleton(locSub1), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp1);

        LocalSubscription locSub2 = new LocalSubscription(LocalSubscriptionStatus.REQUESTED, selector,consumerCommonName);
        ServiceProvider sp2 = new ServiceProvider(serviceProviderName2, new Capabilities(), Collections.singleton(locSub2), Collections.emptySet(), LocalDateTime.now());

        serviceProviderRepository.save(sp2);

        Subscription sub = new Subscription(selector, SubscriptionStatus.REQUESTED, "my-interchange");
        Endpoint endpoint = new Endpoint("source", "host", 5671, new SubscriptionShard());
        sub.setEndpoints(Collections.singleton(endpoint));
        Neighbour neighbour = new Neighbour("neighbour", new NeighbourCapabilities(), new NeighbourSubscriptionRequest(), new SubscriptionRequest(Collections.singleton(sub)));

        neighbourRepository.save(neighbour);

        Match match1 = new Match(locSub1, sub, serviceProviderName1);
        Match match2 = new Match(locSub2, sub, serviceProviderName2);

        matchRepository.save(match1);
        matchRepository.save(match2);

        assertThat(matchRepository.findAll()).hasSize(2);
    }

}