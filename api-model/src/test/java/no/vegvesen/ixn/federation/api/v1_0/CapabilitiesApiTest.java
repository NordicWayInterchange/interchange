package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.vegvesen.ixn.federation.api.v1_0.capability.*;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiTest {

    @Test
    public void createEmptyCapabilitiesApiJson() throws JsonProcessingException {
        CapabilitiesSplitApi capabilitiesApi = new CapabilitiesSplitApi();
        capabilitiesApi.setName("a.itsinterchange.eu");
        Set<CapabilitySplitApi> capSet = new HashSet<>();
        Set<String> quadTree = Sets.newLinkedHashSet("01230123", "01230122");
        CapabilitySplitApi denm = new CapabilitySplitApi(
                new DenmApplicationApi(
                        "NO-12345",
                        "pub-1",
                        "NO",
                        "DENM:1.2.2",
                        quadTree,
                        Collections.singleton(3)
                ),
                new MetadataApi()
        );

        CapabilitySplitApi datex = new CapabilitySplitApi(
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

        CapabilitySplitApi ivim = new CapabilitySplitApi(
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
        CapabilitiesSplitApi result = mapper.readValue(json,CapabilitiesSplitApi.class);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getCapabilities()).isEmpty();
    }
}