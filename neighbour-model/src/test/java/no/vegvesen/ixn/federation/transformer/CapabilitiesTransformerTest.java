package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesSplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitySplitApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.capability.CapabilitySplit;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesTransformerTest {

    CapabilitiesTransformer transformer = new CapabilitiesTransformer();

    @Test
    public void addMetadataWithoutShardCount() {
        HashSet<CapabilitySplitApi> capabilities = new HashSet<>();
        capabilities.add(new CapabilitySplitApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", List.of(), "myPublicationType", "publisherName"), new MetadataApi()));
        CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi("norway", capabilities);

        Capabilities caps = transformer.capabilitiesApiToCapabilities(capabilitiesApi);

        CapabilitySplit cap = caps.getCapabilities().stream().findFirst().get();

        assertThat(cap.getMetadata().getShardCount()).isEqualTo(1);
    }

    @Test
    public void addMetadataWithShardCount() {
        HashSet<CapabilitySplitApi> capabilities = new HashSet<>();
        MetadataApi metadataApi = new MetadataApi();
        metadataApi.setShardCount(3);
        capabilities.add(new CapabilitySplitApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", List.of(), "myPublicationType","publisherName"), metadataApi));
        CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi("norway", capabilities);

        Capabilities caps = transformer.capabilitiesApiToCapabilities(capabilitiesApi);

        CapabilitySplit cap = caps.getCapabilities().stream().findFirst().get();

        assertThat(cap.getMetadata().getShardCount()).isEqualTo(3);
    }
}
