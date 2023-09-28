package no.vegvesen.ixn.federation.qpid;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class BindingTest {
    @Test
    public void readBindingFromJson() throws IOException {
        String object = "{\n" +
                "        \"bindingKey\": \"cap-106173e4-f3be-42a1-a50e-264be5ffbe27\",\n" +
                "        \"destination\": \"bi-queue\",\n" +
                "        \"arguments\": {\n" +
                "          \"x-filter-jms-selector\": \"(protocolVersion = 'MAPEM:1.3.1') AND (quadTree like '%,12002031%' OR quadTree like '%,1200201%' OR quadTree like '%,10223310%' OR quadTree like '%,12001022%' OR quadTree like '%,12001020%' OR quadTree like '%,1200013%' OR quadTree like '%,12002300%' OR quadTree like '%,1022303%' OR quadTree like '%,12002302%' OR quadTree like '%,12001200%' OR quadTree like '%,1200212%' OR quadTree like '%,120030012%' OR quadTree like '%,12001202%' OR quadTree like '%,1200210%' OR quadTree like '%,120030010%' OR quadTree like '%,120030222%' OR quadTree like '%,120030220%' OR quadTree like '%,1200023%' OR quadTree like '%,12001032%' OR quadTree like '%,1023211%' OR quadTree like '%,1023213000%' OR quadTree like '%,102302%' OR quadTree like '%,12003002%' OR quadTree like '%,12003000%' OR quadTree like '%,102232%' OR quadTree like '%,12002033%' OR quadTree like '%,102231%' OR quadTree like '%,1022133%' OR quadTree like '%,12001023%' OR quadTree like '%,12003020%' OR quadTree like '%,12001021%' OR quadTree like '%,12002301%' OR quadTree like '%,12002303%' OR quadTree like '%,120012100%' OR quadTree like '%,12001030%' OR quadTree like '%,1200100%' OR quadTree like '%,12002211%' OR quadTree like '%,12001201%' OR quadTree like '%,102322011%' OR quadTree like '%,12001222%' OR quadTree like '%,1200213%' OR quadTree like '%,12001220%' OR quadTree like '%,120003%' OR quadTree like '%,1200211%' OR quadTree like '%,12001012%' OR quadTree like '%,12002310%' OR quadTree like '%,1022330%' OR quadTree like '%,102320%' OR quadTree like '%,1023212%' OR quadTree like '%,12001010%' OR quadTree like '%,1023210%' OR quadTree like '%,102303%') AND (publisherId = 'NO00005') AND (messageType = 'MAPEM') AND (originatingCountry = 'NO')\"\n" +
                "        },\n" +
                "        \"name\": \"cap-106173e4-f3be-42a1-a50e-264be5ffbe27\",\n" +
                "        \"type\": \"binding\"\n" +
                "      }";

        ObjectMapper mapper = new ObjectMapper();
        Binding result = mapper.readValue(object, Binding.class);
        assertThat(result.getBindingKey()).isEqualTo("cap-106173e4-f3be-42a1-a50e-264be5ffbe27");
    }



}
