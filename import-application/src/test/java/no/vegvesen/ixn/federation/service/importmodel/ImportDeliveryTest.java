package no.vegvesen.ixn.federation.service.importmodel;

import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.service.ImportTransformer;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportDeliveryTest {
    @Test
    public void testImportDelivery(){
        DeliveryImportApi deliveryImportApi = new DeliveryImportApi(
                UUID.randomUUID().toString(),
                Set.of(),
                "/path",
                "a = b",
                DeliveryImportApi.DeliveryStatusImportApi.CREATED,
                "This is my delivery",
                false
        );
        ImportTransformer transformer = new ImportTransformer();
        LocalDelivery localDelivery = transformer.transformDeliveryImportApiToLocalDelivery(deliveryImportApi);
        assertThat(localDelivery.getDescription()).isNotNull();
    }

}
