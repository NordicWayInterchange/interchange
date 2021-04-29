package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;


public class CapabilityApiTest {

    @Test
    public void testCapabilityWithUnknownField() throws JsonProcessingException {
        CapabilityApi capability = new CapabilityApi();
        capability.setMessageType("TEST");
        capability.setOriginatingCountry("NO");
        capability.setProtocolVersion("1.0");
        capability.setPublisherId("12345");
        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(capability));
        String input = "{\"messageType\":\"DATEX2\",\"foo\":\"bar\",\"publisherId\":\"12345\",\"originatingCountry\":\"NO\",\"protocolVersion\":\"1.0\",\"quadTree\":[]}";
        CapabilityApi result = mapper.readValue(input,CapabilityApi.class);
    }

}
