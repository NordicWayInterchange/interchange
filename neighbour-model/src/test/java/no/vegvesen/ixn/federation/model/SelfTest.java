package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SelfTest {

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
        assertThat(Self.getMessageChannelUrl("my-host.my-domain.top", "5678"))
                .isEqualTo("amqps://my-host.my-domain.top:5678/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
        assertThat(Self.getMessageChannelUrl("my-host.my-domain.top", "5671"))
                .isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameDefaultPorts() {
        assertThat(Self.getMessageChannelUrl("my-host.my-domain.top",null))
                .isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithoutDomainNameAndSpecificPort() {
        assertThat(Self.getMessageChannelUrl("my-host", "5678"))
                .isEqualTo("amqps://my-host:5678/");
    }
}
