package no.vegvesen.ixn.federation.transformer;

import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilitiesApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.CapabilityApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.DatexApplicationApi;
import no.vegvesen.ixn.federation.api.v1_0.capability.MetadataApi;
import no.vegvesen.ixn.federation.model.Capabilities;
import no.vegvesen.ixn.federation.model.capability.Capability;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesTransformerTest {

    CapabilitiesTransformer transformer = new CapabilitiesTransformer();

    @Test
    public void addMetadataWithoutShardCount() {
        HashSet<CapabilityApi> capabilities = new HashSet<>();
        capabilities.add(new CapabilityApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", List.of(), "myPublicationType", "publisherName"), new MetadataApi()));
        CapabilitiesApi capabilitiesApi = new CapabilitiesApi("norway", capabilities);

        Capabilities caps = transformer.capabilitiesApiToCapabilities(capabilitiesApi);

        Capability cap = caps.getCapabilities().stream().findFirst().get();

        assertThat(cap.getMetadata().getShardCount()).isEqualTo(1);
    }

    @Test
    public void addMetadataWithShardCount() {
        HashSet<CapabilityApi> capabilities = new HashSet<>();
        MetadataApi metadataApi = new MetadataApi();
        metadataApi.setShardCount(3);
        capabilities.add(new CapabilityApi(new DatexApplicationApi("myPublisherId", "pub-1", "NO", "pv1", List.of(), "myPublicationType","publisherName"), metadataApi));
        CapabilitiesApi capabilitiesApi = new CapabilitiesApi("norway", capabilities);

        Capabilities caps = transformer.capabilitiesApiToCapabilities(capabilitiesApi);

        Capability cap = caps.getCapabilities().stream().findFirst().get();

        assertThat(cap.getMetadata().getShardCount()).isEqualTo(3);
    }
}
