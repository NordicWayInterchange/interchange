package no.vegvesen.ixn.federation.service.importmodel;

import no.vegvesen.ixn.federation.model.PrivateChannel;
import no.vegvesen.ixn.federation.service.ImportTransformer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ImportPrivateChannelTest {

    @Test
    public void importPrivateChannelWithDescription() {
        PrivateChannelImportApi privateChannelImportApi = new PrivateChannelImportApi(
                UUID.randomUUID().toString(),
                "sp-1",
                List.of(),
                PrivateChannelImportApi.PrivateChannelStatusImportApi.CREATED,
                new PrivateChannelEndpointImportApi(
                        "host",
                        1234,
                        "queue"
                ),
                "This is a description"
        );
        ImportTransformer transformer = new ImportTransformer();
        PrivateChannel privateChannel = transformer.transformPrivateChannelImportApiToPrivateChannel(privateChannelImportApi);
        assertThat(privateChannel.getDescription()).isNotNull();
    }
}
