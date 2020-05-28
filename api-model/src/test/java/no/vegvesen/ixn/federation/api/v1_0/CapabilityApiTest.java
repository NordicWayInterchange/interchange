package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApiTest {

    @Test
    public void createEmptyCapabilityApiJson() throws JsonProcessingException {
        CapabilityApi capabilityApi = new CapabilityApi();
        capabilityApi.setName("a.itsinterchange.eu");
        Set<DataTypeApi> capSet = new HashSet<>();
		Set<String> quadTree = Sets.newLinkedHashSet("01230123", "01230122");
		DenmDataTypeApi denmRoadworksWinterService = new DenmDataTypeApi(
				"NO-12345", "Some Norwegian publisher",
				"NO", "DENM:1.2.2", "application/octet-stream", quadTree,
				"some-denm-service-type?", "3", "6");
		Datex2DataTypeApi datexSituationReroutingWinterDriving = new Datex2DataTypeApi(
				"NO-12345", "Some Norwegian publisher",
				"NO", "DATEX2:2.3", "application/xml", quadTree,
				"SituationPublication", Sets.newLinkedHashSet("ReroutingManagement", "WinterDrivingManagement"));
		IviDataTypeApi iviType128Pictograms557_559_612 = new IviDataTypeApi(
				"NO-12345", "Some Norwegian publisher",
				"NO", "IVI:1.2", "application/base64", quadTree,
				"some-ivi-service-type?", 128, Sets.newLinkedHashSet(557, 559, 612)
		);


        capSet.add(datexSituationReroutingWinterDriving);
        capSet.add(denmRoadworksWinterService);
        capSet.add(iviType128Pictograms557_559_612);
        capabilityApi.setCapabilities(capSet);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(capabilityApi));
    }

}