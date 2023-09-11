package no.vegvesen.ixn.federation.api.v1_0;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class EndpointApiTest {

    @Test
    public void writeEndpointWithoutBandwidthAndMessageRate() throws JsonProcessingException {
        EndpointApi api = new EndpointApi(
                "mySource",
                "myHost",
                123
        );
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(api);
        assertThat(json).doesNotContain("maxMessageRate").doesNotContain("maxBandwidth");
    }

    @Test
    public void readEndpointWithoutBandwidthAndMessageRate() throws JsonProcessingException {
        String input = "{\"source\":\"mySource\",\"host\":\"myHost\",\"port\":123}";
        EndpointApi endpointApi = new ObjectMapper().readValue(input,EndpointApi.class);
        assertThat(endpointApi.getSource()).isEqualTo("mySource");
        assertThat(endpointApi.getHost()).isEqualTo("myHost");
        assertThat(endpointApi.getPort()).isEqualTo(123);
        assertThat(endpointApi.getMaxMessageRate()).isNull();
        assertThat(endpointApi.getMaxBandwidth()).isNull();

    }
}
