package no.vegvesen.ixn.federation.service.exportmodel;

import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.model.LocalSubscriptionStatus;
import no.vegvesen.ixn.federation.service.ExportTransformer;
import no.vegvesen.ixn.federation.service.exportmodel.LocalSubscriptionExportApi;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExportLocalSubscriptionTest {

    @Test
    public void testExportLocalSubscription() {
        LocalSubscription localSubscription = new LocalSubscription(
                UUID.randomUUID().toString(),
                LocalSubscriptionStatus.CREATED,
                "a = b",
                "consumer",
                Set.of(),
                Set.of(),
                "This is a description"
        );
        ExportTransformer transformer = new ExportTransformer();
        LocalSubscriptionExportApi localSubscriptionExportApi = transformer.transformLocalSubscriptionToLocalSubscriptionExportApi(localSubscription);
        assertThat(localSubscriptionExportApi.getDescription()).isNotNull();
    }
}
