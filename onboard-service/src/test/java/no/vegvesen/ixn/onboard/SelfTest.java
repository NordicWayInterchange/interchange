package no.vegvesen.ixn.onboard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SelfTest {

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedNonDefaultPorts() {
        assertThat(SelfService.getMessageChannelUrl("my-host.my-domain.top", "5678"))
                .isEqualTo("amqps://my-host.my-domain.top:5678/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameAndSpecifiedDefaultPorts() {
        assertThat(SelfService.getMessageChannelUrl("my-host.my-domain.top", "5671"))
                .isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithDomainNameDefaultPorts() {
        assertThat(SelfService.getMessageChannelUrl("my-host.my-domain.top",null))
                .isEqualTo("amqps://my-host.my-domain.top/");
    }

    @Test
    public void getMessageChannelUrlWithoutDomainNameAndSpecificPort() {
        assertThat(SelfService.getMessageChannelUrl("my-host", "5678"))
                .isEqualTo("amqps://my-host:5678/");
    }
}
