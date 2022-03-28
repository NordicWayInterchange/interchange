package no.vegvesen.ixn.federation.service;

import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import javax.transaction.Transactional;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Transactional
public class MatchDiscoveryServiceIT {

    @Autowired
    private MatchRepository matchRepository;

}
