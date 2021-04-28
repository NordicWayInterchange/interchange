package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilitiesApiTest {

    @Test
    public void createEmptyCapabilitiesApiJson() throws JsonProcessingException {
        CapabilitiesApi capabilitiesApi = new CapabilitiesApi();
        capabilitiesApi.setName("a.itsinterchange.eu");
        Set<CapabilityApi> capSet = new HashSet<>();
		Set<String> quadTree = Sets.newLinkedHashSet("01230123", "01230122");
		DenmCapabilityApi denmRoadworksWinterService = new DenmCapabilityApi(
				"NO-12345",	"NO", "DENM:1.2.2",
				quadTree, Sets.newLinkedHashSet("3"));
		DatexCapabilityApi datexSituationReroutingWinterDriving = new DatexCapabilityApi(
				"NO-12345",
				"NO", "DATEX2:2.3", quadTree,
				Sets.newLinkedHashSet("SituationPublication"));
		IviCapabilityApi iviType128Pictograms557_559_612 = new IviCapabilityApi(
				"NO-12345",
				"NO", "IVI:1.2", quadTree,
				Sets.newLinkedHashSet("128"));


        capSet.add(datexSituationReroutingWinterDriving);
        capSet.add(denmRoadworksWinterService);
        capSet.add(iviType128Pictograms557_559_612);
        capabilitiesApi.setCapabilities(capSet);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(capabilitiesApi));
    }

    @Test
    public void capabilitiesWithUnknownField() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"version\":\"1.1\",\"foo\":\"bar\",\"name\":\"test\",\"capabilities\":[]}";
        CapabilitiesApi result = mapper.readValue(json,CapabilitiesApi.class);
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getCapabilities()).isEmpty();
    }
}