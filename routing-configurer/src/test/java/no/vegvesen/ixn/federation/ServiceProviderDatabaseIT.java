package no.vegvesen.ixn.federation;

import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.properties.InterchangeNodeProperties;
import no.vegvesen.ixn.federation.qpid.QpidClient;
import no.vegvesen.ixn.federation.qpid.QpidClientConfig;
import no.vegvesen.ixn.federation.repository.MatchRepository;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import no.vegvesen.ixn.federation.service.OutgoingMatchDiscoveryService;
import no.vegvesen.ixn.postgresinit.PostgresTestcontainerInitializer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ContextConfiguration(initializers = {PostgresTestcontainerInitializer.Initializer.class})
@Testcontainers
public class ServiceProviderDatabaseIT {


    @MockBean
    private QpidClient qpidClient;

    @MockBean
    private QpidClientConfig qpidClientConfig;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private OutgoingMatchDiscoveryService outgoingMatchDiscoveryService;

    @Autowired
    private ServiceProviderRouter router;

    @Test
    public void tearDownLocalSubscriptionWithEmptyMatch() {
        LocalSubscription subscription = new LocalSubscription(
                LocalSubscriptionStatus.TEAR_DOWN,
                "a = b",
                getNodeProperties().getName()
        );
        ServiceProvider serviceProvider = new ServiceProvider(
                "sp1",
                Collections.singleton(
                        subscription
                )
        );
        serviceProviderRepository.save(serviceProvider);
        router.processSubscription(serviceProvider,subscription,getNodeProperties().getName(),getNodeProperties().getMessageChannelPort());
        assertThat(serviceProvider.getSubscriptions()).isEmpty();
        verify(qpidClient, never()).queueExists(anyString());
    }

    @Bean
    public InterchangeNodeProperties getNodeProperties() {
        InterchangeNodeProperties interchangeNodeProperties = new InterchangeNodeProperties(
                "localhost",
                "5671"
        );
        return interchangeNodeProperties;
    }
}
