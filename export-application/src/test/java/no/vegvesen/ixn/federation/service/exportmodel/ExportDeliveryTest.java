package no.vegvesen.ixn.federation.service.exportmodel;

import no.vegvesen.ixn.federation.model.LocalDelivery;
import no.vegvesen.ixn.federation.model.LocalDeliveryStatus;
import no.vegvesen.ixn.federation.service.ExportTransformer;
import no.vegvesen.ixn.federation.service.exportmodel.DeliveryExportApi;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExportDeliveryTest {

    @Test
    public void testExportDelivery() {
        LocalDelivery delivery = new LocalDelivery(
                UUID.randomUUID().toString(),
                Set.of(),
                "a = b",
                LocalDeliveryStatus.CREATED,
                "This is a description",
                false
        );
        ExportTransformer transformer = new ExportTransformer();
        DeliveryExportApi deliveryExportApi = transformer.transformDeliveryToDeliveryExportApi(delivery);
        assertThat(deliveryExportApi.getDescription()).isNotNull();
    }
}
