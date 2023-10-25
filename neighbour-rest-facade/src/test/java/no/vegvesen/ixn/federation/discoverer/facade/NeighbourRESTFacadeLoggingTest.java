package no.vegvesen.ixn.federation.discoverer.facade;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.transformer.CapabilitiesTransformer;
import no.vegvesen.ixn.federation.transformer.CapabilityToCapabilityApiTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionRequestTransformer;
import no.vegvesen.ixn.federation.transformer.SubscriptionTransformer;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

public class NeighbourRESTFacadeLoggingTest {

    @Test
    public void postCapabilities() {
        SubscriptionTransformer subscriptionTransformer = new SubscriptionTransformer();
        Logger logger = (Logger)LoggerFactory.getLogger(NeighbourRESTFacade.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        RestTemplate template = mock(RestTemplate.class);
        NeighbourRESTFacade client = new NeighbourRESTFacade(
                new NeighbourRESTClient(
                        template,
                        new ObjectMapper()
                ),
                new CapabilitiesTransformer(),
                new CapabilityToCapabilityApiTransformer(),
                subscriptionTransformer,
                new SubscriptionRequestTransformer(
                        subscriptionTransformer
                )
        );
    }


}
