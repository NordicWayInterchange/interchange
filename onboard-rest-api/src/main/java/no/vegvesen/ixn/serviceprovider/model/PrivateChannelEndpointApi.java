package no.vegvesen.ixn.serviceprovider.model;

import no.vegvesen.ixn.federation.model.PrivateChannelEndpoint;

import java.util.Objects;

public class PrivateChannelEndpointApi {
    private String source;
    private Integer port;
    private String queueName;
    public PrivateChannelEndpointApi() {
    }

    public PrivateChannelEndpointApi(String source, Integer port, String queueName) {
        this.source = source;
        this.port = port;
        this.queueName = queueName;
    }
    public PrivateChannelEndpointApi(PrivateChannelEndpoint privateChannelEndpoint){
            this.source = privateChannelEndpoint.getSource();
            this.port = privateChannelEndpoint.getPort();
            this.queueName = privateChannelEndpoint.getQueueName();
    }

    public String getHost() {
        return source;
    }

    public void setHost(String host) {
        this.source = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateChannelEndpointApi that = (PrivateChannelEndpointApi) o;
        return Objects.equals(source, that.source) && Objects.equals(port, that.port) && Objects.equals(queueName, that.queueName);
    }
    @Override
    public int hashCode(){
        return Objects.hash(source, port, queueName);
    }
    public String toString() {
        return "PrivateChannelEndpointApi{" +
                ", source='" + source + '\'' +
                ", port=" + port +
                ", queueName=" + queueName +
                '}';
    }
}
