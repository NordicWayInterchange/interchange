package no.vegvesen.ixn.federation.adminserver.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

    private String name;

    private String messageChannelPort = "5671";

    private String biQueueName = "bi-queue";

    private String brokerExternalName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessageChannelPort() {
        return messageChannelPort;
    }

    public void setMessageChannelPort(String messageChannelPort) {
        this.messageChannelPort = messageChannelPort;
    }

    public String getBiQueueName() {
        return biQueueName;
    }

    public void setBiQueueName(String biQueueName) {
        this.biQueueName = biQueueName;
    }

    public String getBrokerExternalName() {
        return brokerExternalName;
    }

    public void setBrokerExternalName(String brokerExternalName) {
        this.brokerExternalName = brokerExternalName;
    }
}
