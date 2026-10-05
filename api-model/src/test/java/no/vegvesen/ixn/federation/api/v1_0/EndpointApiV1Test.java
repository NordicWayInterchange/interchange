package no.vegvesen.ixn.federation.api.v1_0;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class EndpointApiV1Test {

    @Test
    public void writeEndpointWithoutBandwidthAndMessageRate() throws JacksonException {
        EndpointApiV1 api = new EndpointApiV1(
                "mySource",
                "myHost",
                123
        );
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(api);
        assertThat(json).doesNotContain("maxMessageRate").doesNotContain("maxBandwidth");
    }

    @Test
    public void readEndpointV1WithoutBandwidthAndMessageRate() throws JacksonException {
        String input = "{\"source\":\"mySource\",\"host\":\"myHost\",\"port\":123}";
        EndpointApiV1 endpointApiV1 = new ObjectMapper().readValue(input, EndpointApiV1.class);
        assertThat(endpointApiV1.getSource()).isEqualTo("mySource");
        assertThat(endpointApiV1.getHost()).isEqualTo("myHost");
        assertThat(endpointApiV1.getPort()).isEqualTo(123);
        assertThat(endpointApiV1.getMaxMessageRate()).isNull();
        assertThat(endpointApiV1.getMaxBandwidth()).isNull();

    }
}
