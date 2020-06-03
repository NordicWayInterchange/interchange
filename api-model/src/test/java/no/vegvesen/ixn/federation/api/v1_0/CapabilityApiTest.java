package no.vegvesen.ixn.federation.api.v1_0;

/*-
 * #%L
 * api-model
 * %%
 * Copyright (C) 2019 - 2020 Nordic Way 3
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

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
