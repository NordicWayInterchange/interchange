package no.vegvesen.ixn.serviceprovider;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeTransformerTest {

    @Test
    public void typeTransformerConvertUrlToHostAndPortProperly() {
        String url = "amqps://my-interchange:5671";
        List<String> hostAndPort = TypeTransformer.makeHostAndPortOfUrl(url);
        assertThat(hostAndPort.get(0)).isEqualTo("my-interchange");
        assertThat(hostAndPort.get(1)).isEqualTo("5671");
    }

    @Test
    public void typeTransformerConvertUrlToHostAndPortProperlyWithoutSpecifiedPort() {
        String url = "amqps://my-interchange";
        List<String> hostAndPort = TypeTransformer.makeHostAndPortOfUrl(url);
        assertThat(hostAndPort.get(0)).isEqualTo("my-interchange");
        assertThat(hostAndPort.get(1)).isEqualTo("5671");
    }


}
