package no.vegvesen.ixn.federation.service.importmodel;

import no.vegvesen.ixn.federation.model.LocalSubscription;
import no.vegvesen.ixn.federation.service.ImportTransformer;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportLocalSubscriptionTest {

    @Test
    public void testImportLocalSubscriptionRetainsDescription() {
        LocalSubscriptionImportApi localSubscriptionImportApi = new LocalSubscriptionImportApi(
                UUID.randomUUID().toString(),
                "consumer",
                "a = b",
                LocalSubscriptionImportApi.LocalSubscriptionStatusImportApi.CREATED,
                Set.of(),
                Set.of(),
                "This is a description"
        );
        ImportTransformer importTransformer = new ImportTransformer();
        LocalSubscription localSubscription = importTransformer.transformLocalSubscriptionImportApiToLocalSubscription(localSubscriptionImportApi);
        assertThat(localSubscription.getDescription()).isNotNull();
    }
}
