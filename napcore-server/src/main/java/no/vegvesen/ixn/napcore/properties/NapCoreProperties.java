package no.vegvesen.ixn.napcore.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "napcore.node-provider")
public class NapCoreProperties {

    private String name;

    private String nap;

    private String messageChannelPort = "5671";

    public NapCoreProperties() {

    }

    public NapCoreProperties(String name, String nap) {
        this.name = name;
        this.nap = nap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNap() {
        return nap;
    }

    public void setNap(String nap) {
        this.nap = nap;
    }

    public String getMessageChannelPort() {
        return messageChannelPort;
    }

    public void setMessageChannelPort(String messageChannelPort) {
        this.messageChannelPort = messageChannelPort;
    }
}
