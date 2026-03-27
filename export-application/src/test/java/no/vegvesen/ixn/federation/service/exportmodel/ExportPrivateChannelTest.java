package no.vegvesen.ixn.federation.service.exportmodel;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.model.PrivateChannelEndpoint;
import no.vegvesen.ixn.federation.model.PrivateChannelStatus;
import no.vegvesen.ixn.federation.service.ExportTransformer;
import no.vegvesen.ixn.federation.service.exportmodel.PrivateChannelExportApi;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ExportPrivateChannelTest {


    @Test
    public void exportPrivateChannelWithDescription() {
        PrivateChannel privateChannel = new PrivateChannel(
                Set.of(),
                PrivateChannelStatus.CREATED,
                "Description",
                new PrivateChannelEndpoint(
                        "host",
                        1234,
                        "queue"

                ),
                "sp-1"
        );
        ExportTransformer transformer = new ExportTransformer();
        PrivateChannelExportApi privateChannelExportApi = transformer.transformPrivateChannelToPrivateChannelExportApi(privateChannel);
        assertThat(privateChannelExportApi.getDescription()).isNotNull();
    }
}
