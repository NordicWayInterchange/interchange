package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiTest {

    @Test
    public void createEmptyCapabilitiesApiJson() throws JsonProcessingException {
        CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
        capabilitiesApi.setName("a.itsinterchange.eu");
        Set<CapabilityApi> capSet = new HashSet<>();
        List<String> quadTree = List.of("01230123", "01230122");
        CapabilityApi denm = new CapabilityApi(
                new DenmApplicationApi(
                        "NO-12345",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        quadTree,
                        List.of(3)
                ),
                new MetadataApi()
        );

        CapabilityApi datex = new CapabilityApi(
                new DatexApplicationApi(
                        "NO-12345",
                        "pub-2",
                        "NO",
                        "DATEX2:2.3",
                        quadTree,
                        "SituationPublication",
                        "publisherName"
                ),
                new MetadataApi()
        );

        CapabilityApi ivim = new CapabilityApi(
                new IvimApplicationApi(
                        "NO-12345",
                        "pub-3",
                        "NO",
                        "IVI:1.2",
                        quadTree),
                new MetadataApi()
        );

        capSet.add(datex);
        capSet.add(denm);
        capSet.add(ivim);
        capabilitiesApi.setCapabilities(capSet);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(capabilitiesApi));
    }

    @Test
    public void capabilitiesWithUnknownField() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"version\":\"1.1\",\"foo\":\"bar\",\"name\":\"test\",\"capabilities\":[]}";
        CapabilitiesApi result = mapper.readValue(json, CapabilitiesApi.class);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getCapabilities()).isEmpty();
    }
}