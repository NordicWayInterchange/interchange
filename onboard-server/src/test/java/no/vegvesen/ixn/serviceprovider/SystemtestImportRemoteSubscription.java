package no.vegvesen.ixn.serviceprovider;

import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.federation.repository.ServiceProviderRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@SpringBootTest
@ContextConfiguration(initializers = {ServiceProviderImport.RemoteInitializer.class})
public class SystemtestImportRemoteSubscription {

    @Autowired
    ServiceProviderRepository repository;
    public static String KING_GUSTAF_SUBSCRIPTION = "[{" +
            " \"name\" : \"king_gustaf.bouvetinterchange.eu\"," +
            " \"subscriptions\" : [" +
            " { \"id\" : \"102\"," +
            " \"path\" : \"/pilotinterchange.eu.bouvet.pilotinterchange.eu.npra.io.client1/subscriptions/102\"," +
            " \"selector\" : \"messageType = 'DENM'\"," +
            " \"lastUpdatedTimestamp\" : 1654769198233," +
            " \"status\" : \"CREATED\"," +
            " \"endpoints\" :" +
            " [" +
            " { \"host\" : \"bouvet.pilotinterchange.eu\"," +
            " \"port\" : 5671," +
            " \"source\" : \"c8488ac2-f571-4c6f-aa7b-27b7c17aede4\"," +
            " \"maxBandwidth\" : null," +
            " \"maxMessageRate\" : null" +
            " }" +
            " ]" +
            " }" +
            " ]," +
            " \"capabilities\" : [ ]," +
            " \"deliveries\" : [ ] " +
            "}]";

    @Test
    @Disabled
    public void importSubscriptonToRemote() throws IOException {
        OldServiceProviderApi[] serviceProviders = ServiceProviderImport.getOldServiceProviderApis(new ByteArrayInputStream(KING_GUSTAF_SUBSCRIPTION.getBytes()));
        for (OldServiceProviderApi oldServiceProviderApi : serviceProviders) {
            ServiceProvider serviceProvider = ServiceProviderImport.mapOldServiceProviderApiToServiceProvider(oldServiceProviderApi);
            repository.save(serviceProvider);
        }

    }
}
