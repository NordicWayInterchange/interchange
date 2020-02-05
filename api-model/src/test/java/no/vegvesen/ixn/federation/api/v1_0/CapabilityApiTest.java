package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class CapabilityApiTest {

    @Test
    public void createEmptyCapabilityApiJson() throws JsonProcessingException {
        CapabilityApi capabilityApi = new CapabilityApi();
        capabilityApi.setName("sp-one.bouvetinterchange.no");
        Set<DataTypeApi> capSet = new HashSet<>();
        Datex2DataTypeApi dataTypeApi = new Datex2DataTypeApi("NO");
        capSet.add(dataTypeApi);
        capabilityApi.setCapabilities(capSet);
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(capabilityApi));
    }

}