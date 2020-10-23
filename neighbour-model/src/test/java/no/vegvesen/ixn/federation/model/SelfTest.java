package no.vegvesen.ixn.federation.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SelfTest {

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
        Self self = new Self("my-host.my-domain.top");
        self.setMessageChannelPort("5678");
        assertThat(self.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top:5678/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
        Self self = new Self("my-host.my-domain.top");
        self.setMessageChannelPort("5671");
        assertThat(self.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameDefaultPorts() {
        Self self = new Self("my-host.my-domain.top");
        assertThat(self.getMessageChannelUrl()).isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithoutDomainNameAndSpecificPort() {
        Self self = new Self("my-host");
        self.setMessageChannelPort("5678");
        assertThat(self.getMessageChannelUrl()).isEqualTo("amqps://my-host:5678/");
    }
}
