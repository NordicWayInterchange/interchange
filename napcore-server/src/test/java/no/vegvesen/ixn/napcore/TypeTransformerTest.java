package no.vegvesen.ixn.napcore;

import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.ServiceProvider;
import no.vegvesen.ixn.napcore.model.ServiceProviderBiqueueAccessResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {

    @Test
    public void testHandleNullBiConsumer() {
        ServiceProvider serviceProvider = new ServiceProvider(
                "testuser",
                null, //To explicitly test for null values
                new Capabilities(),
                Set.of(),
                Set.of(),
                LocalDateTime.now()
        );

        TypeTransformer typeTransformer = new TypeTransformer();
        ServiceProviderBiqueueAccessResponse serviceProviderBiqueueAccessResponse = typeTransformer.transformBiconsumerAccess(serviceProvider);
        assertThat(serviceProviderBiqueueAccessResponse.isAccess()).isFalse();
    }
}
