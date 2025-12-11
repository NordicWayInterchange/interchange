package no.vegvesen.ixn.federation.adminserver;

import no.vegvesen.ixn.federation.adminserver.model.serviceProvider.ServiceProviderApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {

    @Test
    public void testServiceProviderWithNullBiConsumer() {
        ServiceProvider serviceProvider = new ServiceProvider(
                "testuser",
                null, //To explicitly test for null values
                new Capabilities(),
                Set.of(),
                Set.of(),
                LocalDateTime.now()
        );
        TypeTransformer typeTransformer = new TypeTransformer();
        List<ServiceProviderApi> serviceProviderApis = typeTransformer.serviceProviderListToServiceProviderApiList(List.of(serviceProvider));
        ServiceProviderApi serviceProviderApi = serviceProviderApis.stream().findFirst().orElseThrow();
        assertThat(serviceProviderApi.getBiconsumer()).isFalse();


    }
}
